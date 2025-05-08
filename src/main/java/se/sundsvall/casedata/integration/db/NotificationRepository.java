package se.sundsvall.casedata.integration.db;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import se.sundsvall.casedata.integration.db.model.NotificationEntity;

@CircuitBreaker(name = "notificationRepository")
public interface NotificationRepository extends JpaRepository<NotificationEntity, String> {

	List<NotificationEntity> findAllByNamespaceAndMunicipalityIdAndOwnerId(
		String namespace,
		String municipalityId,
		String ownerId);

	List<NotificationEntity> findByExpiresBefore(final OffsetDateTime expires);

	List<NotificationEntity> findAllByNamespaceAndMunicipalityIdAndErrandId(
		String namespace,
		String municipalityId,
		Long errandId,
		Sort sort);

	Optional<NotificationEntity> findByIdAndNamespaceAndMunicipalityIdAndErrandId(
		String notificationId,
		String namespace,
		String municipalityId,
		Long errandId);

	void deleteAllByExpiresBefore(OffsetDateTime dateTime);
}
