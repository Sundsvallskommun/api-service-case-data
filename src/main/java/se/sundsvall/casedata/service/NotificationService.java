package se.sundsvall.casedata.service;

import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;
import static org.zalando.problem.Status.NOT_FOUND;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toNotification;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toNotificationEntity;
import static se.sundsvall.casedata.service.util.mappers.PatchMapper.patchNotification;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;

import generated.se.sundsvall.employee.PortalPersonData;
import se.sundsvall.casedata.api.filter.IncomingRequestFilter;
import se.sundsvall.casedata.api.model.Notification;
import se.sundsvall.casedata.api.model.PatchNotification;
import se.sundsvall.casedata.integration.db.ErrandRepository;
import se.sundsvall.casedata.integration.db.NotificationRepository;
import se.sundsvall.casedata.service.util.mappers.EntityMapper;

@Service
public class NotificationService {

	private static final String NOTIFICATION_ENTITY_NOT_FOUND = "Notification with id '%s' not found in namespace '%s' for municipality with id '%s'";
	private static final String ERRAND_ENTITY_NOT_FOUND = "Errand with id '%s' not found in namespace '%s' for municipality with id '%s'";

	private final IncomingRequestFilter incomingRequestFilter;
	private final NotificationRepository notificationRepository;
	private final ErrandRepository errandRepository;
	private final EmployeeService employeeService;

	public enum EventType {
		CREATE,
		READ,
		UPDATE,
		DELETE,
		ACCESS,
		EXECUTE,
		CANCEL,
		DROP;
	}

	public NotificationService(
		final NotificationRepository notificationRepository,
		final IncomingRequestFilter incomingRequestFilter,
		final ErrandRepository errandRepository,
		final EmployeeService employeeService) {

		this.notificationRepository = notificationRepository;
		this.incomingRequestFilter = incomingRequestFilter;
		this.errandRepository = errandRepository;
		this.employeeService = employeeService;
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

		final var creator = getPortalPersonData(incomingRequestFilter.getAdUser());
		final var owner = getPortalPersonData(notification.getOwnerId());

		return toNotification(notificationRepository.save(toNotificationEntity(notification, municipalityId, namespace, errandEntity, creator, owner)));

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

		final var owner = getPortalPersonData(notification.getOwnerId());

		notificationRepository.save(patchNotification(entity, notification, owner));
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

	private PortalPersonData getPortalPersonData(String adAccountId) {
		return Optional.ofNullable(adAccountId)
			.map(employeeService::getEmployeeByLoginName)
			.orElse(null);
	}
}
