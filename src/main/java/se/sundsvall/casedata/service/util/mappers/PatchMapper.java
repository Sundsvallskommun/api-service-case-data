package se.sundsvall.casedata.service.util.mappers;

import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toAddress;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toFacility;

import java.util.ArrayList;
import java.util.Optional;

import se.sundsvall.casedata.api.model.AttachmentDTO;
import se.sundsvall.casedata.api.model.FacilityDTO;
import se.sundsvall.casedata.api.model.NoteDTO;
import se.sundsvall.casedata.api.model.PatchAppealDTO;
import se.sundsvall.casedata.api.model.PatchDecisionDTO;
import se.sundsvall.casedata.api.model.PatchErrandDTO;
import se.sundsvall.casedata.api.model.StakeholderDTO;
import se.sundsvall.casedata.integration.db.model.Appeal;
import se.sundsvall.casedata.integration.db.model.Attachment;
import se.sundsvall.casedata.integration.db.model.Decision;
import se.sundsvall.casedata.integration.db.model.Errand;
import se.sundsvall.casedata.integration.db.model.Facility;
import se.sundsvall.casedata.integration.db.model.Note;
import se.sundsvall.casedata.integration.db.model.Stakeholder;
import se.sundsvall.casedata.integration.db.model.enums.AppealStatus;
import se.sundsvall.casedata.integration.db.model.enums.TimelinessReview;

public final class PatchMapper {

	private PatchMapper() {}

	public static Errand patchErrand(final Errand errand, final PatchErrandDTO patch) {
		// ExtraParameters are not patched, they are posted for whatever reason.
		Optional.ofNullable(patch.getExtraParameters()).ifPresent(s -> errand.getExtraParameters().putAll(patch.getExtraParameters()));
		Optional.ofNullable(patch.getCaseType()).ifPresent(caseType -> errand.setCaseType(caseType.name()));
		Optional.ofNullable(patch.getExternalCaseId()).ifPresent(errand::setExternalCaseId);
		Optional.ofNullable(patch.getPriority()).ifPresent(errand::setPriority);
		Optional.ofNullable(patch.getDescription()).ifPresent(errand::setDescription);
		Optional.ofNullable(patch.getCaseTitleAddition()).ifPresent(errand::setCaseTitleAddition);
		Optional.ofNullable(patch.getDiaryNumber()).ifPresent(errand::setDiaryNumber);
		Optional.ofNullable(patch.getPhase()).ifPresent(errand::setPhase);
		Optional.ofNullable(patch.getStartDate()).ifPresent(errand::setStartDate);
		Optional.ofNullable(patch.getEndDate()).ifPresent(errand::setEndDate);
		Optional.ofNullable(patch.getApplicationReceived()).ifPresent(errand::setApplicationReceived);
		Optional.ofNullable(patch.getFacilities()).ifPresent(facilities -> errand.getFacilities().addAll(patch.getFacilities().stream().map(facilityDTO -> toFacility(facilityDTO, errand.getMunicipalityId())).toList()));
		return errand;
	}

	public static Decision patchDecision(final Decision decision, final PatchDecisionDTO patch) {
		// ExtraParameters are not patched, they are posted for whatever reason.
		Optional.ofNullable(patch.getExtraParameters()).ifPresent(s -> decision.getExtraParameters().putAll(patch.getExtraParameters()));
		Optional.ofNullable(patch.getDecisionType()).ifPresent(decision::setDecisionType);
		Optional.ofNullable(patch.getDecisionOutcome()).ifPresent(decision::setDecisionOutcome);
		Optional.ofNullable(patch.getDescription()).ifPresent(decision::setDescription);
		Optional.ofNullable(patch.getDecidedAt()).ifPresent(decision::setDecidedAt);
		Optional.ofNullable(patch.getValidFrom()).ifPresent(decision::setValidFrom);
		Optional.ofNullable(patch.getValidTo()).ifPresent(decision::setValidTo);
		return decision;
	}

	public static Appeal patchAppeal(final Appeal appeal, final PatchAppealDTO patch) {
		Optional.ofNullable(patch.getDescription()).ifPresent(appeal::setDescription);
		Optional.ofNullable(patch.getStatus()).ifPresent(status -> appeal.setStatus(AppealStatus.valueOf(patch.getStatus())));
		Optional.ofNullable(patch.getTimelinessReview()).ifPresent(timeLinesReview -> appeal.setTimelinessReview(TimelinessReview.valueOf(patch.getTimelinessReview())));
		return appeal;
	}

	public static Stakeholder patchStakeholder(final Stakeholder stakeholder, final StakeholderDTO patch) {
		// ExtraParameters are not patched, they are posted for whatever reason.
		Optional.ofNullable(patch.getExtraParameters()).ifPresent(s -> stakeholder.getExtraParameters().putAll(patch.getExtraParameters()));
		Optional.ofNullable(patch.getType()).ifPresent(stakeholder::setType);
		Optional.ofNullable(patch.getFirstName()).ifPresent(stakeholder::setFirstName);
		Optional.ofNullable(patch.getLastName()).ifPresent(stakeholder::setLastName);
		Optional.ofNullable(patch.getPersonId()).ifPresent(stakeholder::setPersonId);
		Optional.ofNullable(patch.getOrganizationName()).ifPresent(stakeholder::setOrganizationName);
		Optional.ofNullable(patch.getOrganizationNumber()).ifPresent(stakeholder::setOrganizationNumber);
		Optional.ofNullable(patch.getAuthorizedSignatory()).ifPresent(stakeholder::setAuthorizedSignatory);
		Optional.ofNullable(patch.getAdAccount()).ifPresent(stakeholder::setAdAccount);
		Optional.ofNullable(patch.getRoles()).ifPresent(roles -> stakeholder.setRoles(patch.getRoles()));
		Optional.ofNullable(patch.getAddresses()).ifPresent(s -> stakeholder.setAddresses(new ArrayList<>(patch.getAddresses().stream().map(EntityMapper::toAddress).toList())));
		Optional.ofNullable(patch.getContactInformation()).ifPresent(s -> stakeholder.setContactInformation(new ArrayList<>(patch.getContactInformation().stream().map(EntityMapper::toContactInformation).toList())));
		return stakeholder;
	}

	public static Attachment patchAttachment(final Attachment attachment, final AttachmentDTO patch) {
		// ExtraParameters are not patched, they are posted for whatever reason.
		Optional.ofNullable(patch.getExtraParameters()).ifPresent(s -> attachment.getExtraParameters().putAll(patch.getExtraParameters()));
		Optional.ofNullable(patch.getCategory()).ifPresent(attachment::setCategory);
		Optional.ofNullable(patch.getName()).ifPresent(attachment::setName);
		Optional.ofNullable(patch.getNote()).ifPresent(attachment::setNote);
		Optional.ofNullable(patch.getExtension()).ifPresent(attachment::setExtension);
		Optional.ofNullable(patch.getMimeType()).ifPresent(attachment::setMimeType);
		Optional.ofNullable(patch.getFile()).ifPresent(attachment::setFile);
		return attachment;
	}

	public static Note patchNote(final Note note, final NoteDTO patch) {
		// ExtraParameters are not patched, they are posted for whatever reason.
		Optional.ofNullable(patch.getExtraParameters()).ifPresent(s -> note.getExtraParameters().putAll(patch.getExtraParameters()));
		Optional.ofNullable(patch.getTitle()).ifPresent(note::setTitle);
		Optional.ofNullable(patch.getText()).ifPresent(note::setText);
		Optional.ofNullable(patch.getNoteType()).ifPresent(note::setNoteType);
		Optional.ofNullable(patch.getCreatedBy()).ifPresent(note::setCreatedBy);
		Optional.ofNullable(patch.getUpdatedBy()).ifPresent(note::setUpdatedBy);
		return note;
	}

	public static Facility patchFacility(final Facility facility, final FacilityDTO patch) {
		// ExtraParameters are not patched, they are posted for whatever reason.
		Optional.ofNullable(patch.getExtraParameters()).ifPresent(obj -> facility.getExtraParameters().putAll(obj));
		Optional.ofNullable(patch.getDescription()).ifPresent(facility::setDescription);
		Optional.ofNullable(patch.getAddress()).ifPresent(obj -> facility.setAddress(toAddress(obj)));
		Optional.ofNullable(patch.getFacilityCollectionName()).ifPresent(facility::setFacilityCollectionName);
		Optional.ofNullable(patch.getFacilityType()).ifPresent(facility::setFacilityType);
		Optional.of(patch.isMainFacility()).ifPresent(facility::setMainFacility);
		return facility;
	}
}
