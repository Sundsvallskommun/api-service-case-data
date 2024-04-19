package se.sundsvall.casedata.service;

import io.github.resilience4j.retry.annotation.Retry;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import se.sundsvall.casedata.api.model.StakeholderDTO;
import se.sundsvall.casedata.integration.db.StakeholderRepository;
import se.sundsvall.casedata.integration.db.model.Stakeholder;
import se.sundsvall.casedata.service.util.mappers.EntityMapper;

import java.util.List;

import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toStakeholderDto;
import static se.sundsvall.casedata.service.util.mappers.PatchMapper.patchStakeholder;
import static se.sundsvall.casedata.service.util.mappers.PutMapper.putStakeholder;

@Service
@Transactional
public class StakeholderService {

	private static final String STAKEHOLDER_NOT_FOUND = "Stakeholder not found";

	private final StakeholderRepository stakeholderRepository;

	public StakeholderService(final StakeholderRepository stakeholderRepository) {
		this.stakeholderRepository = stakeholderRepository;
	}

	public List<StakeholderDTO> findAllStakeholders() {
		final List<Stakeholder> stakeholderList = stakeholderRepository.findAll();
		if (stakeholderList.isEmpty()) {
			throw Problem.valueOf(Status.NOT_FOUND, STAKEHOLDER_NOT_FOUND);
		}
		return stakeholderList.stream().map(EntityMapper::toStakeholderDto).toList();
	}

	public List<StakeholderDTO> findStakeholdersByRole(final String stakeholderRole) {
		final List<Stakeholder> stakeholderList = stakeholderRepository.findByRoles(stakeholderRole);

		if (stakeholderList.isEmpty()) {
			throw Problem.valueOf(Status.NOT_FOUND, STAKEHOLDER_NOT_FOUND);
		}

		return stakeholderList.stream().map(EntityMapper::toStakeholderDto).toList();
	}

	public StakeholderDTO findById(final Long id) {
		return toStakeholderDto(stakeholderRepository.findById(id).orElseThrow(() -> Problem.valueOf(Status.NOT_FOUND, STAKEHOLDER_NOT_FOUND)));
	}

	@Retry(name = "OptimisticLocking")
	public void patch(final Long stakeholderId, final StakeholderDTO stakeholderDTO) {
		final var entity = stakeholderRepository.findById(stakeholderId)
			.orElseThrow(() -> Problem.valueOf(Status.NOT_FOUND, STAKEHOLDER_NOT_FOUND));
		patchStakeholder(entity, stakeholderDTO);
		stakeholderRepository.save(entity);
	}

	@Retry(name = "OptimisticLocking")
	public void put(final Long id, final StakeholderDTO dto) {
		final var entity = stakeholderRepository.findById(id)
			.orElseThrow(() -> Problem.valueOf(Status.NOT_FOUND, STAKEHOLDER_NOT_FOUND));
		putStakeholder(entity, dto);
		stakeholderRepository.save(entity);
	}

}
