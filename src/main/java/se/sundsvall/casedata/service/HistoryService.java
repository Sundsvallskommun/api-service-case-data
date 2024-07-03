package se.sundsvall.casedata.service;

import static org.javers.repository.jql.QueryBuilder.byInstance;
import static org.zalando.problem.Status.NOT_FOUND;

import org.javers.core.Changes;
import org.javers.core.Javers;
import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;
import org.zalando.problem.ThrowableProblem;

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

	public String findAttachmentHistory(final Long id, final String municipalityId) {
		final var attachment = attachmentRepository.findByIdAndMunicipalityId(id, municipalityId)
			.orElseThrow(() -> ATTACHMENT_NOT_FOUND_PROBLEM);
		final Changes changes = javers.findChanges(byInstance(attachment).withChildValueObjects().build());

		if (changes.isEmpty()) {
			throw ATTACHMENT_NOT_FOUND_PROBLEM;
		}
		return javers.getJsonConverter().toJson(changes);
	}

	public String findDecisionHistory(final Long id, final String municipalityId) {
		final var decision = decisionRepository.findByIdAndMunicipalityId(id, municipalityId)
			.orElseThrow(() -> DECISION_NOT_FOUND_PROBLEM);
		final Changes changes = javers.findChanges(byInstance(decision).withChildValueObjects().build());

		if (changes.isEmpty()) {
			throw DECISION_NOT_FOUND_PROBLEM;
		}
		return javers.getJsonConverter().toJson(changes);
	}

	public String findErrandHistory(final Long id, final String municipalityId) {
		final var errand = errandRepository.findByIdAndMunicipalityId(id, municipalityId)
			.orElseThrow(() -> ERRAND_NOT_FOUND_PROBLEM);
		final Changes changes = javers.findChanges(byInstance(errand).withChildValueObjects().build());

		if (changes.isEmpty()) {
			throw ERRAND_NOT_FOUND_PROBLEM;
		}
		return javers.getJsonConverter().toJson(changes);
	}

	public String findFacilityHistory(final Long id, final String municipalityId) {
		final var facility = facilityRepository.findByIdAndMunicipalityId(id, municipalityId)
			.orElseThrow(() -> FACILITY_NOT_FOUND_PROBLEM);
		final Changes changes = javers.findChanges(byInstance(facility).withChildValueObjects().build());

		if (changes.isEmpty()) {
			throw FACILITY_NOT_FOUND_PROBLEM;
		}
		return javers.getJsonConverter().toJson(changes);
	}

	public String findNoteHistory(final Long id, final String municipalityId) {
		final var note = noteRepository.findByIdAndMunicipalityId(id, municipalityId)
			.orElseThrow(() -> NOTE_NOT_FOUND_PROBLEM);
		final Changes changes = javers.findChanges(byInstance(note).withChildValueObjects().build());

		if (changes.isEmpty()) {
			throw NOTE_NOT_FOUND_PROBLEM;
		}
		return javers.getJsonConverter().toJson(changes);
	}

	public String findStakeholderHistory(final Long id, final String municipalityId) {
		var stakeholder = stakeholderRepository.findByIdAndMunicipalityId(id, municipalityId)
			.orElseThrow(() -> STAKEHOLDER_NOT_FOUND_PROBLEM);
		final Changes changes = javers.findChanges(byInstance(stakeholder).withChildValueObjects().build());

		if (changes.isEmpty()) {
			throw STAKEHOLDER_NOT_FOUND_PROBLEM;
		}
		return javers.getJsonConverter().toJson(changes);
	}

}
