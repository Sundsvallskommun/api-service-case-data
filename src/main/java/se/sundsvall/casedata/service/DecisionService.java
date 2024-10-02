package se.sundsvall.casedata.service;

import static java.text.MessageFormat.format;
import static org.zalando.problem.Status.NOT_FOUND;
import static se.sundsvall.casedata.service.util.Constants.ERRAND_WAS_NOT_FOUND;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toDecision;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toDecisionDto;
import static se.sundsvall.casedata.service.util.mappers.PatchMapper.patchDecision;
import static se.sundsvall.casedata.service.util.mappers.PutMapper.putDecision;

import java.util.List;

import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;

import se.sundsvall.casedata.api.model.DecisionDTO;
import se.sundsvall.casedata.api.model.PatchDecisionDTO;
import se.sundsvall.casedata.integration.db.DecisionRepository;
import se.sundsvall.casedata.integration.db.ErrandRepository;
import se.sundsvall.casedata.integration.db.model.Decision;
import se.sundsvall.casedata.integration.db.model.Errand;
import se.sundsvall.casedata.service.util.mappers.EntityMapper;

import io.github.resilience4j.retry.annotation.Retry;

@Service
public class DecisionService {

	private static final String DECISION_WAS_NOT_FOUND_ON_ERRAND_WITH_ID = "Decision was not found on errand with id: {0}";

	private static final String DECISION_WITH_ID_X_WAS_NOT_FOUND_ON_ERRAND_WITH_ID_X = "Decision with id: {0} was not found on errand with id: {1}";

	private static final String DECISION_NOT_FOUND = "Decision not found";

	private final DecisionRepository decisionRepository;

	private final ErrandRepository errandRepository;

	private final ProcessService processService;

	public DecisionService(final DecisionRepository decisionRepository, final ErrandRepository errandRepository, final ProcessService processService) {
		this.decisionRepository = decisionRepository;
		this.errandRepository = errandRepository;
		this.processService = processService;
	}

	public List<DecisionDTO> findDecisionsOnErrand(final Long errandId, final String municipalityId, final String namespace) {
		final List<Decision> decisionList = getErrandByIdAndMunicipalityIdAndNamespace(errandId, municipalityId, namespace).getDecisions();
		if ((decisionList == null) || decisionList.isEmpty()) {
			throw Problem.valueOf(NOT_FOUND, format(DECISION_WAS_NOT_FOUND_ON_ERRAND_WITH_ID, errandId));
		}
		return decisionList.stream().map(EntityMapper::toDecisionDto).toList();
	}

	public DecisionDTO findByIdAndMunicipalityIdAndNamespace(final Long id, final String municipalityId, final String namespace) {
		return toDecisionDto(decisionRepository.findByIdAndMunicipalityIdAndNamespace(id, municipalityId, namespace)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, DECISION_NOT_FOUND)));
	}

	@Retry(name = "OptimisticLocking")
	public void replaceDecision(final Long id, final String municipalityId, final String namespace, final DecisionDTO dto) {
		final var entity = decisionRepository.findByIdAndMunicipalityIdAndNamespace(id, municipalityId, namespace)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, DECISION_NOT_FOUND));
		decisionRepository.save(putDecision(entity, dto));
	}

	@Retry(name = "OptimisticLocking")
	public void updateDecision(final Long id, final String municipalityId, final String namespace, final PatchDecisionDTO dto) {
		final var entity = decisionRepository.findByIdAndMunicipalityIdAndNamespace(id, municipalityId, namespace)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, DECISION_NOT_FOUND));
		decisionRepository.save(patchDecision(entity, dto));
	}


	@Retry(name = "OptimisticLocking")
	public DecisionDTO addDecisionToErrand(final Long errandId, final String municipalityId, final String namespace, final DecisionDTO decisionDTO) {
		final var oldErrand = getErrandByIdAndMunicipalityIdAndNamespace(errandId, municipalityId, namespace);
		final var decision = toDecision(decisionDTO, municipalityId, namespace);
		decision.setErrand(oldErrand);
		oldErrand.getDecisions().add(decision);
		final var updatedErrand = errandRepository.save(oldErrand);
		processService.updateProcess(updatedErrand);
		return toDecisionDto(decision);
	}


	@Retry(name = "OptimisticLocking")
	public void deleteDecisionOnErrand(final Long errandId, final String municipalityId, final String namespace, final Long decisionId) {
		final var errand = getErrandByIdAndMunicipalityIdAndNamespace(errandId, municipalityId, namespace);
		final var decisionToRemove = errand.getDecisions().stream()
			.filter(decision -> decision.getId().equals(decisionId))
			.findAny()
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, format(DECISION_WITH_ID_X_WAS_NOT_FOUND_ON_ERRAND_WITH_ID_X, decisionId, errandId)));
		errand.getDecisions().remove(decisionToRemove);
		errandRepository.save(errand);
		processService.updateProcess(errand);
	}

	public Errand getErrandByIdAndMunicipalityIdAndNamespace(final Long errandId, final String municipalityId, final String namespace) {
		return errandRepository.findByIdAndMunicipalityIdAndNamespace(errandId, municipalityId, namespace)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND,
				format(ERRAND_WAS_NOT_FOUND, errandId)));
	}

}
