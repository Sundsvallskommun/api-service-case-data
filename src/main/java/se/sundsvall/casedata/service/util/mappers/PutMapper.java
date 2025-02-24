package se.sundsvall.casedata.service.util.mappers;

import java.util.ArrayList;
import java.util.Optional;
import se.sundsvall.casedata.api.model.Attachment;
import se.sundsvall.casedata.api.model.Decision;
import se.sundsvall.casedata.api.model.Facility;
import se.sundsvall.casedata.api.model.Note;
import se.sundsvall.casedata.api.model.Stakeholder;
import se.sundsvall.casedata.integration.db.model.AttachmentEntity;
import se.sundsvall.casedata.integration.db.model.DecisionEntity;
import se.sundsvall.casedata.integration.db.model.FacilityEntity;
import se.sundsvall.casedata.integration.db.model.NoteEntity;
import se.sundsvall.casedata.integration.db.model.StakeholderEntity;

public final class PutMapper {

	private PutMapper() {}

	public static AttachmentEntity putAttachment(final AttachmentEntity oldAttachmentEntity, final Attachment attachment) {
		Optional.ofNullable(attachment).ifPresent(newAttachment -> {
			oldAttachmentEntity.setExtraParameters(newAttachment.getExtraParameters());
			oldAttachmentEntity.setCategory(newAttachment.getCategory());
			oldAttachmentEntity.setName(newAttachment.getName());
			oldAttachmentEntity.setNote(newAttachment.getNote());
			oldAttachmentEntity.setExtension(newAttachment.getExtension());
			oldAttachmentEntity.setMimeType(newAttachment.getMimeType());
		});
		return oldAttachmentEntity;
	}

	public static StakeholderEntity putStakeholder(final StakeholderEntity oldStakeholder, final Stakeholder stakeholder) {
		Optional.ofNullable(stakeholder).ifPresent(newStakeholder -> {
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
			oldStakeholder.setAddresses(new ArrayList<>(newStakeholder.getAddresses().stream().map(EntityMapper::toAddressEntity).toList()));
			oldStakeholder.setContactInformation(new ArrayList<>(newStakeholder.getContactInformation().stream().map(EntityMapper::toContactInformationEntity).toList()));
		});
		return oldStakeholder;
	}

	public static DecisionEntity putDecision(final DecisionEntity oldDecision, final Decision decision) {
		Optional.ofNullable(decision).ifPresent(newDecision -> {
			oldDecision.setExtraParameters(newDecision.getExtraParameters());
			oldDecision.setDecisionType(newDecision.getDecisionType());
			oldDecision.setDecisionOutcome(newDecision.getDecisionOutcome());
			oldDecision.setDescription(newDecision.getDescription());
			oldDecision.setDecidedAt(newDecision.getDecidedAt());
			oldDecision.setValidFrom(newDecision.getValidFrom());
			oldDecision.setValidTo(newDecision.getValidTo());
			oldDecision.setDecidedBy(EntityMapper.toStakeholderEntity(newDecision.getDecidedBy(), oldDecision.getMunicipalityId(), oldDecision.getNamespace()));
			oldDecision.setLaw(new ArrayList<>(newDecision.getLaw().stream().map(EntityMapper::toLawEntity).toList()));
			oldDecision.getAttachments().clear();
			newDecision.getAttachments().forEach(attachment -> oldDecision.getAttachments().add(EntityMapper.toAttachmentEntity(oldDecision.getErrand().getId(), attachment, oldDecision.getMunicipalityId(), oldDecision.getNamespace())));
		});
		return oldDecision;
	}

	public static NoteEntity putNote(final NoteEntity oldNoteEntity, final Note note) {
		Optional.of(note).ifPresent(newNote -> {
			oldNoteEntity.setExtraParameters(newNote.getExtraParameters());
			oldNoteEntity.setTitle(newNote.getTitle());
			oldNoteEntity.setText(newNote.getText());
			oldNoteEntity.setCreatedBy(newNote.getCreatedBy());
			oldNoteEntity.setUpdatedBy(newNote.getUpdatedBy());
		});
		return oldNoteEntity;
	}

	public static FacilityEntity putFacility(final FacilityEntity oldFacility, final Facility facility) {
		Optional.of(facility).ifPresent(facilityEntity -> {
			oldFacility.setFacilityType(facility.getFacilityType());
			oldFacility.setMainFacility(facility.isMainFacility());
			oldFacility.setDescription(facility.getDescription());
			oldFacility.setAddress(EntityMapper.toAddressEntity(facility.getAddress()));
			oldFacility.setFacilityCollectionName(facility.getFacilityCollectionName());
			oldFacility.setExtraParameters(facility.getExtraParameters());
		});
		return oldFacility;
	}

}
