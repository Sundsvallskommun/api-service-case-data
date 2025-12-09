package se.sundsvall.casedata.service.notification.processor;

import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toNotificationEntity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import se.sundsvall.casedata.api.model.Notification;
import se.sundsvall.casedata.integration.db.NotificationRepository;
import se.sundsvall.casedata.integration.db.model.ErrandEntity;
import se.sundsvall.casedata.service.EmployeeService;

/**
 * Processor to handle notifications to owner of errand.
 */
@Component
public class ErrandOwnerNotificationProcessor extends AbstractNotificationProcessor {
	private static final Logger LOGGER = LoggerFactory.getLogger(ErrandOwnerNotificationProcessor.class);

	ErrandOwnerNotificationProcessor(final NotificationRepository notificationRepository, final EmployeeService employeeService) {
		super(notificationRepository, employeeService);
	}

	@Override
	public String processNotification(String municipalityId, String namespace, Notification notification, ErrandEntity errandEntity) {
		LOGGER.info("Processing notification in ErrandOwnerNotificationProcessor which will create a notification based on incoming request");

		final var notificationEntity = toNotificationEntity(notification, municipalityId, namespace, errandEntity);
		applyCommmonBusinessLogic(notificationEntity);

		return notificationRepository.save(notificationEntity).getId();
	}
}
