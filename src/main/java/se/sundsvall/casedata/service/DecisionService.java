package se.sundsvall.casedata.service;

import static org.zalando.problem.Status.NOT_FOUND;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toDecisionDto;
import static se.sundsvall.casedata.service.util.mappers.PatchMapper.patchDecision;
import static se.sundsvall.casedata.service.util.mappers.PutMapper.putDecision;

import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;

import se.sundsvall.casedata.api.model.DecisionDTO;
import se.sundsvall.casedata.api.model.PatchDecisionDTO;
import se.sundsvall.casedata.integration.db.DecisionRepository;

import io.github.resilience4j.retry.annotation.Retry;

@Service
public class DecisionService {

	private static final String DECISION_NOT_FOUND = "Decision not found";

	private final DecisionRepository decisionRepository;

	public DecisionService(final DecisionRepository decisionRepository) {
		this.decisionRepository = decisionRepository;
	}

	public DecisionDTO findByIdAndMunicipalityId(final Long id, final String municipalityId, final String namespace) {
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

}
