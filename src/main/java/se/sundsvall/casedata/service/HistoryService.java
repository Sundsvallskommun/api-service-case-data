package se.sundsvall.casedata.service;

import static org.zalando.problem.Status.NOT_FOUND;

import org.javers.core.Changes;
import org.javers.core.Javers;
import org.javers.repository.jql.QueryBuilder;
import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;
import org.zalando.problem.ThrowableProblem;

import se.sundsvall.casedata.integration.db.model.Attachment;
import se.sundsvall.casedata.integration.db.model.Decision;
import se.sundsvall.casedata.integration.db.model.Errand;
import se.sundsvall.casedata.integration.db.model.Facility;
import se.sundsvall.casedata.integration.db.model.Note;
import se.sundsvall.casedata.integration.db.model.Stakeholder;

@Service
public class HistoryService {

	private static final ThrowableProblem ATTACHMENT_NOT_FOUND_PROBLEM = Problem.valueOf(NOT_FOUND, "Attachment not found");
	private static final ThrowableProblem DECISION_NOT_FOUND_PROBLEM = Problem.valueOf(NOT_FOUND, "Decision not found");
	private static final ThrowableProblem ERRAND_NOT_FOUND_PROBLEM = Problem.valueOf(NOT_FOUND, "Errand not found");
	private static final ThrowableProblem FACILITY_NOT_FOUND_PROBLEM = Problem.valueOf(NOT_FOUND, "Facility not found");
	private static final ThrowableProblem NOTE_NOT_FOUND_PROBLEM = Problem.valueOf(NOT_FOUND, "Note not found");
	private static final ThrowableProblem STAKEHOLDER_NOT_FOUND_PROBLEM = Problem.valueOf(NOT_FOUND, "Stakeholder not found");

	private final Javers javers;

	public HistoryService(final Javers javers) {
		this.javers = javers;
	}

	public String findAttachmentHistory(final Long id) {
		final QueryBuilder query = QueryBuilder.byInstanceId(id, Attachment.class).withChildValueObjects();
		final Changes changes = javers.findChanges(query.build());
		if (changes.isEmpty()) {
			throw ATTACHMENT_NOT_FOUND_PROBLEM;
		}
		return javers.getJsonConverter().toJson(changes);
	}

	public String findDecisionHistory(final Long id) {
		final QueryBuilder query = QueryBuilder.byInstanceId(id, Decision.class).withChildValueObjects();
		final Changes changes = javers.findChanges(query.build());
		if (changes.isEmpty()) {
			throw DECISION_NOT_FOUND_PROBLEM;
		}
		return javers.getJsonConverter().toJson(changes);
	}

	public String findErrandHistory(final Long id) {
		final QueryBuilder query = QueryBuilder.byInstanceId(id, Errand.class).withChildValueObjects();
		final Changes changes = javers.findChanges(query.build());
		if (changes.isEmpty()) {
			throw ERRAND_NOT_FOUND_PROBLEM;
		}
		return javers.getJsonConverter().toJson(changes);
	}

	public String findFacilityHistory(final Long id) {
		final QueryBuilder query = QueryBuilder.byInstanceId(id, Facility.class).withChildValueObjects();
		final Changes changes = javers.findChanges(query.build());
		if (changes.isEmpty()) {
			throw FACILITY_NOT_FOUND_PROBLEM;
		}
		return javers.getJsonConverter().toJson(changes);
	}

	public String findNoteHistory(final Long id) {
		final QueryBuilder query = QueryBuilder.byInstanceId(id, Note.class).withChildValueObjects();
		final Changes changes = javers.findChanges(query.build());
		if (changes.isEmpty()) {
			throw NOTE_NOT_FOUND_PROBLEM;
		}
		return javers.getJsonConverter().toJson(changes);
	}

	public String findStakeholderHistory(final Long id) {
		final QueryBuilder query = QueryBuilder.byInstanceId(id, Stakeholder.class).withChildValueObjects();
		final Changes changes = javers.findChanges(query.build());
		if (changes.isEmpty()) {
			throw STAKEHOLDER_NOT_FOUND_PROBLEM;
		}
		return javers.getJsonConverter().toJson(changes);
	}

}
