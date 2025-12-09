package se.sundsvall.casedata.service.notification.processor;

import static java.util.Optional.ofNullable;
import static org.springframework.util.StringUtils.hasText;
import static se.sundsvall.casedata.service.util.ServiceUtil.getAdUser;

import generated.se.sundsvall.employee.PortalPersonData;
import org.apache.commons.lang3.Strings;
import se.sundsvall.casedata.integration.db.NotificationRepository;
import se.sundsvall.casedata.integration.db.model.NotificationEntity;
import se.sundsvall.casedata.service.EmployeeService;

abstract class AbstractNotificationProcessor implements NotificationProcessorInterface {

	final NotificationRepository notificationRepository;
	final EmployeeService employeeService;

	AbstractNotificationProcessor(final NotificationRepository notificationRepository, final EmployeeService employeeService) {
		this.notificationRepository = notificationRepository;
		this.employeeService = employeeService;
	}

	void applyCommmonBusinessLogic(final NotificationEntity notificationEntity) {
		final var executingUser = getAdUser();

		// If notification is created by the user that owns the notification (ownnerId) it should be acknowledged from start.
		if (Strings.CI.equals(notificationEntity.getOwnerId(), executingUser)) {
			notificationEntity.setAcknowledged(true);
		}

		// If ownerId is set, use this to fetch "ownerFullName".
		if (hasText(notificationEntity.getOwnerId())) {
			notificationEntity.setOwnerFullName(getFullname(notificationEntity.getMunicipalityId(), notificationEntity.getOwnerId()));
		}

		// If executingUser is set, use this to populate "createdBy" and createdByFullName.
		if (hasText(executingUser)) {
			notificationEntity.setCreatedBy(executingUser);
			notificationEntity.setCreatedByFullName(getFullname(notificationEntity.getMunicipalityId(), executingUser));
		}
	}

	private String getFullname(final String municipalityId, final String nullableUserId) {
		return ofNullable(nullableUserId)
			.map(userId -> employeeService.getEmployeeByLoginName(municipalityId, userId))
			.map(PortalPersonData::getFullname)
			.orElse(null);
	}
}
