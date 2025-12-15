package se.sundsvall.casedata.service.notification.processor;

import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static se.sundsvall.casedata.api.model.validation.enums.StakeholderRole.REPORTER;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toNotificationEntity;
import static se.sundsvall.dept44.util.LogUtils.sanitizeForLogging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import se.sundsvall.casedata.api.model.Notification;
import se.sundsvall.casedata.integration.db.NotificationRepository;
import se.sundsvall.casedata.integration.db.model.ErrandEntity;
import se.sundsvall.casedata.integration.db.model.NotificationEntity;
import se.sundsvall.casedata.integration.db.model.StakeholderEntity;
import se.sundsvall.casedata.service.EmployeeService;

/**
 * Processor to handle notifications where errand has a stakeholder having role REPORTER and stakeholder ad account
 * contains non empty value. If a stakeholder that meets the requirements is found a notification is created with the
 * stakeholder as owner, otherwise no notification is created.
 */
@Component
public class ErrandReporterNotificationProcessor extends AbstractNotificationProcessor {
	private static final Logger LOGGER = LoggerFactory.getLogger(ErrandReporterNotificationProcessor.class);

	ErrandReporterNotificationProcessor(final NotificationRepository notificationRepository, final EmployeeService employeeService) {
		super(notificationRepository, employeeService);
	}

	@Override
	public String processNotification(String municipalityId, String namespace, Notification notification, ErrandEntity errandEntity) {
		LOGGER.info("Processing notification in ErrandReporterNotificationProcessor");

		if (isNull(errandEntity)) {
			return null;
		}

		return ofNullable(errandEntity.getStakeholders()).orElse(emptyList()).stream()
			.filter(this::hasReporterRole)
			.map(stakeholderEntity -> {
				LOGGER.info("Stakeholder with ad account '{}' on errand '{}' fulfills required conditions, proceeding to create a notification'", sanitizeForLogging(stakeholderEntity.getAdAccount()), sanitizeForLogging(errandEntity.getErrandNumber()));

				final var notificationEntity = toNotificationEntity(notification, municipalityId, namespace, errandEntity);
				applyBusinessLogic(stakeholderEntity, notificationEntity);

				return notificationRepository.save(notificationEntity);
			})
			.map(NotificationEntity::getId)
			.findFirst()
			.orElse(null);
	}

	private void applyBusinessLogic(final StakeholderEntity stakeholderEntity, final NotificationEntity notificationEntity) {
		// Set ownerId of notification to user id of stakeholder with reporter role (common business logic will use id to
		// calculate full name)
		notificationEntity.setOwnerId(stakeholderEntity.getAdAccount());

		// Apply common business logic
		applyCommmonBusinessLogic(notificationEntity);
	}

	/**
	 * To be processable, the stakeholder of the errand must have a non blank adAccount and have the role of reporter
	 *
	 * @param  stakeholderEntity the stakeholder entity to validate
	 * @return                   true if conditions are fulfilled, false otherwise
	 */
	private boolean hasReporterRole(final StakeholderEntity stakeholderEntity) {
		return isNotBlank(stakeholderEntity.getAdAccount()) && ofNullable(stakeholderEntity.getRoles()).orElse(emptyList()).stream()
			.map(String::trim)
			.anyMatch(role -> REPORTER.name().equals(role));
	}
}
