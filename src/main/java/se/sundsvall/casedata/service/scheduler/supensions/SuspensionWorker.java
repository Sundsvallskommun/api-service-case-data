package se.sundsvall.casedata.service.scheduler.supensions;

import static java.time.OffsetDateTime.now;
import static java.util.Collections.emptyList;

import java.util.Optional;
import org.springframework.stereotype.Component;
import se.sundsvall.casedata.api.model.Notification;
import se.sundsvall.casedata.api.model.validation.enums.StakeholderRole;
import se.sundsvall.casedata.integration.db.ErrandRepository;
import se.sundsvall.casedata.integration.db.model.ErrandEntity;
import se.sundsvall.casedata.integration.db.model.StakeholderEntity;
import se.sundsvall.casedata.service.NotificationService;

@Component
public class SuspensionWorker {

	private static final String NOTIFICATION_MESSAGE = "Parkering av ärendet har upphört";

	private static final String NOTIFICATION_TYPE = "UPDATE";

	private final ErrandRepository errandRepository;

	private final NotificationService notificationService;

	public SuspensionWorker(final ErrandRepository errandsRepository, final NotificationService notificationService) {
		this.errandRepository = errandsRepository;
		this.notificationService = notificationService;
	}

	public void processExpiredSuspensions() {
		errandRepository
			.findAllBySuspendedToBefore(now())
			.forEach(entity -> notificationService
				.createNotification(entity.getMunicipalityId(), entity.getNamespace(), createNotification(entity)));
	}

	private Notification createNotification(ErrandEntity errand) {
		var stakeholder = findAdministrator(errand);

		return Notification.builder()
			.withOwnerFullName("%s %s".formatted(stakeholder.getFirstName(), stakeholder.getLastName()))
			.withOwnerId(stakeholder.getAdAccount())
			.withType(NOTIFICATION_TYPE)
			.withDescription(NOTIFICATION_MESSAGE)
			.withErrandId(errand.getId())
			.withErrandNumber(errand.getErrandNumber())
			.build();
	}

	private StakeholderEntity findAdministrator(final ErrandEntity errand) {
		return Optional.ofNullable(errand.getStakeholders())
			.orElse(emptyList())
			.stream()
			.filter(obj -> obj.getRoles()
				.contains(StakeholderRole.ADMINISTRATOR.name()))
			.findFirst()
			.orElseThrow(() -> new IllegalStateException("No administrator found for errand with id: %s".formatted(errand.getId())));
	}

}
