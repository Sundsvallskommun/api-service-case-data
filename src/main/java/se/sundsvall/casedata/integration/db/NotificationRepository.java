package se.sundsvall.casedata.integration.db;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import se.sundsvall.casedata.integration.db.model.NotificationEntity;

@CircuitBreaker(name = "notificationRepository")
public interface NotificationRepository extends JpaRepository<NotificationEntity, String> {

	boolean existsByIdAndNamespaceAndMunicipalityIdAndErrandId(
		final String id,
		final String namespace,
		final String municipalityId,
		final Long errandId);

	Optional<NotificationEntity> findByIdAndNamespaceAndMunicipalityId(
		String id,
		String namespace,
		String municipalityId);

	List<NotificationEntity> findAllByNamespaceAndMunicipalityIdAndOwnerId(
		String namespace,
		String municipalityId,
		String ownerId);

	Optional<NotificationEntity> findByNamespaceAndMunicipalityIdAndOwnerIdAndAcknowledgedAndErrandIdAndType(
		String namespace,
		String municipalityId,
		String ownerId,
		boolean acknowledged,
		long errandId,
		String type);

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
}
