package se.sundsvall.casedata.service;

import java.util.List;

import org.springframework.stereotype.Service;

import se.sundsvall.casedata.api.model.Notification;
import se.sundsvall.casedata.api.model.PatchNotification;
import se.sundsvall.casedata.api.model.PostNotification;
import se.sundsvall.dept44.util.jacoco.ExcludeFromJacocoGeneratedCoverageReport;

@Service
@ExcludeFromJacocoGeneratedCoverageReport // TODO: remove this line
public class NotificationService {

	public List<Notification> getNotifications(String municipalityId, String namespace, String ownerId) {
		/* TODO Implement */
		return List.of(Notification.builder().build());
	}

	public Notification getNotification(String municipalityId, String namespace, String notificationId) {
		/* TODO Implement */
		return Notification.builder().build();
	}

	public Notification createNotification(String municipalityId, String namespace, PostNotification notification) {
		/* TODO Implement */
		return Notification.builder().build();
	}

	public void updateNotifications(String municipalityId, String namespace, List<PatchNotification> notifications) {
		/* TODO Implement */
	}

	public void deleteNotification(String municipalityId, String namespace, String notificationId) {
		/* TODO Implement */
	}
}
