package se.sundsvall.casedata.integration.db;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import se.sundsvall.casedata.integration.db.model.AttachmentEntity;

/**
 * Dedicated repository for the one-off base64-to-binary attachment backfill (DRAKEN-4446), kept separate from
 * {@link AttachmentRepository} on purpose:
 *
 * <ul>
 * <li><b>Isolated circuit breaker</b> — it uses its own {@code attachmentMigrationRepository} breaker so a burst of
 * migration failures (e.g. a database hiccup during the hours-long backfill, or oversized rows) cannot trip the
 * {@code attachmentRepository} breaker that the live attachment endpoints depend on.</li>
 * <li><b>Not JaVers-audited</b> — the backfill is a technical data transformation, not a business change; auditing it
 * would snapshot the large base64 {@code file} of every migrated row into the audit tables. (The native update below is
 * not intercepted by JaVers anyway, but keeping the whole repository un-audited makes that explicit.)</li>
 * </ul>
 */
@CircuitBreaker(name = "attachmentMigrationRepository")
public interface AttachmentMigrationRepository extends JpaRepository<AttachmentEntity, Long> {

	/**
	 * Returns the ids of attachments still holding their content base64-encoded in the legacy {@code file} column and not
	 * yet migrated to the binary {@code content} blob, ordered by id for deterministic batching. Drives the idempotent
	 * base64-to-binary backfill job: a migrated row gets a non-null {@code content} and drops out of the result set, so
	 * repeated invocations converge to an empty result. Only the ids are selected to keep the (potentially large) base64
	 * payloads out of memory until a row is actually processed.
	 */
	@Query("select a.id from AttachmentEntity a where a.content is null and a.file is not null and length(a.file) > 0 order by a.id")
	List<Long> findIdsForMigration(final Pageable pageable);

	/**
	 * Returns just the legacy base64 {@code file} value of a single attachment, avoiding loading the rest of the (eagerly
	 * fetched) entity graph during the backfill.
	 */
	@Query("select a.file from AttachmentEntity a where a.id = :id")
	Optional<String> findFileById(@Param("id") final Long id);

	/**
	 * Writes the migrated binary {@code content} and its SHA-256 {@code hash} for a single attachment, but only while it is
	 * still un-migrated ({@code content is null}). The {@code content is null} guard makes the backfill idempotent and
	 * safe under retries. This is a native {@code @Modifying} update on purpose: it is not intercepted by JaVers (which
	 * only audits {@code save}/{@code delete}), so the one-off technical migration does not snapshot the large base64
	 * payload of every row into the audit tables, and it rewrites only the two migrated columns rather than the whole row.
	 * Being native it also deliberately leaves the {@code @Version} and {@code @UpdateTimestamp updated} columns untouched
	 * (a technical migration is not a business modification). The decoded bytes are bound as a single parameter, so the
	 * database's {@code max_allowed_packet} must cover the largest attachment.
	 *
	 * @return the number of rows updated: {@code 1} when the row was migrated, {@code 0} when it had already been migrated
	 */
	@Modifying
	@Query(value = "update attachment set content = :content, hash = :hash where id = :id and content is null", nativeQuery = true)
	int applyBinaryContent(@Param("id") final Long id, @Param("content") final byte[] content, @Param("hash") final String hash);
}
