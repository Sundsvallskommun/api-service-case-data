package se.sundsvall.casedata.service.scheduler.supensions;


import static java.time.OffsetDateTime.now;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import se.sundsvall.casedata.api.model.PatchErrand;
import se.sundsvall.casedata.api.model.Suspension;
import se.sundsvall.casedata.integration.db.ErrandRepository;
import se.sundsvall.casedata.service.ErrandService;


@Component
public class SuspensionWorker {

	private final ErrandRepository errandRepository;

	private final ErrandService errandService;

	public SuspensionWorker(final ErrandRepository errandsRepository, final ErrandService errandService) {
		this.errandRepository = errandsRepository;
		this.errandService = errandService;
	}

	@Transactional
	public void cleanUpSuspensions() {

		errandRepository
			.findAllBySuspendedToBefore(now())
			.forEach(entity -> {
				final var errand = PatchErrand.builder()
					.withSuspension(new Suspension()).build();
				errandService.updateErrand(entity.getId(), entity.getMunicipalityId(), entity.getNamespace(), errand);
			});
	}

}
