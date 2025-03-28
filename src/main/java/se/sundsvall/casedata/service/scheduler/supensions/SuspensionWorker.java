package se.sundsvall.casedata.service.scheduler.supensions;

import static java.time.OffsetDateTime.now;
import static java.util.Collections.emptyList;
import static se.sundsvall.casedata.api.model.validation.enums.StakeholderRole.ADMINISTRATOR;
import static se.sundsvall.casedata.integration.db.model.enums.NotificationSubType.SUSPENSION;

import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import se.sundsvall.casedata.api.model.Notification;
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

	@Transactional
	public void processExpiredSuspensions() {
		errandRepository
			.findAllBySuspendedToBefore(now())
			.forEach(this::processSuspension);
	}

	private void processSuspension(final ErrandEntity errandEntity) {

		// Create notification
		notificationService.create(errandEntity.getMunicipalityId(), errandEntity.getNamespace(), createNotification(errandEntity), errandEntity);

		// Remove suspension date.
		errandEntity.setSuspendedFrom(null);
		errandEntity.setSuspendedTo(null);
		errandRepository.save(errandEntity);
	}

	private Notification createNotification(final ErrandEntity errandEntity) {
		return Notification.builder()
			.withOwnerFullName(findAdministratorStakeholderFullName(errandEntity))
			.withOwnerId(findAdministratorStakeholderUserId(errandEntity))
			.withType(NOTIFICATION_TYPE)
			.withSubType(SUSPENSION.toString())
			.withDescription(NOTIFICATION_MESSAGE)
			.withErrandId(errandEntity.getId())
			.withErrandNumber(errandEntity.getErrandNumber())
			.build();
	}

	private String findAdministratorStakeholderUserId(final ErrandEntity errand) {
		return Optional.ofNullable(findAdministratorStakeholder(errand))
			.map(StakeholderEntity::getAdAccount)
			.orElse(null);
	}

	private String findAdministratorStakeholderFullName(final ErrandEntity errand) {
		return Optional.ofNullable(findAdministratorStakeholder(errand))
			.map(s -> "%s %s".formatted(s.getFirstName(), s.getLastName()))
			.orElse(null);
	}

	private StakeholderEntity findAdministratorStakeholder(final ErrandEntity errand) {
		return Optional.ofNullable(errand.getStakeholders())
			.orElse(emptyList())
			.stream()
			.filter(obj -> obj.getRoles().contains(ADMINISTRATOR.name()))
			.findFirst()
			.orElse(null);
	}
}
