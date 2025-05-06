package se.sundsvall.casedata.service;

import static java.lang.Boolean.TRUE;
import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;
import static org.springframework.data.domain.Sort.unsorted;
import static org.springframework.util.StringUtils.hasText;
import static org.zalando.problem.Status.NOT_FOUND;
import static se.sundsvall.casedata.service.util.Constants.ERRAND_ENTITY_NOT_FOUND;
import static se.sundsvall.casedata.service.util.Constants.NOTIFICATION_ENTITY_NOT_FOUND;
import static se.sundsvall.casedata.service.util.ServiceUtil.getAdUser;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toNotification;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toNotificationEntity;
import static se.sundsvall.casedata.service.util.mappers.PatchMapper.patchNotification;

import generated.se.sundsvall.employee.PortalPersonData;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.zalando.problem.Problem;
import se.sundsvall.casedata.api.model.Notification;
import se.sundsvall.casedata.api.model.PatchNotification;
import se.sundsvall.casedata.integration.db.ErrandRepository;
import se.sundsvall.casedata.integration.db.NotificationRepository;
import se.sundsvall.casedata.integration.db.model.ErrandEntity;
import se.sundsvall.casedata.integration.db.model.NotificationEntity;
import se.sundsvall.casedata.service.util.mappers.EntityMapper;

@Service
@Transactional
public class NotificationService {

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
		final ErrandRepository errandRepository,
		final EmployeeService employeeService) {

		this.notificationRepository = notificationRepository;
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

	public String create(final String municipalityId, final String namespace, final Notification notification, ErrandEntity errandEntity) {

		final var notificationEntity = toNotificationEntity(notification, municipalityId, namespace, errandEntity);

		applyBusinessLogicForCreate(municipalityId, notificationEntity);

		return toNotification(notificationRepository.save(notificationEntity)).getId();
	}

	public String create(final String municipalityId, final String namespace, final Notification notification) {
		final var errandEntity = errandRepository.findByIdAndMunicipalityIdAndNamespace(notification.getErrandId(), municipalityId, namespace)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, ERRAND_ENTITY_NOT_FOUND.formatted(notification.getErrandId(), namespace, municipalityId)));

		return create(municipalityId, namespace, notification, errandEntity);
	}

	public void update(final String municipalityId, final String namespace, final List<PatchNotification> notifications) {
		notifications.forEach(notification -> updateNotification(municipalityId, namespace, notification.getId(), notification));
	}

	public void delete(final String municipalityId, final String namespace, final Long errandId, final String notificationId) {
		final var entity = notificationRepository.findByIdAndNamespaceAndMunicipalityIdAndErrandId(notificationId, namespace, municipalityId, errandId)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, NOTIFICATION_ENTITY_NOT_FOUND.formatted(notificationId, namespace, municipalityId, errandId)));

		notificationRepository.delete(entity);
	}

	public void globalAcknowledgeNotificationsByErrandId(final String municipalityId, final String namespace, final long errandId) {

		final var errandEntityList = notificationRepository.findAllByNamespaceAndMunicipalityIdAndErrandId(namespace, municipalityId, errandId, unsorted());

		errandEntityList.forEach(errand -> errand.setGlobalAcknowledged(true));

		notificationRepository.saveAll(errandEntityList);
	}

	private void updateNotification(final String municipalityId, final String namespace, final String notificationId, final PatchNotification notification) {
		final var notificationEntity = notificationRepository.findByIdAndNamespaceAndMunicipalityIdAndErrandId(notificationId, namespace, municipalityId, notification.getErrandId())
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, NOTIFICATION_ENTITY_NOT_FOUND.formatted(notificationId, namespace, municipalityId, notification.getErrandId())));

		patchNotification(notificationEntity, notification);

		applyBusinessLogicForUpdate(municipalityId, notification, notificationEntity);

		notificationRepository.save(notificationEntity);
	}

	private PortalPersonData getPortalPersonData(final String municipalityId, final String adAccountId) {
		return Optional.ofNullable(adAccountId)
			.map(userId -> employeeService.getEmployeeByLoginName(municipalityId, userId))
			.orElse(null);
	}

	private void applyBusinessLogicForCreate(final String municipalityId, final NotificationEntity notificationEntity) {

		final var executingUser = getAdUser();

		// If notification is created by the user that owns the notification (ownnerId) it should be acknowledged from start.
		if (equalsIgnoreCase(notificationEntity.getOwnerId(), executingUser)) {
			notificationEntity.setAcknowledged(true);
		}

		// If ownerId is set, use this to fetch "ownerFullName".
		if (hasText(notificationEntity.getOwnerId())) {
			final var ownerFullName = Optional.ofNullable(getPortalPersonData(municipalityId, notificationEntity.getOwnerId()))
				.map(PortalPersonData::getFullname)
				.orElse(null);

			notificationEntity.setOwnerFullName(ownerFullName);
		}

		// If executingUser is set, use this to populate "createdBy" and createdByFullName (but only if createdBy is empty).
		if (StringUtils.hasText(executingUser)) {
			final var createdByFullName = Optional.ofNullable(getPortalPersonData(municipalityId, executingUser))
				.map(PortalPersonData::getFullname)
				.orElse(null);

			notificationEntity.setCreatedBy(executingUser);
			notificationEntity.setCreatedByFullName(createdByFullName);
		}
	}

	private void applyBusinessLogicForUpdate(final String municipalityId, final PatchNotification notification, final NotificationEntity notificationEntity) {

		// If a notification is acknowledged, it's also global_acknowledged.
		if (TRUE.equals(notification.getAcknowledged())) {
			notificationEntity.setGlobalAcknowledged(true);
		}

		// If ownerId is set, fetch "ownerFullName" again.
		if (hasText(notification.getOwnerId())) {
			final var ownerFullName = Optional.ofNullable(getPortalPersonData(municipalityId, notification.getOwnerId()))
				.map(PortalPersonData::getFullname)
				.orElse(null);

			notificationEntity.setOwnerFullName(ownerFullName);
		}
	}
}
