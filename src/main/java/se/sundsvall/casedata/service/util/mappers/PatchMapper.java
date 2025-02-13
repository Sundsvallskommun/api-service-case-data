package se.sundsvall.casedata.service.util.mappers;

import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toAddressEntity;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toFacilityEntity;
import static se.sundsvall.casedata.service.util.mappers.ErrandExtraParameterMapper.toErrandParameterEntityList;

import java.util.ArrayList;
import java.util.List;
import se.sundsvall.casedata.api.model.Attachment;
import se.sundsvall.casedata.api.model.Facility;
import se.sundsvall.casedata.api.model.Note;
import se.sundsvall.casedata.api.model.PatchDecision;
import se.sundsvall.casedata.api.model.PatchErrand;
import se.sundsvall.casedata.api.model.PatchNotification;
import se.sundsvall.casedata.api.model.Stakeholder;
import se.sundsvall.casedata.integration.db.model.AttachmentEntity;
import se.sundsvall.casedata.integration.db.model.DecisionEntity;
import se.sundsvall.casedata.integration.db.model.ErrandEntity;
import se.sundsvall.casedata.integration.db.model.FacilityEntity;
import se.sundsvall.casedata.integration.db.model.NoteEntity;
import se.sundsvall.casedata.integration.db.model.NotificationEntity;
import se.sundsvall.casedata.integration.db.model.StakeholderEntity;

public final class PatchMapper {

	private PatchMapper() {}

	public static ErrandEntity patchErrand(final ErrandEntity errand, final PatchErrand patch) {

		ofNullable(patch.getExtraParameters()).ifPresent(extraParameters -> {
			ofNullable(errand.getExtraParameters()).ifPresentOrElse(List::clear, () -> errand.setExtraParameters(new ArrayList<>()));
			errand.getExtraParameters().addAll(toErrandParameterEntityList(extraParameters, errand));
		});

		ofNullable(patch.getCaseType()).ifPresent(caseType -> errand.setCaseType(caseType.name()));
		ofNullable(patch.getExternalCaseId()).ifPresent(errand::setExternalCaseId);
		ofNullable(patch.getPriority()).ifPresent(errand::setPriority);
		ofNullable(patch.getDescription()).ifPresent(errand::setDescription);
		ofNullable(patch.getCaseTitleAddition()).ifPresent(errand::setCaseTitleAddition);
		ofNullable(patch.getDiaryNumber()).ifPresent(errand::setDiaryNumber);
		ofNullable(patch.getPhase()).ifPresent(errand::setPhase);
		ofNullable(patch.getStartDate()).ifPresent(errand::setStartDate);
		ofNullable(patch.getEndDate()).ifPresent(errand::setEndDate);
		ofNullable(patch.getApplicationReceived()).ifPresent(errand::setApplicationReceived);
		ofNullable(patch.getFacilities()).ifPresent(facilities -> errand.getFacilities().addAll(patch.getFacilities().stream().map(facility -> toFacilityEntity(facility, errand.getMunicipalityId(), errand.getNamespace())).toList()));
		ofNullable(patch.getRelatesTo()).ifPresent(relatesTo -> errand.getRelatesTo().addAll(patch.getRelatesTo().stream().map(EntityMapper::toRelatedErrandEntity).toList()));
		ofNullable(patch.getLabels()).ifPresent(labels -> errand.setLabels(patch.getLabels()));
		ofNullable(patch.getSuspension()).ifPresent(
			suspension -> {
				errand.setSuspendedFrom(suspension.getSuspendedFrom());
				errand.setSuspendedTo(suspension.getSuspendedTo());
			});

		ofNullable(patch.getStatus()).ifPresent(status -> {
			final var statusEntity = EntityMapper.toStatusEntity(status);
			errand.setStatus(statusEntity);
			errand.getStatuses().add(statusEntity);
		});

		return errand;
	}

	public static DecisionEntity patchDecision(final DecisionEntity decision, final PatchDecision patch) {
		// ExtraParameters are not patched, they are posted for whatever reason.
		ofNullable(patch.getExtraParameters()).ifPresent(s -> decision.getExtraParameters().putAll(patch.getExtraParameters()));
		ofNullable(patch.getDecisionType()).ifPresent(decision::setDecisionType);
		ofNullable(patch.getDecisionOutcome()).ifPresent(decision::setDecisionOutcome);
		ofNullable(patch.getDescription()).ifPresent(decision::setDescription);
		ofNullable(patch.getDecidedAt()).ifPresent(decision::setDecidedAt);
		ofNullable(patch.getValidFrom()).ifPresent(decision::setValidFrom);
		ofNullable(patch.getValidTo()).ifPresent(decision::setValidTo);
		return decision;
	}

	public static StakeholderEntity patchStakeholder(final StakeholderEntity stakeholder, final Stakeholder patch) {
		// ExtraParameters are not patched, they are posted for whatever reason.
		ofNullable(patch.getExtraParameters()).ifPresent(s -> stakeholder.getExtraParameters().putAll(patch.getExtraParameters()));
		ofNullable(patch.getType()).ifPresent(stakeholder::setType);
		ofNullable(patch.getFirstName()).ifPresent(stakeholder::setFirstName);
		ofNullable(patch.getLastName()).ifPresent(stakeholder::setLastName);
		ofNullable(patch.getPersonId()).ifPresent(stakeholder::setPersonId);
		ofNullable(patch.getOrganizationName()).ifPresent(stakeholder::setOrganizationName);
		ofNullable(patch.getOrganizationNumber()).ifPresent(stakeholder::setOrganizationNumber);
		ofNullable(patch.getAuthorizedSignatory()).ifPresent(stakeholder::setAuthorizedSignatory);
		ofNullable(patch.getAdAccount()).ifPresent(stakeholder::setAdAccount);
		ofNullable(patch.getRoles()).ifPresent(roles -> stakeholder.setRoles(patch.getRoles()));
		ofNullable(patch.getAddresses()).ifPresent(s -> stakeholder.setAddresses(new ArrayList<>(patch.getAddresses().stream().map(EntityMapper::toAddressEntity).toList())));
		ofNullable(patch.getContactInformation()).ifPresent(s -> stakeholder.setContactInformation(new ArrayList<>(patch.getContactInformation().stream().map(EntityMapper::toContactInformationEntity).toList())));
		return stakeholder;
	}

	public static AttachmentEntity patchAttachment(final AttachmentEntity attachmentEntity, final Attachment patch) {
		// ExtraParameters are not patched, they are posted for whatever reason.
		ofNullable(patch.getExtraParameters()).ifPresent(s -> attachmentEntity.getExtraParameters().putAll(patch.getExtraParameters()));
		ofNullable(patch.getCategory()).ifPresent(attachmentEntity::setCategory);
		ofNullable(patch.getName()).ifPresent(attachmentEntity::setName);
		ofNullable(patch.getNote()).ifPresent(attachmentEntity::setNote);
		ofNullable(patch.getExtension()).ifPresent(attachmentEntity::setExtension);
		ofNullable(patch.getMimeType()).ifPresent(attachmentEntity::setMimeType);
		ofNullable(patch.getFile()).ifPresent(attachmentEntity::setFile);
		return attachmentEntity;
	}

	public static NoteEntity patchNote(final NoteEntity noteEntity, final Note patch) {
		// ExtraParameters are not patched, they are posted for whatever reason.
		ofNullable(patch.getExtraParameters()).ifPresent(s -> noteEntity.getExtraParameters().putAll(patch.getExtraParameters()));
		ofNullable(patch.getTitle()).ifPresent(noteEntity::setTitle);
		ofNullable(patch.getText()).ifPresent(noteEntity::setText);
		ofNullable(patch.getNoteType()).ifPresent(noteEntity::setNoteType);
		ofNullable(patch.getCreatedBy()).ifPresent(noteEntity::setCreatedBy);
		ofNullable(patch.getUpdatedBy()).ifPresent(noteEntity::setUpdatedBy);
		return noteEntity;
	}

	public static FacilityEntity patchFacility(final FacilityEntity facility, final Facility patch) {
		// ExtraParameters are not patched, they are posted for whatever reason.
		ofNullable(patch.getExtraParameters()).ifPresent(obj -> facility.getExtraParameters().putAll(obj));
		ofNullable(patch.getDescription()).ifPresent(facility::setDescription);
		ofNullable(patch.getAddress()).ifPresent(obj -> facility.setAddress(toAddressEntity(obj)));
		ofNullable(patch.getFacilityCollectionName()).ifPresent(facility::setFacilityCollectionName);
		ofNullable(patch.getFacilityType()).ifPresent(facility::setFacilityType);
		of(patch.isMainFacility()).ifPresent(facility::setMainFacility);
		return facility;
	}

	public static NotificationEntity patchNotification(final NotificationEntity notificationEntity, final PatchNotification patch) {
		ofNullable(patch.getAcknowledged()).ifPresent(notificationEntity::setAcknowledged);
		ofNullable(patch.getContent()).ifPresent(notificationEntity::setContent);
		ofNullable(patch.getDescription()).ifPresent(notificationEntity::setDescription);
		ofNullable(patch.getExpires()).ifPresent(notificationEntity::setExpires);
		ofNullable(patch.getOwnerId()).ifPresent(notificationEntity::setOwnerId);
		ofNullable(patch.getType()).ifPresent(notificationEntity::setType);
		return notificationEntity;
	}
}
