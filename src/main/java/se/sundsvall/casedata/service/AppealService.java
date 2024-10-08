package se.sundsvall.casedata.service;

import static java.text.MessageFormat.format;
import static org.zalando.problem.Status.NOT_FOUND;
import static se.sundsvall.casedata.service.util.Constants.ERRAND_WAS_NOT_FOUND;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toAppeal;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toAppealEntity;
import static se.sundsvall.casedata.service.util.mappers.PatchMapper.patchAppeal;
import static se.sundsvall.casedata.service.util.mappers.PutMapper.putAppeal;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;

import se.sundsvall.casedata.api.model.Appeal;
import se.sundsvall.casedata.api.model.PatchAppeal;
import se.sundsvall.casedata.integration.db.AppealRepository;
import se.sundsvall.casedata.integration.db.ErrandRepository;
import se.sundsvall.casedata.integration.db.model.ErrandEntity;
import se.sundsvall.casedata.service.util.mappers.EntityMapper;

import io.github.resilience4j.retry.annotation.Retry;

@Service
public class AppealService {

	private static final String APPEAL_NOT_FOUND = "Appeal not found";

	private static final String APPEAL_WITH_ID_X_WAS_NOT_FOUND_ON_ERRAND_WITH_ID_X = "Appeal with id: {0} was not found on errand with id: {1}";

	private final ErrandRepository errandRepository;

	private final AppealRepository appealRepository;

	private final ProcessService processService;

	public AppealService(final ErrandRepository errandRepository, final AppealRepository appealRepository, final ProcessService processService) {
		this.errandRepository = errandRepository;
		this.appealRepository = appealRepository;
		this.processService = processService;
	}

	public List<Appeal> findAllAppealsOnErrand(final Long errandId, final String municipalityId, final String namespace) {
		final var errand = getErrandByIdAndMunicipalityIdAndNamespace(errandId, municipalityId, namespace);

		return errand.getAppeals().stream()
			.map(EntityMapper::toAppeal)
			.toList();

	}

	public Appeal findAppealOnErrand(final Long errandId, final Long id, final String municipalityId, final String namespace) {
		var errand = getErrandByIdAndMunicipalityIdAndNamespace(errandId, municipalityId, namespace);

		return errand.getAppeals().stream()
			.filter(appeal -> appeal.getId().equals(id))
			.findFirst()
			.map(EntityMapper::toAppeal)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, APPEAL_NOT_FOUND));
	}

	@Retry(name = "OptimisticLocking")
	public void replaceAppeal(final Long errandId, final Long id, final String municipalityId, final String namespace, final Appeal newAppeal) {
		var errand = getErrandByIdAndMunicipalityIdAndNamespace(errandId, municipalityId, namespace);

		var oldEntity = errand.getAppeals().stream()
			.filter(appeal -> appeal.getId().equals(id))
			.findFirst()
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, APPEAL_NOT_FOUND));

		var newDecision = errand.getDecisions().stream()
			.filter(decision -> decision.getId().equals(newAppeal.getDecisionId()))
			.findFirst()
			.orElse(null);

		oldEntity.setDecision(newDecision);

		appealRepository.save(putAppeal(oldEntity, newAppeal));
	}


	@Retry(name = "OptimisticLocking")
	public void updateAppeal(final Long errandId, final Long id, final String municipalityId, final String namespace, final PatchAppeal patchAppeal) {
		var errand = getErrandByIdAndMunicipalityIdAndNamespace(errandId, municipalityId, namespace);

		final var oldEntity = errand.getAppeals().stream()
			.filter(appeal -> appeal.getId().equals(id))
			.findFirst()
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, APPEAL_NOT_FOUND));

		appealRepository.save(patchAppeal(oldEntity, patchAppeal));
	}


	@Retry(name = "OptimisticLocking")
	public void deleteAppealOnErrand(final Long errandId, final String municipalityId, final String namespace, final Long appealId) {
		final var errand = getErrandByIdAndMunicipalityIdAndNamespace(errandId, municipalityId, namespace);
		final var appealToRemove = errand.getAppeals().stream()
			.filter(appeal -> appeal.getId().equals(appealId))
			.findAny()
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, format(APPEAL_WITH_ID_X_WAS_NOT_FOUND_ON_ERRAND_WITH_ID_X, appealId, errandId)));
		errand.getAppeals().remove(appealToRemove);
		errandRepository.save(errand);
		processService.updateProcess(errand);
	}

	@Retry(name = "OptimisticLocking")
	public Appeal addAppealToErrand(final Long errandId, final String municipalityId, final String namespace, final Appeal appeal) {
		final var oldErrand = getErrandByIdAndMunicipalityIdAndNamespace(errandId, municipalityId, namespace);
		final var appealEntity = toAppealEntity(appeal, municipalityId, namespace);
		appealEntity.setErrand(oldErrand);

		final var decision = oldErrand.getDecisions().stream()
			.filter(decision1 -> decision1.getId().equals(appeal.getDecisionId()))
			.findFirst()
			.orElse(null);

		Optional.ofNullable(decision).ifPresent(appealEntity::setDecision);
		oldErrand.getAppeals().add(appealEntity);

		errandRepository.save(oldErrand);
		return toAppeal(appealEntity);
	}

	private ErrandEntity getErrandByIdAndMunicipalityIdAndNamespace(final Long errandId, final String municipalityId, final String namespace) {
		return errandRepository.findByIdAndMunicipalityIdAndNamespace(errandId, municipalityId, namespace)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND,
				format(ERRAND_WAS_NOT_FOUND, errandId)));
	}


}
