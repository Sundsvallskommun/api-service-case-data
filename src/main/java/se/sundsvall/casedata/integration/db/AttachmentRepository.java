package se.sundsvall.casedata.integration.db;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import se.sundsvall.casedata.integration.db.model.AttachmentEntity;

@JaversSpringDataAuditable
@CircuitBreaker(name = "attachmentRepository")
public interface AttachmentRepository extends JpaRepository<AttachmentEntity, Long>, JpaSpecificationExecutor<AttachmentEntity> {

	List<AttachmentEntity> findAllByErrandIdAndMunicipalityIdAndNamespace(final Long errandId, final String municipalityId, final String namespace);

	boolean existsByErrandIdAndMunicipalityIdAndNamespaceAndHash(final Long errandId, final String municipalityId, final String namespace, final String hash);

	Optional<AttachmentEntity> findByIdAndErrandIdAndMunicipalityIdAndNamespace(final Long id, final Long errandId, final String municipalityId, final String namespace);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	Optional<AttachmentEntity> findWithPessimisticLockingByIdAndErrandIdAndMunicipalityIdAndNamespace(final Long id, final Long errandId, final String municipalityId, final String namespace);

	/*
	 * Attachments belonging to a decision carry decision_id and no errand_id, so they are reached through the queries
	 * below and are never returned by the errand-scoped queries above.
	 */

	List<AttachmentEntity> findAllByDecisionIdAndMunicipalityIdAndNamespace(final Long decisionId, final String municipalityId, final String namespace);

	Optional<AttachmentEntity> findByIdAndDecisionIdAndMunicipalityIdAndNamespace(final Long id, final Long decisionId, final String municipalityId, final String namespace);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	Optional<AttachmentEntity> findWithPessimisticLockingByIdAndDecisionIdAndMunicipalityIdAndNamespace(final Long id, final Long decisionId, final String municipalityId, final String namespace);
}
