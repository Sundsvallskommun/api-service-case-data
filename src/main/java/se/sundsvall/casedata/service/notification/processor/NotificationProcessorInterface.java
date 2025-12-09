package se.sundsvall.casedata.service.notification.processor;

import se.sundsvall.casedata.api.model.Notification;
import se.sundsvall.casedata.integration.db.model.ErrandEntity;

public interface NotificationProcessorInterface {
	String processNotification(final String municipalityId, final String namespace, final Notification notification, ErrandEntity errandEntity);
}
