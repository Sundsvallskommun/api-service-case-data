package se.sundsvall.casedata.service;

import static java.text.MessageFormat.format;
import static org.zalando.problem.Status.NOT_FOUND;
import static se.sundsvall.casedata.service.util.Constants.ERRAND_WAS_NOT_FOUND;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toAppeal;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toAppealDto;
import static se.sundsvall.casedata.service.util.mappers.PatchMapper.patchAppeal;
import static se.sundsvall.casedata.service.util.mappers.PutMapper.putAppeal;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;

import se.sundsvall.casedata.api.model.AppealDTO;
import se.sundsvall.casedata.api.model.PatchAppealDTO;
import se.sundsvall.casedata.integration.db.AppealRepository;
import se.sundsvall.casedata.integration.db.ErrandRepository;
import se.sundsvall.casedata.integration.db.model.Errand;
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

	public List<AppealDTO> findByErrandIdAndMunicipalityIdAndNamespace(final Long errandId, final String municipalityId, final String namespace) {
		final var errand = getErrandByIdAndMunicipalityIdAndNamespace(errandId, municipalityId, namespace);

		return errand.getAppeals().stream()
			.map(EntityMapper::toAppealDto)
			.toList();

	}

	public AppealDTO findByIdAndMunicipalityIdAndNamespace(final Long id, final String municipalityId, final String namespace) {
		return toAppealDto(appealRepository.findByIdAndMunicipalityIdAndNamespace(id, municipalityId, namespace)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, APPEAL_NOT_FOUND)));
	}

	@Retry(name = "OptimisticLocking")
	public void replaceAppeal(final Long id, final String municipalityId, final String namespace, final AppealDTO dto) {
		final var entity = appealRepository.findByIdAndMunicipalityIdAndNamespace(id, municipalityId, namespace)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, APPEAL_NOT_FOUND));

		if ((entity.getDecision() != null) && !entity.getDecision().getId().equals(dto.getDecisionId())) {
			final var changedDecision = entity.getErrand().getDecisions().stream()
				.filter(decision -> decision.getId().equals(dto.getDecisionId()))
				.findFirst()
				.orElse(null);
			entity.setDecision(changedDecision);
		}

		appealRepository.save(putAppeal(entity, dto));
	}

	@Retry(name = "OptimisticLocking")
	public void updateAppeal(final Long id, final String municipalityId, final String namespace, final PatchAppealDTO dto) {
		final var entity = appealRepository.findByIdAndMunicipalityIdAndNamespace(id, municipalityId, namespace)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, APPEAL_NOT_FOUND));
		appealRepository.save(patchAppeal(entity, dto));
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
	public AppealDTO addAppealToErrand(final Long errandId, final String municipalityId, final String namespace, final AppealDTO appealDTO) {
		final var oldErrand = getErrandByIdAndMunicipalityIdAndNamespace(errandId, municipalityId, namespace);
		final var appeal = toAppeal(appealDTO, municipalityId, namespace);
		appeal.setErrand(oldErrand);

		final var decision = oldErrand.getDecisions().stream()
			.filter(decision1 -> decision1.getId().equals(appealDTO.getDecisionId()))
			.findFirst()
			.orElse(null);

		Optional.ofNullable(decision).ifPresent(appeal::setDecision);
		oldErrand.getAppeals().add(appeal);

		errandRepository.save(oldErrand);
		return toAppealDto(appeal);
	}

	private Errand getErrandByIdAndMunicipalityIdAndNamespace(final Long errandId, final String municipalityId, final String namespace) {
		return errandRepository.findByIdAndMunicipalityIdAndNamespace(errandId, municipalityId, namespace)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND,
				format(ERRAND_WAS_NOT_FOUND, errandId)));
	}


}
