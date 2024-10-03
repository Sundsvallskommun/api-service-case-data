package se.sundsvall.casedata.service;

import static java.text.MessageFormat.format;
import static org.zalando.problem.Status.NOT_FOUND;
import static se.sundsvall.casedata.service.util.Constants.ERRAND_WAS_NOT_FOUND;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toStakeholder;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toStakeholderDto;
import static se.sundsvall.casedata.service.util.mappers.PatchMapper.patchStakeholder;
import static se.sundsvall.casedata.service.util.mappers.PutMapper.putStakeholder;

import java.util.List;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;

import se.sundsvall.casedata.api.model.StakeholderDTO;
import se.sundsvall.casedata.integration.db.ErrandRepository;
import se.sundsvall.casedata.integration.db.StakeholderRepository;
import se.sundsvall.casedata.integration.db.model.Errand;
import se.sundsvall.casedata.integration.db.model.Stakeholder;
import se.sundsvall.casedata.service.util.mappers.EntityMapper;

import io.github.resilience4j.retry.annotation.Retry;

@Service
@Transactional
public class StakeholderService {

	private static final String STAKEHOLDER_WITH_ID_X_WAS_NOT_FOUND_ON_ERRAND_WITH_ID_X = "Stakeholder with id: {0} was not found on errand with id: {1}";

	private static final String STAKEHOLDER_NOT_FOUND = "Stakeholder not found";

	private final StakeholderRepository stakeholderRepository;

	private final ErrandRepository errandRepository;

	private final ProcessService processService;

	public StakeholderService(final StakeholderRepository stakeholderRepository, final ErrandRepository errandRepository, final ProcessService processService) {
		this.stakeholderRepository = stakeholderRepository;
		this.errandRepository = errandRepository;
		this.processService = processService;
	}

	public List<StakeholderDTO> findAllStakeholdersByMunicipalityIdAndNamespace(final String municipalityId, final String namespace) {
		final List<Stakeholder> stakeholderList = stakeholderRepository.findAllByMunicipalityIdAndNamespace(municipalityId, namespace);
		if (stakeholderList.isEmpty()) {
			throw Problem.valueOf(NOT_FOUND, STAKEHOLDER_NOT_FOUND);
		}
		return stakeholderList.stream().map(EntityMapper::toStakeholderDto).toList();
	}

	public List<StakeholderDTO> findStakeholdersByRoleAndMunicipalityIdAndNamespace(final String stakeholderRole, final String municipalityId, final String namespace) {
		final List<Stakeholder> stakeholderList = stakeholderRepository.findByRolesAndMunicipalityIdAndNamespace(stakeholderRole, municipalityId, namespace);

		if (stakeholderList.isEmpty()) {
			throw Problem.valueOf(NOT_FOUND, STAKEHOLDER_NOT_FOUND);
		}

		return stakeholderList.stream().map(EntityMapper::toStakeholderDto).toList();
	}

	public StakeholderDTO findByIdAndMunicipalityIdAndNamespace(final Long id, final String municipalityId, final String namespace) {
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


	@Retry(name = "OptimisticLocking")
	public void deleteStakeholderOnErrand(final Long errandId, final String municipalityId, final String namespace, final Long stakeholderId) {
		final var errand = getErrandByIdAndMunicipalityIdAndNamespace(errandId, municipalityId, namespace);
		final var stakeholderToRemove = errand.getStakeholders().stream()
			.filter(stakeholder -> stakeholder.getId().equals(stakeholderId))
			.findFirst().orElseThrow(() -> Problem.valueOf(NOT_FOUND, format(STAKEHOLDER_WITH_ID_X_WAS_NOT_FOUND_ON_ERRAND_WITH_ID_X, stakeholderId, errandId)));
		errand.getStakeholders().remove(stakeholderToRemove);
		errandRepository.save(errand);
		processService.updateProcess(errand);
	}

	@Retry(name = "OptimisticLocking")
	public StakeholderDTO addStakeholderToErrand(final Long errandId, final String municipalityId, final String namespace, final StakeholderDTO stakeholderDTO) {
		final var oldErrand = getErrandByIdAndMunicipalityIdAndNamespace(errandId, municipalityId, namespace);
		final var stakeholder = toStakeholder(stakeholderDTO, municipalityId, namespace);
		stakeholder.setErrand(oldErrand);
		oldErrand.getStakeholders().add(stakeholder);
		final var updatedErrand = errandRepository.save(oldErrand);
		processService.updateProcess(updatedErrand);
		return toStakeholderDto(stakeholder);
	}

	@Retry(name = "OptimisticLocking")
	public void replaceStakeholdersOnErrand(final Long errandId, final String municipalityId, final String namespace, final List<StakeholderDTO> dtos) {
		final var oldErrand = getErrandByIdAndMunicipalityIdAndNamespace(errandId, municipalityId, namespace);
		oldErrand.getStakeholders().clear();
		oldErrand.getStakeholders().addAll(dtos.stream().map(stakeholderDTO -> toStakeholder(stakeholderDTO, municipalityId, namespace)).toList());
		oldErrand.getStakeholders().forEach(stakeholder -> stakeholder.setErrand(oldErrand));
		final var updatedErrand = errandRepository.save(oldErrand);
		processService.updateProcess(updatedErrand);
	}

	public Errand getErrandByIdAndMunicipalityIdAndNamespace(final Long errandId, final String municipalityId, final String namespace) {
		return errandRepository.findByIdAndMunicipalityIdAndNamespace(errandId, municipalityId, namespace)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND,
				format(ERRAND_WAS_NOT_FOUND, errandId)));
	}


}
