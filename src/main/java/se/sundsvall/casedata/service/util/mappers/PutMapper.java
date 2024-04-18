package se.sundsvall.casedata.service.util.mappers;

import se.sundsvall.casedata.api.model.AppealDTO;
import se.sundsvall.casedata.api.model.AttachmentDTO;
import se.sundsvall.casedata.api.model.DecisionDTO;
import se.sundsvall.casedata.api.model.NoteDTO;
import se.sundsvall.casedata.api.model.StakeholderDTO;
import se.sundsvall.casedata.integration.db.model.Appeal;
import se.sundsvall.casedata.integration.db.model.Attachment;
import se.sundsvall.casedata.integration.db.model.Decision;
import se.sundsvall.casedata.integration.db.model.Note;
import se.sundsvall.casedata.integration.db.model.Stakeholder;
import se.sundsvall.casedata.integration.db.model.enums.AppealStatus;
import se.sundsvall.casedata.integration.db.model.enums.TimelinessReview;

import java.util.ArrayList;
import java.util.Optional;

public final class PutMapper {

	private PutMapper() {
	}

	public static Attachment putAttachment(final Attachment oldAttachment, final AttachmentDTO dto) {
		Optional.ofNullable(dto).ifPresent(newAttachment -> {
			oldAttachment.setExtraParameters(newAttachment.getExtraParameters());
			oldAttachment.setCategory(newAttachment.getCategory());
			oldAttachment.setName(newAttachment.getName());
			oldAttachment.setNote(newAttachment.getNote());
			oldAttachment.setExtension(newAttachment.getExtension());
			oldAttachment.setMimeType(newAttachment.getMimeType());
			oldAttachment.setFile(newAttachment.getFile());
		});
		return oldAttachment;
	}

	public static Stakeholder putStakeholder(final Stakeholder oldStakeholder, final StakeholderDTO dto) {
		Optional.ofNullable(dto).ifPresent(newStakeholder -> {
			oldStakeholder.setExtraParameters(newStakeholder.getExtraParameters());
			oldStakeholder.setType(newStakeholder.getType());
			oldStakeholder.setFirstName(newStakeholder.getFirstName());
			oldStakeholder.setLastName(newStakeholder.getLastName());
			oldStakeholder.setPersonId(newStakeholder.getPersonId());
			oldStakeholder.setOrganizationName(newStakeholder.getOrganizationName());
			oldStakeholder.setOrganizationNumber(newStakeholder.getOrganizationNumber());
			oldStakeholder.setAuthorizedSignatory(newStakeholder.getAuthorizedSignatory());
			oldStakeholder.setAdAccount(newStakeholder.getAdAccount());
			oldStakeholder.setRoles(newStakeholder.getRoles());
			oldStakeholder.setAddresses(new ArrayList<>(newStakeholder.getAddresses().stream().map(EntityMapper::toAddress).toList()));
			oldStakeholder.setContactInformation(new ArrayList<>(newStakeholder.getContactInformation().stream().map(EntityMapper::toContactInformation).toList()));
		});
		return oldStakeholder;
	}

	public static Decision putDecision(final Decision oldDecision, final DecisionDTO dto) {
		Optional.ofNullable(dto).ifPresent(newDecision -> {
			oldDecision.setExtraParameters(newDecision.getExtraParameters());
			oldDecision.setDecisionType(newDecision.getDecisionType());
			oldDecision.setDecisionOutcome(newDecision.getDecisionOutcome());
			oldDecision.setDescription(newDecision.getDescription());
			oldDecision.setDecidedAt(newDecision.getDecidedAt());
			oldDecision.setValidFrom(newDecision.getValidFrom());
			oldDecision.setValidTo(newDecision.getValidTo());
			oldDecision.setDecidedBy(EntityMapper.toStakeholder(newDecision.getDecidedBy()));
			oldDecision.setLaw(new ArrayList<>(newDecision.getLaw().stream().map(EntityMapper::toLaw).toList()));
			oldDecision.getAttachments().clear();
			newDecision.getAttachments().forEach(attachment -> oldDecision.getAttachments().add(EntityMapper.toAttachment(attachment)));
		});
		return oldDecision;
	}

	public static Appeal putAppeal(final Appeal oldAppeal, final AppealDTO dto) {
		Optional.ofNullable(dto).ifPresent(newAppeal -> {
			oldAppeal.setDescription(dto.getDescription());
			oldAppeal.setStatus(AppealStatus.valueOf(dto.getStatus()));
			oldAppeal.setTimelinessReview(TimelinessReview.valueOf(dto.getTimelinessReview()));
			oldAppeal.setAppealConcernCommunicatedAt(dto.getAppealConcernCommunicatedAt());
		});
		return oldAppeal;
	}

	public static Note putNote(final Note oldNote, final NoteDTO dto) {
		Optional.of(dto).ifPresent(newNote -> {
			oldNote.setExtraParameters(newNote.getExtraParameters());
			oldNote.setTitle(newNote.getTitle());
			oldNote.setText(newNote.getText());
			oldNote.setCreatedBy(newNote.getCreatedBy());
			oldNote.setUpdatedBy(newNote.getUpdatedBy());
		});
		return oldNote;
	}

}
