package se.sundsvall.casedata.service;

import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;
import static org.zalando.problem.Status.NOT_FOUND;
import static se.sundsvall.casedata.service.util.Constants.ERRAND_ENTITY_NOT_FOUND;
import static se.sundsvall.casedata.service.util.Constants.NOTIFICATION_ENTITY_NOT_FOUND;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toNotification;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toNotificationEntity;
import static se.sundsvall.casedata.service.util.mappers.PatchMapper.patchNotification;

import generated.se.sundsvall.employee.PortalPersonData;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Sort;
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
		DROP
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

	public List<Notification> findNotificationsByOwnerId(final String municipalityId, final String namespace, final String ownerId) {
		return notificationRepository.findAllByNamespaceAndMunicipalityIdAndOwnerId(namespace, municipalityId, ownerId)
			.stream()
			.map(EntityMapper::toNotification)
			.toList();
	}

	public List<Notification> findNotifications(final String municipalityId, final String namespace, final Long errandId, final Sort sort) {
		return notificationRepository.findAllByNamespaceAndMunicipalityIdAndErrandId(namespace, municipalityId, errandId, sort)
			.stream()
			.map(EntityMapper::toNotification)
			.toList();
	}

	public Notification findNotification(final String municipalityId, final String namespace, final Long errandId, final String notificationId) {
		return notificationRepository.findByIdAndNamespaceAndMunicipalityIdAndErrandId(notificationId, namespace, municipalityId, errandId)
			.map(EntityMapper::toNotification)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, NOTIFICATION_ENTITY_NOT_FOUND.formatted(notificationId, namespace, municipalityId, errandId)));
	}

	public Notification create(final String municipalityId, final String namespace, final Notification notification) {
		if ((notification.getOwnerId() == null) || isExecutingUserTheOwner(notification.getOwnerId()) || notificationExists(municipalityId, namespace, notification)) {
			return null;
		}

		final var errandEntity = errandRepository.findByIdAndMunicipalityIdAndNamespace(notification.getErrandId(), municipalityId, namespace)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, ERRAND_ENTITY_NOT_FOUND.formatted(notification.getErrandId(), namespace, municipalityId)));

		final var creator = getPortalPersonData(municipalityId, incomingRequestFilter.getAdUser());
		final var owner = getPortalPersonData(municipalityId, notification.getOwnerId());

		return toNotification(notificationRepository.save(toNotificationEntity(notification, municipalityId, namespace, errandEntity, creator, owner)));
	}

	public void update(final String municipalityId, final String namespace, final List<PatchNotification> notifications) {
		notifications.forEach(notification -> updateNotification(municipalityId, namespace, notification.getId(), notification));
	}

	public void delete(final String municipalityId, final String namespace, final Long errandId, final String notificationId) {
		final var entity = notificationRepository.findByIdAndNamespaceAndMunicipalityIdAndErrandId(notificationId, namespace, municipalityId, errandId)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, NOTIFICATION_ENTITY_NOT_FOUND.formatted(notificationId, namespace, municipalityId, errandId)));

		notificationRepository.delete(entity);
	}

	private void updateNotification(final String municipalityId, final String namespace, final String notificationId, final PatchNotification notification) {
		final var entity = notificationRepository.findByIdAndNamespaceAndMunicipalityIdAndErrandId(notificationId, namespace, municipalityId, notification.getErrandId())
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, NOTIFICATION_ENTITY_NOT_FOUND.formatted(notificationId, namespace, municipalityId, notification.getErrandId())));

		final var owner = getPortalPersonData(municipalityId, notification.getOwnerId());

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

	private PortalPersonData getPortalPersonData(final String municipalityId, final String adAccountId) {
		return Optional.ofNullable(adAccountId)
			.map(userId -> employeeService.getEmployeeByLoginName(municipalityId, userId))
			.orElse(null);
	}
}
