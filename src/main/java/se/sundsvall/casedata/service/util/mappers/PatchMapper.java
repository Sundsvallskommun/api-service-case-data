package se.sundsvall.casedata.service.util.mappers;

import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toAddressEntity;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toFacilityEntity;
import static se.sundsvall.casedata.service.util.mappers.ErrandExtraParameterMapper.toErrandParameterEntityList;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import generated.se.sundsvall.employee.PortalPersonData;
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
import se.sundsvall.casedata.integration.db.model.ExtraParameterEntity;
import se.sundsvall.casedata.integration.db.model.FacilityEntity;
import se.sundsvall.casedata.integration.db.model.NoteEntity;
import se.sundsvall.casedata.integration.db.model.NotificationEntity;
import se.sundsvall.casedata.integration.db.model.StakeholderEntity;

public final class PatchMapper {

	private PatchMapper() {}

	public static ErrandEntity patchErrand(final ErrandEntity errand, final PatchErrand patch) {

		// ExtraParameters are not patched, they are posted for whatever reason.
		Optional.ofNullable(patch.getExtraParameters()).ifPresent(extraParams -> {
			final List<ExtraParameterEntity> newExtraParams = toErrandParameterEntityList(extraParams, errand);
			errand.getExtraParameters().addAll(newExtraParams);
		});
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
		Optional.ofNullable(patch.getFacilities()).ifPresent(facilities -> errand.getFacilities().addAll(patch.getFacilities().stream().map(facility -> toFacilityEntity(facility, errand.getMunicipalityId(), errand.getNamespace())).toList()));
		Optional.ofNullable(patch.getRelatesTo()).ifPresent(relatesTo -> errand.getRelatesTo().addAll(patch.getRelatesTo().stream().map(EntityMapper::toRelatedErrandEntity).toList()));
		Optional.ofNullable(patch.getSuspension()).ifPresent(
			suspension -> {
				errand.setSuspendedFrom(suspension.getSuspendedFrom());
				errand.setSuspendedTo(suspension.getSuspendedTo());
			});

		return errand;
	}

	public static DecisionEntity patchDecision(final DecisionEntity decision, final PatchDecision patch) {
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

	public static StakeholderEntity patchStakeholder(final StakeholderEntity stakeholder, final Stakeholder patch) {
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
		Optional.ofNullable(patch.getAddresses()).ifPresent(s -> stakeholder.setAddresses(new ArrayList<>(patch.getAddresses().stream().map(EntityMapper::toAddressEntity).toList())));
		Optional.ofNullable(patch.getContactInformation()).ifPresent(s -> stakeholder.setContactInformation(new ArrayList<>(patch.getContactInformation().stream().map(EntityMapper::toContactInformationEntity).toList())));
		return stakeholder;
	}

	public static AttachmentEntity patchAttachment(final AttachmentEntity attachmentEntity, final Attachment patch) {
		// ExtraParameters are not patched, they are posted for whatever reason.
		Optional.ofNullable(patch.getExtraParameters()).ifPresent(s -> attachmentEntity.getExtraParameters().putAll(patch.getExtraParameters()));
		Optional.ofNullable(patch.getCategory()).ifPresent(attachmentEntity::setCategory);
		Optional.ofNullable(patch.getName()).ifPresent(attachmentEntity::setName);
		Optional.ofNullable(patch.getNote()).ifPresent(attachmentEntity::setNote);
		Optional.ofNullable(patch.getExtension()).ifPresent(attachmentEntity::setExtension);
		Optional.ofNullable(patch.getMimeType()).ifPresent(attachmentEntity::setMimeType);
		Optional.ofNullable(patch.getFile()).ifPresent(attachmentEntity::setFile);
		return attachmentEntity;
	}

	public static NoteEntity patchNote(final NoteEntity noteEntity, final Note patch) {
		// ExtraParameters are not patched, they are posted for whatever reason.
		Optional.ofNullable(patch.getExtraParameters()).ifPresent(s -> noteEntity.getExtraParameters().putAll(patch.getExtraParameters()));
		Optional.ofNullable(patch.getTitle()).ifPresent(noteEntity::setTitle);
		Optional.ofNullable(patch.getText()).ifPresent(noteEntity::setText);
		Optional.ofNullable(patch.getNoteType()).ifPresent(noteEntity::setNoteType);
		Optional.ofNullable(patch.getCreatedBy()).ifPresent(noteEntity::setCreatedBy);
		Optional.ofNullable(patch.getUpdatedBy()).ifPresent(noteEntity::setUpdatedBy);
		return noteEntity;
	}

	public static FacilityEntity patchFacility(final FacilityEntity facility, final Facility patch) {
		// ExtraParameters are not patched, they are posted for whatever reason.
		Optional.ofNullable(patch.getExtraParameters()).ifPresent(obj -> facility.getExtraParameters().putAll(obj));
		Optional.ofNullable(patch.getDescription()).ifPresent(facility::setDescription);
		Optional.ofNullable(patch.getAddress()).ifPresent(obj -> facility.setAddressEntity(toAddressEntity(obj)));
		Optional.ofNullable(patch.getFacilityCollectionName()).ifPresent(facility::setFacilityCollectionName);
		Optional.ofNullable(patch.getFacilityType()).ifPresent(facility::setFacilityType);
		Optional.of(patch.isMainFacility()).ifPresent(facility::setMainFacility);
		return facility;
	}

	public static NotificationEntity patchNotification(final NotificationEntity notificationEntity, final PatchNotification patch, final PortalPersonData owner) {
		Optional.ofNullable(patch.getAcknowledged()).ifPresent(notificationEntity::setAcknowledged);
		Optional.ofNullable(patch.getContent()).ifPresent(notificationEntity::setContent);
		Optional.ofNullable(patch.getDescription()).ifPresent(notificationEntity::setDescription);
		Optional.ofNullable(patch.getExpires()).ifPresent(notificationEntity::setExpires);
		Optional.ofNullable(owner).ifPresent(obj -> notificationEntity.setOwnerFullName(obj.getFullname()));
		Optional.ofNullable(patch.getOwnerId()).ifPresent(notificationEntity::setOwnerId);
		Optional.ofNullable(patch.getType()).ifPresent(notificationEntity::setType);
		return notificationEntity;
	}

}
