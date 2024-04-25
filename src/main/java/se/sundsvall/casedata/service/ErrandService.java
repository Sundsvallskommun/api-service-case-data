package se.sundsvall.casedata.service;

import static java.util.Objects.isNull;
import static org.zalando.problem.Status.NOT_FOUND;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toAppeal;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toAppealDto;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toDecision;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toDecisionDto;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toErrand;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toErrandDto;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toNote;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toNoteDto;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toStakeholder;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toStakeholderDto;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toStatus;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;
import org.zalando.problem.ThrowableProblem;

import io.github.resilience4j.retry.annotation.Retry;
import se.sundsvall.casedata.api.model.AppealDTO;
import se.sundsvall.casedata.api.model.DecisionDTO;
import se.sundsvall.casedata.api.model.ErrandDTO;
import se.sundsvall.casedata.api.model.FacilityDTO;
import se.sundsvall.casedata.api.model.NoteDTO;
import se.sundsvall.casedata.api.model.PatchErrandDTO;
import se.sundsvall.casedata.api.model.StakeholderDTO;
import se.sundsvall.casedata.api.model.StatusDTO;
import se.sundsvall.casedata.integration.db.AppealRepository;
import se.sundsvall.casedata.integration.db.DecisionRepository;
import se.sundsvall.casedata.integration.db.ErrandRepository;
import se.sundsvall.casedata.integration.db.NoteRepository;
import se.sundsvall.casedata.integration.db.StakeholderRepository;
import se.sundsvall.casedata.integration.db.model.Appeal;
import se.sundsvall.casedata.integration.db.model.Decision;
import se.sundsvall.casedata.integration.db.model.Errand;
import se.sundsvall.casedata.integration.db.model.Note;
import se.sundsvall.casedata.service.util.mappers.EntityMapper;
import se.sundsvall.casedata.service.util.mappers.PatchMapper;

@Service
public class ErrandService {

	private static final String ERRAND_WAS_NOT_FOUND = "Errand with id: {0} was not found";
	private static final String DECISION_WAS_NOT_FOUND_ON_ERRAND_WITH_ID = "Decision was not found on errand with id: {0}";
	private static final String DECISION_WITH_ID_X_WAS_NOT_FOUND_ON_ERRAND_WITH_ID_X = "Decision with id: {0} was not found on errand with id: {1}";
	private static final String APPEAL_WITH_ID_X_WAS_NOT_FOUND_ON_ERRAND_WITH_ID_X = "Appeal with id: {0} was not found on errand with id: {1}";
	private static final String NOTE_WITH_ID_X_WAS_NOT_FOUND_ON_ERRAND_WITH_ID_X = "Note with id: {0} was not found on errand with id: {1}";
	private static final String STAKEHOLDER_WITH_ID_X_WAS_NOT_FOUND_ON_ERRAND_WITH_ID_X = "Stakeholder with id: {0} was not found on errand with id: {1}";
	private static final ThrowableProblem ERRAND_NOT_FOUND_PROBLEM = Problem.valueOf(NOT_FOUND, ERRAND_WAS_NOT_FOUND);

	private final ErrandRepository errandRepository;
	private final AppealRepository appealRepository;
	private final NoteRepository noteRepository;
	private final StakeholderRepository stakeholderRepository;
	private final DecisionRepository decisionRepository;
	private final ProcessService processService;

	public ErrandService(final ErrandRepository errandRepository, final AppealRepository appealRepository,
		final NoteRepository noteRepository, final StakeholderRepository stakeholderRepository,
		final DecisionRepository decisionRepository, final ProcessService processService) {
		this.errandRepository = errandRepository;
		this.appealRepository = appealRepository;
		this.noteRepository = noteRepository;
		this.decisionRepository = decisionRepository;
		this.stakeholderRepository = stakeholderRepository;

		this.processService = processService;
	}

	//////////////////////////////
	// GET operations
	//////////////////////////////

	public List<DecisionDTO> findDecisionsOnErrand(final Long id) {
		final List<Decision> decisionList = getErrand(id).getDecisions();
		if ((decisionList == null) || decisionList.isEmpty()) {
			throw Problem.valueOf(NOT_FOUND, MessageFormat.format(DECISION_WAS_NOT_FOUND_ON_ERRAND_WITH_ID, id));
		}
		return decisionList.stream().map(EntityMapper::toDecisionDto).toList();
	}

	public List<FacilityDTO> findFacilitiesOnErrand(final Long id) {

		// TODO: Implement this
		return List.of(new FacilityDTO());
	}

	public FacilityDTO findFacilityOnErrand(final Long id, Long facilityId) {

		// TODO: Implement this
		return new FacilityDTO();
	}

	public ErrandDTO findById(final Long id) {
		return toErrandDto(errandRepository.findById(id)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND,
				MessageFormat.format(ERRAND_WAS_NOT_FOUND, id))));
	}

	public Errand getErrand(final Long errandId) {
		return errandRepository.findById(errandId)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND,
				MessageFormat.format(ERRAND_WAS_NOT_FOUND, errandId)));
	}

	public void deleteById(final Long errandId) {
		if (!errandRepository.existsById(errandId)) {
			throw Problem.valueOf(NOT_FOUND, MessageFormat.format(ERRAND_WAS_NOT_FOUND, errandId));
		}

		errandRepository.deleteById(errandId);
	}

	/**
	 * @return Page of ErrandDTO without duplicates
	 */
	public Page<ErrandDTO> findAll(final Specification<Errand> specification, final Map<String, String> extraParameters, final Pageable pageable) {
		// Extract all ID's and remove duplicates
		final List<Long> allIds = errandRepository.findAll(specification).stream()
			.filter(errandDTO -> hashmapContainsAllKeyAndValues(errandDTO.getExtraParameters(), extraParameters))
			.map(Errand::getId)
			.collect(Collectors.toSet())
			.stream().toList();

		// Get errands without duplicates
		final Page<ErrandDTO> errandDTOPage = errandRepository.findAllByIdIn(allIds, pageable)
			.map(EntityMapper::toErrandDto);

		if (errandDTOPage.isEmpty()) {
			throw ERRAND_NOT_FOUND_PROBLEM;
		}
		return errandDTOPage;
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
	public ErrandDTO createErrand(final ErrandDTO errandDTO) {
		final var errand = toErrand(errandDTO);
		final var resultErrand = errandRepository.save(errand);

		// Will not start a process if it's not a parking permit or mex errand
		startProcess(resultErrand);

		return toErrandDto(resultErrand);
	}

	@Retry(name = "OptimisticLocking")
	public FacilityDTO createFacility(final Long id, final FacilityDTO facilityDTO) {
		// TODO: Implement this
		return new FacilityDTO();
	}

	//////////////////////////////
	// DELETE operations
	//////////////////////////////

	@Retry(name = "OptimisticLocking")
	public void deleteStakeholderOnErrand(final Long errandId, final Long stakeholderId) {
		final Errand errand = getErrand(errandId);
		final var stakeholderToRemove = errand.getStakeholders().stream()
			.filter(stakeholder -> stakeholder.getId().equals(stakeholderId))
			.findFirst().orElseThrow(() -> Problem.valueOf(NOT_FOUND, MessageFormat.format(STAKEHOLDER_WITH_ID_X_WAS_NOT_FOUND_ON_ERRAND_WITH_ID_X, stakeholderId, errandId)));
		stakeholderRepository.delete(stakeholderToRemove);
		processService.updateProcess(errand);
	}

	@Retry(name = "OptimisticLocking")
	public void deleteDecisionOnErrand(final Long errandId, final Long decisionId) {
		final Errand errand = getErrand(errandId);
		final Decision decisionToRemove = errand.getDecisions().stream()
			.filter(decision -> decision.getId().equals(decisionId))
			.findAny()
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, MessageFormat.format(DECISION_WITH_ID_X_WAS_NOT_FOUND_ON_ERRAND_WITH_ID_X, decisionId, errandId)));
		decisionRepository.delete(decisionToRemove);
		processService.updateProcess(errand);
	}

	@Retry(name = "OptimisticLocking")
	public void deleteNoteOnErrand(final Long errandId, final Long noteId) {
		final Errand errand = getErrand(errandId);
		final Note noteToRemove = errand.getNotes().stream()
			.filter(note -> note.getId().equals(noteId))
			.findAny()
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, MessageFormat.format(NOTE_WITH_ID_X_WAS_NOT_FOUND_ON_ERRAND_WITH_ID_X, noteId, errandId)));
		noteRepository.delete(noteToRemove);
		processService.updateProcess(errand);
	}

	@Retry(name = "OptimisticLocking")
	public void deleteAppealOnErrand(final Long errandId, final Long appealId) {
		final Errand errand = getErrand(errandId);
		final Appeal appealToRemove = errand.getAppeals().stream()
			.filter(appeal -> appeal.getId().equals(appealId))
			.findAny()
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, MessageFormat.format(APPEAL_WITH_ID_X_WAS_NOT_FOUND_ON_ERRAND_WITH_ID_X, appealId, errandId)));
		appealRepository.delete(appealToRemove);
		processService.updateProcess(errand);
	}

	@Retry(name = "OptimisticLocking")
	public void deleteFacilityOnErrand(final Long errandId, final Long facilityId) {
		// TODO: Implement this.
	}

	//////////////////////////////
	// PATCH operations
	//////////////////////////////

	@Retry(name = "OptimisticLocking")
	public void updateErrand(final Long id, final PatchErrandDTO patchErrandDTO) {
		final var oldErrand = getErrand(id);
		final var updatedErrand = PatchMapper.patchErrand(oldErrand, patchErrandDTO);
		errandRepository.save(updatedErrand);
		processService.updateProcess(updatedErrand);
	}

	@Retry(name = "OptimisticLocking")
	public void addStatusToErrand(final Long id, final StatusDTO statusDTO) {
		final var oldErrand = getErrand(id);
		final var status = toStatus(statusDTO);
		oldErrand.getStatuses().add(status);
		final var updatedErrand = errandRepository.save(oldErrand);
		processService.updateProcess(updatedErrand);
	}

	@Retry(name = "OptimisticLocking")
	public StakeholderDTO addStakeholderToErrand(final Long id, final StakeholderDTO stakeholderDTO) {
		final var oldErrand = getErrand(id);
		final var stakeholder = toStakeholder(stakeholderDTO);
		stakeholder.setErrand(oldErrand);
		oldErrand.getStakeholders().add(stakeholder);
		final var updatedErrand = errandRepository.save(oldErrand);
		processService.updateProcess(updatedErrand);
		return toStakeholderDto(stakeholder);
	}

	@Retry(name = "OptimisticLocking")
	public NoteDTO addNoteToErrand(final Long id, final NoteDTO noteDTO) {
		final var oldErrand = getErrand(id);
		final var note = toNote(noteDTO);
		note.setErrand(oldErrand);
		oldErrand.getNotes().add(note);
		final var updatedErrand = errandRepository.save(oldErrand);
		processService.updateProcess(updatedErrand);
		return toNoteDto(note);
	}

	@Retry(name = "OptimisticLocking")
	public DecisionDTO addDecisionToErrand(final Long id, final DecisionDTO decisionDTO) {
		final var oldErrand = getErrand(id);
		final var decision = toDecision(decisionDTO);
		decision.setErrand(oldErrand);
		oldErrand.getDecisions().add(decision);
		final var updatedErrand = errandRepository.save(oldErrand);
		processService.updateProcess(updatedErrand);
		return toDecisionDto(decision);
	}

	@Retry(name = "OptimisticLocking")
	public AppealDTO addAppealToErrand(final Long id, final AppealDTO appealDTO) {
		final var oldErrand = getErrand(id);
		final var appeal = toAppeal(appealDTO);
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
	public FacilityDTO updateFacilityOnErrand(final Long id, Long facilityId, final FacilityDTO facilityDTO) {
		// TODO: Implement this.
		return new FacilityDTO();
	}

	//////////////////////////////
	// PUT operations
	//////////////////////////////

	@Retry(name = "OptimisticLocking")
	public void replaceStatusesOnErrand(final Long id, final List<StatusDTO> dtos) {
		final var oldErrand = getErrand(id);
		oldErrand.setStatuses(new ArrayList<>(dtos.stream().map(EntityMapper::toStatus).toList()));
		final var updatedErrand = errandRepository.save(oldErrand);
		processService.updateProcess(updatedErrand);
	}

	@Retry(name = "OptimisticLocking")
	public void replaceStakeholdersOnErrand(final Long id, final List<StakeholderDTO> dtos) {
		final var oldErrand = getErrand(id);
		oldErrand.setStakeholders(new ArrayList<>(dtos.stream().map(EntityMapper::toStakeholder).toList()));
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
