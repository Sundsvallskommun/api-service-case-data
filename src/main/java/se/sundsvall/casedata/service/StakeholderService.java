package se.sundsvall.casedata.service;

import static org.zalando.problem.Status.NOT_FOUND;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toStakeholderDto;
import static se.sundsvall.casedata.service.util.mappers.PatchMapper.patchStakeholder;
import static se.sundsvall.casedata.service.util.mappers.PutMapper.putStakeholder;

import java.util.List;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;

import se.sundsvall.casedata.api.model.StakeholderDTO;
import se.sundsvall.casedata.integration.db.StakeholderRepository;
import se.sundsvall.casedata.integration.db.model.Stakeholder;
import se.sundsvall.casedata.service.util.mappers.EntityMapper;

import io.github.resilience4j.retry.annotation.Retry;

@Service
@Transactional
public class StakeholderService {

	private static final String STAKEHOLDER_NOT_FOUND = "Stakeholder not found";

	private final StakeholderRepository stakeholderRepository;

	public StakeholderService(final StakeholderRepository stakeholderRepository) {
		this.stakeholderRepository = stakeholderRepository;
	}

	public List<StakeholderDTO> findAllStakeholdersByMunicipalityId(final String municipalityId, final String namespace) {
		final List<Stakeholder> stakeholderList = stakeholderRepository.findAllByMunicipalityIdAndNamespace(municipalityId, namespace);
		if (stakeholderList.isEmpty()) {
			throw Problem.valueOf(NOT_FOUND, STAKEHOLDER_NOT_FOUND);
		}
		return stakeholderList.stream().map(EntityMapper::toStakeholderDto).toList();
	}

	public List<StakeholderDTO> findStakeholdersByRoleAndMunicipalityId(final String stakeholderRole, final String municipalityId, final String namespace) {
		final List<Stakeholder> stakeholderList = stakeholderRepository.findByRolesAndMunicipalityIdAndNamespace(stakeholderRole, municipalityId, namespace);

		if (stakeholderList.isEmpty()) {
			throw Problem.valueOf(NOT_FOUND, STAKEHOLDER_NOT_FOUND);
		}

		return stakeholderList.stream().map(EntityMapper::toStakeholderDto).toList();
	}

	public StakeholderDTO findByIdAndMunicipalityId(final Long id, final String municipalityId, final String namespace) {
		return toStakeholderDto(stakeholderRepository.findByIdAndMunicipalityIdAndNamespace(id, municipalityId, namespace)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, STAKEHOLDER_NOT_FOUND)));
	}

	@Retry(name = "OptimisticLocking")
	public void patch(final Long stakeholderId, final String municipalityId, final String namespace, final StakeholderDTO stakeholderDTO) {
		final var entity = stakeholderRepository.findByIdAndMunicipalityIdAndNamespace(stakeholderId, municipalityId, namespace)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, STAKEHOLDER_NOT_FOUND));
		patchStakeholder(entity, stakeholderDTO);
		stakeholderRepository.save(entity);
	}

	@Retry(name = "OptimisticLocking")
	public void put(final Long id, final String municipalityId, final String namespace, final StakeholderDTO dto) {
		final var entity = stakeholderRepository.findByIdAndMunicipalityIdAndNamespace(id, municipalityId, namespace)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, STAKEHOLDER_NOT_FOUND));
		putStakeholder(entity, dto);
		stakeholderRepository.save(entity);
	}

}
