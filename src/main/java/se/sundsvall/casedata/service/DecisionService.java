package se.sundsvall.casedata.service;

import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toDecisionDto;
import static se.sundsvall.casedata.service.util.mappers.PatchMapper.patchDecision;
import static se.sundsvall.casedata.service.util.mappers.PutMapper.putDecision;

import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;

import se.sundsvall.casedata.api.model.DecisionDTO;
import se.sundsvall.casedata.api.model.PatchDecisionDTO;
import se.sundsvall.casedata.integration.db.DecisionRepository;

import io.github.resilience4j.retry.annotation.Retry;

@Service
public class DecisionService {

	private static final ThrowableProblem DECISION_NOT_FOUND_PROBLEM = Problem.valueOf(Status.NOT_FOUND, "Decision not found");

	private final DecisionRepository decisionRepository;

	public DecisionService(final DecisionRepository decisionRepository) {
		this.decisionRepository = decisionRepository;
	}


	public DecisionDTO findById(final Long id) {
		return toDecisionDto(decisionRepository.findById(id)
			.orElseThrow(() -> DECISION_NOT_FOUND_PROBLEM));
	}

	@Retry(name = "OptimisticLocking")
	public void replaceDecision(final Long id, final DecisionDTO dto) {
		final var entity = decisionRepository.findById(id)
			.orElseThrow(() -> DECISION_NOT_FOUND_PROBLEM);
		decisionRepository.save(putDecision(entity, dto));
	}

	@Retry(name = "OptimisticLocking")
	public void updateDecision(final Long id, final PatchDecisionDTO dto) {
		final var entity = decisionRepository.findById(id)
			.orElseThrow(() -> DECISION_NOT_FOUND_PROBLEM);
		decisionRepository.save(patchDecision(entity, dto));
	}

}
