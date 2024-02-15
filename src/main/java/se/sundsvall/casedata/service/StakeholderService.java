package se.sundsvall.casedata.service;

import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toStakeholderDto;
import static se.sundsvall.casedata.service.util.mappers.PatchMapper.patchStakeholder;
import static se.sundsvall.casedata.service.util.mappers.PutMapper.putStakeholder;

import java.util.List;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;

import se.sundsvall.casedata.api.model.StakeholderDTO;
import se.sundsvall.casedata.integration.db.StakeholderRepository;
import se.sundsvall.casedata.integration.db.model.Stakeholder;
import se.sundsvall.casedata.integration.db.model.enums.StakeholderRole;
import se.sundsvall.casedata.service.util.mappers.EntityMapper;

import io.github.resilience4j.retry.annotation.Retry;

@Service
@Transactional
public class StakeholderService {

	private static final ThrowableProblem STAKEHOLDER_NOT_FOUND_PROBLEM = Problem.valueOf(Status.NOT_FOUND, "Stakeholder not found");

	private final StakeholderRepository stakeholderRepository;

	public StakeholderService(final StakeholderRepository stakeholderRepository) {
		this.stakeholderRepository = stakeholderRepository;
	}

	public List<StakeholderDTO> findAllStakeholders() {
		final List<Stakeholder> stakeholderList = stakeholderRepository.findAll();
		if (stakeholderList.isEmpty()) {
			throw STAKEHOLDER_NOT_FOUND_PROBLEM;
		}
		return stakeholderList.stream().map(EntityMapper::toStakeholderDto).toList();
	}

	public List<StakeholderDTO> findStakeholdersByRole(final StakeholderRole stakeholderRole) {
		final List<Stakeholder> stakeholderList = stakeholderRepository.findByRoles(stakeholderRole);

		if (stakeholderList.isEmpty()) {
			throw STAKEHOLDER_NOT_FOUND_PROBLEM;
		}

		return stakeholderList.stream().map(EntityMapper::toStakeholderDto).toList();
	}

	public StakeholderDTO findById(final Long id) {
		return toStakeholderDto(stakeholderRepository.findById(id).orElseThrow(() -> STAKEHOLDER_NOT_FOUND_PROBLEM));
	}

	@Retry(name = "OptimisticLocking")
	public void patch(final Long stakeholderId, final StakeholderDTO stakeholderDTO) {
		final var entity = stakeholderRepository.findById(stakeholderId)
			.orElseThrow(() -> STAKEHOLDER_NOT_FOUND_PROBLEM);
		patchStakeholder(entity, stakeholderDTO);
		stakeholderRepository.save(entity);
	}

	@Retry(name = "OptimisticLocking")
	public void put(final Long id, final StakeholderDTO dto) {
		final var entity = stakeholderRepository.findById(id)
			.orElseThrow(() -> STAKEHOLDER_NOT_FOUND_PROBLEM);
		putStakeholder(entity, dto);
		stakeholderRepository.save(entity);
	}

}
