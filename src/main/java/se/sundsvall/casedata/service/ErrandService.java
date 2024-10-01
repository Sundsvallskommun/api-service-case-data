package se.sundsvall.casedata.service;

import static java.text.MessageFormat.format;
import static java.util.Objects.isNull;
import static org.zalando.problem.Status.NOT_FOUND;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toAppeal;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toAppealDto;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toDecision;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toDecisionDto;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toErrand;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toErrandDto;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toFacility;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toFacilityDto;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toNote;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toNoteDto;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toStakeholder;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toStakeholderDto;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toStatus;
import static se.sundsvall.casedata.service.util.mappers.PatchMapper.patchFacility;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import jakarta.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;

import se.sundsvall.casedata.api.model.AppealDTO;
import se.sundsvall.casedata.api.model.DecisionDTO;
import se.sundsvall.casedata.api.model.ErrandDTO;
import se.sundsvall.casedata.api.model.FacilityDTO;
import se.sundsvall.casedata.api.model.NoteDTO;
import se.sundsvall.casedata.api.model.PatchErrandDTO;
import se.sundsvall.casedata.api.model.StakeholderDTO;
import se.sundsvall.casedata.api.model.StatusDTO;
import se.sundsvall.casedata.integration.db.ErrandRepository;
import se.sundsvall.casedata.integration.db.FacilityRepository;
import se.sundsvall.casedata.integration.db.model.Decision;
import se.sundsvall.casedata.integration.db.model.Errand;
import se.sundsvall.casedata.integration.db.model.Facility;
import se.sundsvall.casedata.service.util.mappers.EntityMapper;
import se.sundsvall.casedata.service.util.mappers.PatchMapper;
import se.sundsvall.casedata.service.util.mappers.PutMapper;

import io.github.resilience4j.retry.annotation.Retry;

@Service
public class ErrandService {

	private static final String ERRAND_WAS_NOT_FOUND = "Errand with id: {0} was not found";

	private static final String DECISION_WAS_NOT_FOUND_ON_ERRAND_WITH_ID = "Decision was not found on errand with id: {0}";

	private static final String DECISION_WITH_ID_X_WAS_NOT_FOUND_ON_ERRAND_WITH_ID_X = "Decision with id: {0} was not found on errand with id: {1}";

	private static final String FACILITY_WITH_ID_X_WAS_NOT_FOUND_ON_ERRAND_WITH_ID_X = "Facility with id: {0} was not found on errand with id: {1}";

	private static final String APPEAL_WITH_ID_X_WAS_NOT_FOUND_ON_ERRAND_WITH_ID_X = "Appeal with id: {0} was not found on errand with id: {1}";

	private static final String NOTE_WITH_ID_X_WAS_NOT_FOUND_ON_ERRAND_WITH_ID_X = "Note with id: {0} was not found on errand with id: {1}";

	private static final String STAKEHOLDER_WITH_ID_X_WAS_NOT_FOUND_ON_ERRAND_WITH_ID_X = "Stakeholder with id: {0} was not found on errand with id: {1}";

	private final ErrandRepository errandRepository;

	private final FacilityRepository facilityRepository;

	private final ProcessService processService;

	public ErrandService(final ErrandRepository errandRepository, final FacilityRepository facilityRepository, final ProcessService processService) {
		this.errandRepository = errandRepository;
		this.facilityRepository = facilityRepository;
		this.processService = processService;
	}

	////////////////////////////// f
	// GET operations
	//////////////////////////////

	public List<DecisionDTO> findDecisionsOnErrand(final Long errandId, final String municipalityId, final String namespace) {
		final List<Decision> decisionList = getErrandByIdAndMunicipalityIdAndNamespace(errandId, municipalityId, namespace).getDecisions();
		if ((decisionList == null) || decisionList.isEmpty()) {
			throw Problem.valueOf(NOT_FOUND, format(DECISION_WAS_NOT_FOUND_ON_ERRAND_WITH_ID, errandId));
		}
		return decisionList.stream().map(EntityMapper::toDecisionDto).toList();
	}

	public List<FacilityDTO> findFacilitiesOnErrand(final Long errandId, final String municipalityId, final String namespace) {
		return getErrandByIdAndMunicipalityIdAndNamespace(errandId, municipalityId, namespace).getFacilities().stream()
			.map(EntityMapper::toFacilityDto)
			.toList();
	}

	public FacilityDTO findFacilityOnErrand(final Long errandId, final Long facilityId, final String municipalityId, final String namespace) {
		return toFacilityDto(facilityRepository.findByIdAndErrandIdAndMunicipalityIdAndNamespace(facilityId, errandId, municipalityId, namespace)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, format(FACILITY_WITH_ID_X_WAS_NOT_FOUND_ON_ERRAND_WITH_ID_X, facilityId, errandId))));
	}

	public ErrandDTO findByIdAndMunicipalityIdAndNamespace(final Long errandId, final String municipalityId, final String namespace) {
		return toErrandDto(errandRepository.findByIdAndMunicipalityIdAndNamespace(errandId, municipalityId, namespace)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND,
				format(ERRAND_WAS_NOT_FOUND, errandId))));
	}

	public Errand getErrandByIdAndMunicipalityIdAndNamespace(final Long errandId, final String municipalityId, final String namespace) {
		return errandRepository.findByIdAndMunicipalityIdAndNamespace(errandId, municipalityId, namespace)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND,
				format(ERRAND_WAS_NOT_FOUND, errandId)));
	}

	@Transactional
	public void deleteByIdAndMunicipalityIdAndNamespace(final Long errandId, final String municipalityId, final String namespace) {
		if (!errandRepository.existsByIdAndMunicipalityIdAndNamespace(errandId, municipalityId, namespace)) {
			throw Problem.valueOf(NOT_FOUND, format(ERRAND_WAS_NOT_FOUND, errandId));
		}

		errandRepository.deleteByIdAndMunicipalityIdAndNamespace(errandId, municipalityId, namespace);
	}

	/**
	 * @return Page of ErrandDTO without duplicates
	 */
	public Page<ErrandDTO> findAll(final Specification<Errand> specification, final String municipalityId, final String namespace, final Map<String, String> extraParameters, final Pageable pageable) {
		// Extract all ID's and remove duplicates
		final List<Long> allIds = errandRepository.findAll(specification).stream()
			.filter(errand -> municipalityId.equals(errand.getMunicipalityId()))
			.filter(errand -> hashmapContainsAllKeyAndValues(errand.getExtraParameters(), extraParameters))
			.map(Errand::getId)
			.distinct()
			.toList();

		return errandRepository.findAllByIdInAndMunicipalityIdAndNamespace(allIds, municipalityId, namespace, pageable)
			.map(EntityMapper::toErrandDto);
	}

	private boolean hashmapContainsAllKeyAndValues(final Map<String, String> map, final Map<String, String> mapToCheck) {
		for (final Map.Entry<String, String> entry : mapToCheck.entrySet()) {
			final String mapValue = map.get(entry.getKey());
			if (!entry.getValue().equals(mapValue)) {
				return false;
			}
		}
		return true;
	}

	//////////////////////////////
	// POST operations
	//////////////////////////////

	/**
	 * Saves errand and update the process in ParkingPermit if it's a parking permit errand
	 */
	public ErrandDTO createErrand(final ErrandDTO errandDTO, final String municipalityId, final String namespace) {
		final var errand = toErrand(errandDTO, municipalityId, namespace);
		final var resultErrand = errandRepository.save(errand);

		// Will not start a process if it's not a parking permit or mex errand
		startProcess(resultErrand);

		return toErrandDto(resultErrand);
	}

	@Retry(name = "OptimisticLocking")
	public FacilityDTO createFacility(final Long errandId, final String municipalityId, final String namespace, final FacilityDTO facilityDTO) {
		final var errand = getErrandByIdAndMunicipalityIdAndNamespace(errandId, municipalityId, namespace);
		final var facility = toFacility(facilityDTO, municipalityId, namespace);
		facility.setErrand(errand);

		final var facilityDto = toFacilityDto(facilityRepository.save(facility));

		processService.updateProcess(errand);

		return facilityDto;
	}

	//////////////////////////////
	// DELETE operations
	//////////////////////////////

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

	@Retry(name = "OptimisticLocking")
	public void deleteNoteOnErrand(final Long errandId, final String municipalityId, final String namespace, final Long noteId) {
		final var errand = getErrandByIdAndMunicipalityIdAndNamespace(errandId, municipalityId, namespace);
		final var noteToRemove = errand.getNotes().stream()
			.filter(note -> note.getId().equals(noteId))
			.findAny()
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, format(NOTE_WITH_ID_X_WAS_NOT_FOUND_ON_ERRAND_WITH_ID_X, noteId, errandId)));
		errand.getNotes().remove(noteToRemove);
		errandRepository.save(errand);
		processService.updateProcess(errand);
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
	public void deleteFacilityOnErrand(final Long errandId, final String municipalityId, final String namespace, final Long facilityId) {
		final var errand = getErrandByIdAndMunicipalityIdAndNamespace(errandId, municipalityId, namespace);
		final var facilityToRemove = errand.getFacilities().stream()
			.filter(facility -> facility.getId().equals(facilityId))
			.findAny()
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, format(FACILITY_WITH_ID_X_WAS_NOT_FOUND_ON_ERRAND_WITH_ID_X, facilityId, errandId)));

		errand.getFacilities().remove(facilityToRemove);
		errandRepository.save(errand);
		processService.updateProcess(errand);
	}

	//////////////////////////////
	// PATCH operations
	//////////////////////////////

	@Retry(name = "OptimisticLocking")
	public void updateErrand(final Long errandId, final String municipalityId, final String namespace, final PatchErrandDTO patchErrandDTO) {
		final var oldErrand = getErrandByIdAndMunicipalityIdAndNamespace(errandId, municipalityId, namespace);
		final var updatedErrand = PatchMapper.patchErrand(oldErrand, patchErrandDTO);
		errandRepository.save(updatedErrand);
		processService.updateProcess(updatedErrand);
	}

	@Retry(name = "OptimisticLocking")
	public void addStatusToErrand(final Long errandId, final String municipalityId, final String namespace, final StatusDTO statusDTO) {
		final var oldErrand = getErrandByIdAndMunicipalityIdAndNamespace(errandId, municipalityId, namespace);
		final var status = toStatus(statusDTO);
		oldErrand.getStatuses().add(status);
		final var updatedErrand = errandRepository.save(oldErrand);
		processService.updateProcess(updatedErrand);
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
	public NoteDTO addNoteToErrand(final Long errandId, final String municipalityId, final String namespace, final NoteDTO noteDTO) {
		final var oldErrand = getErrandByIdAndMunicipalityIdAndNamespace(errandId, municipalityId, namespace);
		final var note = toNote(noteDTO, municipalityId, namespace);
		note.setErrand(oldErrand);
		oldErrand.getNotes().add(note);
		final var updatedErrand = errandRepository.save(oldErrand);
		processService.updateProcess(updatedErrand);
		return toNoteDto(note);
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

	@Retry(name = "OptimisticLocking")
	public FacilityDTO updateFacilityOnErrand(final Long errandId, final String municipalityId, final String namespace, final Long facilityId, final FacilityDTO facilityDTO) {
		final var errand = getErrandByIdAndMunicipalityIdAndNamespace(errandId, municipalityId, namespace);
		final var facility = facilityRepository.findByIdAndErrandIdAndMunicipalityIdAndNamespace(facilityId, errandId, municipalityId, namespace)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, format(FACILITY_WITH_ID_X_WAS_NOT_FOUND_ON_ERRAND_WITH_ID_X, facilityId, errandId)));

		final var updatedFacility = patchFacility(facility, facilityDTO);
		final var result = toFacilityDto(facilityRepository.save(updatedFacility));
		processService.updateProcess(errand);

		return result;
	}

	//////////////////////////////
	// PUT operations
	//////////////////////////////

	@Retry(name = "OptimisticLocking")
	public void replaceStatusesOnErrand(final Long errandId, final String municipalityId, final String namespace, final List<StatusDTO> dtos) {
		final var oldErrand = getErrandByIdAndMunicipalityIdAndNamespace(errandId, municipalityId, namespace);
		oldErrand.setStatuses(new ArrayList<>(dtos.stream().map(EntityMapper::toStatus).toList()));
		final var updatedErrand = errandRepository.save(oldErrand);
		processService.updateProcess(updatedErrand);
	}

	@Retry(name = "OptimisticLocking")
	public void replaceFacilitiesOnErrand(final Long errandId, final String municipalityId, final String namespace, final List<FacilityDTO> dtos) {
		final var oldErrand = getErrandByIdAndMunicipalityIdAndNamespace(errandId, municipalityId, namespace);
		final var facilitiesToChange = oldErrand.getFacilities().stream().filter(facility -> dtos.stream().map(FacilityDTO::getId).toList().contains(facility.getId())).toList();
		final var newFacilities = dtos.stream()
			.filter(dto -> !facilitiesToChange.stream()
				.map(Facility::getId)
				.toList().contains(dto.getId()))
			.map(facilityDTO -> toFacility(facilityDTO, municipalityId, namespace))
			.toList();

		oldErrand.getFacilities().clear();

		oldErrand.getFacilities().addAll(dtos.stream()
			.filter(dto -> facilitiesToChange.stream()
				.map(Facility::getId).toList().contains(dto.getId()))
			.map(dto -> PutMapper.putFacility(facilitiesToChange.stream().
				filter(facility -> facility.getId().equals(dto.getId())).findFirst().orElse(null), dto)).toList());

		oldErrand.getFacilities().addAll(newFacilities.stream().map(facility -> {
			facility.setErrand(oldErrand);
			return facility;
		}).toList());

		final var updatedErrand = errandRepository.save(oldErrand);
		processService.updateProcess(updatedErrand);
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

	private void startProcess(final Errand errand) {
		try {
			final var startProcessResponse = processService.startProcess(errand);
			if (!isNull(startProcessResponse)) {
				errand.setProcessId(startProcessResponse.getProcessId());
				errandRepository.save(errand);
			}
		} catch (final Exception e) {
			errandRepository.delete(errand);
			throw e;
		}
	}

}
