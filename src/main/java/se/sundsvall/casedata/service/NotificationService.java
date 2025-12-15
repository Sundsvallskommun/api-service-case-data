package se.sundsvall.casedata.service;

import static java.lang.Boolean.TRUE;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static org.springframework.data.domain.Sort.unsorted;
import static org.springframework.util.CollectionUtils.isEmpty;
import static org.springframework.util.StringUtils.hasText;
import static org.zalando.problem.Status.NOT_FOUND;
import static se.sundsvall.casedata.service.util.Constants.ERRAND_ENTITY_NOT_FOUND;
import static se.sundsvall.casedata.service.util.Constants.NOTIFICATION_ENTITY_NOT_FOUND;
import static se.sundsvall.casedata.service.util.ServiceUtil.getAdUser;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toNotificationEntity;
import static se.sundsvall.casedata.service.util.mappers.PatchMapper.patchNotification;

import generated.se.sundsvall.employee.PortalPersonData;
import java.util.List;
import org.apache.commons.lang3.Strings;
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
import se.sundsvall.casedata.service.notification.processor.NotificationProcessorInterface;
import se.sundsvall.casedata.service.util.mappers.EntityMapper;

@Service
@Transactional
public class NotificationService {

	private final NotificationRepository notificationRepository;
	private final ErrandRepository errandRepository;
	private final EmployeeService employeeService;
	private final List<NotificationProcessorInterface> notificationProcessors;

	public NotificationService(
		final NotificationRepository notificationRepository,
		final ErrandRepository errandRepository,
		final EmployeeService employeeService,
		final List<NotificationProcessorInterface> notificationProcessors) {

		this.notificationRepository = notificationRepository;
		this.errandRepository = errandRepository;
		this.employeeService = employeeService;
		this.notificationProcessors = notificationProcessors;
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

	/**
	 * Overloaded method used to create notification internally from service for all defined notification processors
	 *
	 * @param municipalityId of the errand that the notification belongs to
	 * @param namespace      of the errand that the notification belongs to
	 * @param notification   the notification to process
	 * @param errandEntity   the errand that the notification belongs to
	 */
	public void create(final String municipalityId, final String namespace, final Notification notification, ErrandEntity errandEntity) {
		create(municipalityId, namespace, notification, errandEntity, null);
	}

	/**
	 * Method used to create notification internally from service for a defined set of NotificationProcessors
	 *
	 * @param municipalityId      of the errand that the notification belongs to
	 * @param namespace           of the errand that the notification belongs to
	 * @param notification        the notification to process
	 * @param errandEntity        the errand that the notification belongs to
	 * @param processorsToExecute a list with full class name for filtering out which processors that shall process the
	 *                            given notification (or null if no filter should be applied)
	 */
	public void create(final String municipalityId, final String namespace, final Notification notification, ErrandEntity errandEntity, List<String> processorsToExecute) {
		ofNullable(notificationProcessors).orElse(emptyList()).stream()
			.filter(processor -> isEmpty(processorsToExecute) || processorsToExecute.contains(processor.getClass().getName()))
			.forEach(processor -> processor.processNotification(municipalityId, namespace, notification, errandEntity));
	}

	/**
	 * Method used for creating notifications based on incoming api request
	 * (called from NotificationResource.createNotification)
	 *
	 * @param  municipalityId of the errand that the message belongs to
	 * @param  namespace      of the errand that the message belongs to
	 * @param  notification   the notification to process
	 * @return                id of the created notification
	 */
	public String create(final String municipalityId, final String namespace, final Notification notification) {
		final var errandEntity = errandRepository.findByIdAndMunicipalityIdAndNamespace(notification.getErrandId(), municipalityId, namespace)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, ERRAND_ENTITY_NOT_FOUND.formatted(notification.getErrandId(), namespace, municipalityId)));

		final var notificationEntity = toNotificationEntity(notification, municipalityId, namespace, errandEntity);

		applyBusinessLogicForCreate(municipalityId, notificationEntity);

		return notificationRepository.save(notificationEntity).getId();
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
		return ofNullable(adAccountId)
			.filter(StringUtils::hasText)
			.map(userId -> employeeService.getEmployeeByLoginName(municipalityId, userId))
			.orElse(null);
	}

	private void applyBusinessLogicForCreate(final String municipalityId, final NotificationEntity notificationEntity) {
		final var executingUser = getAdUser();

		// If notification is created by the user that owns the notification (ownnerId) it should be acknowledged from start.
		if (Strings.CI.equals(notificationEntity.getOwnerId(), executingUser)) {
			notificationEntity.setAcknowledged(true);
		}

		// If ownerId is set, use this to fetch "ownerFullName".
		if (hasText(notificationEntity.getOwnerId())) {
			final var ownerFullName = ofNullable(getPortalPersonData(municipalityId, notificationEntity.getOwnerId()))
				.map(PortalPersonData::getFullname)
				.orElse(null);

			notificationEntity.setOwnerFullName(ownerFullName);
		}

		// If executingUser is set, use this to populate "createdBy" and createdByFullName (but only if createdBy is empty).
		if (hasText(executingUser)) {
			final var createdByFullName = ofNullable(getPortalPersonData(municipalityId, executingUser))
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
			final var ownerFullName = ofNullable(getPortalPersonData(municipalityId, notification.getOwnerId()))
				.map(PortalPersonData::getFullname)
				.orElse(null);

			notificationEntity.setOwnerFullName(ownerFullName);
		}
	}
}
