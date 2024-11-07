package se.sundsvall.casedata.service;

import static java.text.MessageFormat.format;
import static org.javers.repository.jql.QueryBuilder.byInstance;
import static org.zalando.problem.Status.NOT_FOUND;
import static se.sundsvall.casedata.service.util.Constants.ERRAND_WAS_NOT_FOUND;

import java.util.List;

import org.javers.core.Changes;
import org.javers.core.Javers;
import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;
import org.zalando.problem.ThrowableProblem;

import com.google.gson.reflect.TypeToken;

import se.sundsvall.casedata.api.model.history.History;
import se.sundsvall.casedata.integration.db.AttachmentRepository;
import se.sundsvall.casedata.integration.db.DecisionRepository;
import se.sundsvall.casedata.integration.db.ErrandRepository;
import se.sundsvall.casedata.integration.db.FacilityRepository;
import se.sundsvall.casedata.integration.db.NoteRepository;
import se.sundsvall.casedata.integration.db.StakeholderRepository;

@Service
public class HistoryService {

	private static final ThrowableProblem ATTACHMENT_NOT_FOUND_PROBLEM = Problem.valueOf(NOT_FOUND, "Attachment not found");

	private static final ThrowableProblem DECISION_NOT_FOUND_PROBLEM = Problem.valueOf(NOT_FOUND, "Decision not found");

	private static final ThrowableProblem ERRAND_NOT_FOUND_PROBLEM = Problem.valueOf(NOT_FOUND, "Errand not found");

	private static final ThrowableProblem FACILITY_NOT_FOUND_PROBLEM = Problem.valueOf(NOT_FOUND, "Facility not found");

	private static final ThrowableProblem NOTE_NOT_FOUND_PROBLEM = Problem.valueOf(NOT_FOUND, "Note not found");

	private static final ThrowableProblem STAKEHOLDER_NOT_FOUND_PROBLEM = Problem.valueOf(NOT_FOUND, "Stakeholder not found");

	private final ErrandRepository errandRepository;

	private final DecisionRepository decisionRepository;

	private final AttachmentRepository attachmentRepository;

	private final FacilityRepository facilityRepository;

	private final NoteRepository noteRepository;

	private final StakeholderRepository stakeholderRepository;

	private final Javers javers;

	public HistoryService(final Javers javers, final ErrandRepository errandRepository,
		final DecisionRepository decisionRepository, final AttachmentRepository attachmentRepository,
		final FacilityRepository facilityRepository, final NoteRepository noteRepository,
		final StakeholderRepository stakeholderRepository) {
		this.javers = javers;
		this.errandRepository = errandRepository;
		this.decisionRepository = decisionRepository;
		this.attachmentRepository = attachmentRepository;
		this.facilityRepository = facilityRepository;
		this.noteRepository = noteRepository;
		this.stakeholderRepository = stakeholderRepository;
	}

	public List<History> findAttachmentHistoryOnErrand(final Long errandId, final Long id, final String municipalityId, final String namespace) {
		verifyErrandExists(errandId, municipalityId, namespace);
		final var attachment = attachmentRepository.findByIdAndMunicipalityIdAndNamespace(id, municipalityId, namespace)
			.orElseThrow(() -> ATTACHMENT_NOT_FOUND_PROBLEM);
		return findHistory(attachment, ATTACHMENT_NOT_FOUND_PROBLEM);
	}

	public List<History> findDecisionHistoryOnErrand(final Long errandId, final Long id, final String municipalityId, final String namespace) {
		verifyErrandExists(errandId, municipalityId, namespace);
		final var decision = decisionRepository.findByIdAndMunicipalityIdAndNamespace(id, municipalityId, namespace)
			.orElseThrow(() -> DECISION_NOT_FOUND_PROBLEM);
		return findHistory(decision, DECISION_NOT_FOUND_PROBLEM);
	}

	public List<History> findErrandHistory(final Long errandId, final String municipalityId, final String namespace) {
		final var errand = errandRepository.findByIdAndMunicipalityIdAndNamespace(errandId, municipalityId, namespace)
			.orElseThrow(() -> ERRAND_NOT_FOUND_PROBLEM);
		return findHistory(errand, ERRAND_NOT_FOUND_PROBLEM);
	}

	public List<History> findFacilityHistoryOnErrand(final Long errandId, final Long id, final String municipalityId, final String namespace) {
		verifyErrandExists(errandId, municipalityId, namespace);
		final var facility = facilityRepository.findByIdAndMunicipalityIdAndNamespace(id, municipalityId, namespace)
			.orElseThrow(() -> FACILITY_NOT_FOUND_PROBLEM);
		return findHistory(facility, FACILITY_NOT_FOUND_PROBLEM);
	}

	public List<History> findNoteHistoryOnErrand(final Long errandId, final Long id, final String municipalityId, final String namespace) {
		verifyErrandExists(errandId, municipalityId, namespace);
		final var note = noteRepository.findByIdAndMunicipalityIdAndNamespace(id, municipalityId, namespace)
			.orElseThrow(() -> NOTE_NOT_FOUND_PROBLEM);
		return findHistory(note, NOTE_NOT_FOUND_PROBLEM);
	}

	public List<History> findStakeholderHistoryOnErrand(final Long errandId, final Long id, final String municipalityId, final String namespace) {
		verifyErrandExists(errandId, municipalityId, namespace);

		final var stakeholder = stakeholderRepository.findByIdAndMunicipalityIdAndNamespace(id, municipalityId, namespace)
			.orElseThrow(() -> STAKEHOLDER_NOT_FOUND_PROBLEM);

		return findHistory(stakeholder, STAKEHOLDER_NOT_FOUND_PROBLEM);
	}

	private List<History> findHistory(final Object entity, final ThrowableProblem notFoundProblem) {
		final Changes changes = javers.findChanges(byInstance(entity).withChildValueObjects().build());

		if (changes.isEmpty()) {
			throw notFoundProblem;
		}
		final var prettyJavers = javers.getJsonConverter().toJson(changes);
		final var historyType = new TypeToken<List<History>>() {}.getType();

		@SuppressWarnings("unchecked")
		final List<History> historyList = (List<History>) javers.getJsonConverter().fromJson(prettyJavers, historyType);
		if (historyList.isEmpty()) {
			throw notFoundProblem;
		}
		return historyList;

	}

	private void verifyErrandExists(final Long errandId, final String municipalityId, final String namespace) {
		if (!errandRepository.existsByIdAndMunicipalityIdAndNamespace(errandId, municipalityId, namespace)) {
			throw Problem.valueOf(NOT_FOUND, format(ERRAND_WAS_NOT_FOUND, errandId));
		}
	}
}
