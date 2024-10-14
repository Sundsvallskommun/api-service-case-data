package se.sundsvall.casedata.service;

import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;
import static org.zalando.problem.Status.NOT_FOUND;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toNotification;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toNotificationEntity;
import static se.sundsvall.casedata.service.util.mappers.PatchMapper.patchNotification;

import java.util.List;

import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;

import se.sundsvall.casedata.api.filter.IncomingRequestFilter;
import se.sundsvall.casedata.api.model.Notification;
import se.sundsvall.casedata.api.model.PatchNotification;
import se.sundsvall.casedata.integration.db.ErrandRepository;
import se.sundsvall.casedata.integration.db.NotificationRepository;
import se.sundsvall.casedata.service.util.mappers.EntityMapper;

@Service
public class NotificationService {

	private static final String NOTIFICATION_ENTITY_NOT_FOUND = "Notification with id '%s' not found in namespace '%s' for municipality with id '%s'";
	private static final String ERRAND_ENTITY_NOT_FOUND = "Errand with id '%'s not found in namespace '%s' for municipality with id '%s'";

	private final IncomingRequestFilter incomingRequestFilter;
	private final NotificationRepository notificationRepository;
	private final ErrandRepository errandRepository;

	public NotificationService(final NotificationRepository notificationRepository, final IncomingRequestFilter incomingRequestFilter, final ErrandRepository errandRepository) {
		this.notificationRepository = notificationRepository;
		this.incomingRequestFilter = incomingRequestFilter;
		this.errandRepository = errandRepository;
	}

	public List<Notification> getNotifications(String municipalityId, String namespace, String ownerId) {
		return notificationRepository.findAllByNamespaceAndMunicipalityIdAndOwnerId(namespace, municipalityId, ownerId).stream()
			.map(EntityMapper::toNotification)
			.toList();
	}

	public Notification getNotification(String municipalityId, String namespace, String notificationId) {
		return notificationRepository.findByIdAndNamespaceAndMunicipalityId(notificationId, namespace, municipalityId)
			.map(EntityMapper::toNotification)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, NOTIFICATION_ENTITY_NOT_FOUND.formatted(notificationId, namespace, municipalityId)));
	}

	public Notification createNotification(String municipalityId, String namespace, Notification notification) {
		if ((notification.getOwnerId() == null) || isExecutingUserTheOwner(notification.getOwnerId()) || notificationExists(municipalityId, namespace, notification)) {
			return null;
		}

		final var errandEntity = errandRepository.findByIdAndMunicipalityIdAndNamespace(notification.getErrandId(), municipalityId, namespace)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, ERRAND_ENTITY_NOT_FOUND.formatted(notification.getErrandId(), namespace, municipalityId)));

		return toNotification(notificationRepository.save(toNotificationEntity(notification, municipalityId, namespace, errandEntity)));
	}

	public void updateNotifications(String municipalityId, String namespace, List<PatchNotification> notifications) {
		notifications.forEach(notification -> updateNotification(municipalityId, namespace, notification.getId(), notification));
	}

	public void deleteNotification(String municipalityId, String namespace, String notificationId) {
		final var entity = notificationRepository.findByIdAndNamespaceAndMunicipalityId(notificationId, namespace, municipalityId)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, NOTIFICATION_ENTITY_NOT_FOUND.formatted(notificationId, namespace, municipalityId)));

		notificationRepository.delete(entity);
	}

	private void updateNotification(final String municipalityId, final String namespace, final String notificationId, final PatchNotification notification) {
		final var entity = notificationRepository.findByIdAndNamespaceAndMunicipalityId(notificationId, namespace, municipalityId)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, NOTIFICATION_ENTITY_NOT_FOUND.formatted(notificationId, namespace, municipalityId)));

		notificationRepository.save(patchNotification(entity, notification));
	}

	private boolean notificationExists(final String municipalityId, final String namespace, final Notification notification) {
		return notificationRepository
			.findByNamespaceAndMunicipalityIdAndOwnerIdAndAcknowledgedAndErrandIdAndType(
				namespace,
				municipalityId,
				notification.getOwnerId(),
				notification.isAcknowledged(),
				notification.getErrandId(),
				notification.getType())
			.isPresent();
	}

	private boolean isExecutingUserTheOwner(final String ownerId) {
		return equalsIgnoreCase(ownerId, incomingRequestFilter.getAdUser());
	}
}
