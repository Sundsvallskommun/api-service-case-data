package se.sundsvall.casedata.service.util.mappers;

import java.util.List;
import org.junit.jupiter.api.Test;
import se.sundsvall.casedata.api.model.validation.enums.AttachmentCategory;
import se.sundsvall.casedata.api.model.validation.enums.StakeholderRole;
import se.sundsvall.casedata.integration.db.model.enums.StakeholderType;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.casedata.TestUtil.MUNICIPALITY_ID;
import static se.sundsvall.casedata.TestUtil.NAMESPACE;
import static se.sundsvall.casedata.TestUtil.createAttachment;
import static se.sundsvall.casedata.TestUtil.createAttachmentEntity;
import static se.sundsvall.casedata.TestUtil.createDecisionEntity;
import static se.sundsvall.casedata.TestUtil.createErrandEntity;
import static se.sundsvall.casedata.TestUtil.createFacility;
import static se.sundsvall.casedata.TestUtil.createFacilityEntity;
import static se.sundsvall.casedata.TestUtil.createNote;
import static se.sundsvall.casedata.TestUtil.createNoteEntity;
import static se.sundsvall.casedata.TestUtil.createNotificationEntity;
import static se.sundsvall.casedata.TestUtil.createPatchDecision;
import static se.sundsvall.casedata.TestUtil.createPatchErrand;
import static se.sundsvall.casedata.TestUtil.createPatchNotification;
import static se.sundsvall.casedata.TestUtil.createStakeholder;
import static se.sundsvall.casedata.TestUtil.createStakeholderEntity;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toAddressEntity;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toFacilityEntity;
import static se.sundsvall.casedata.service.util.mappers.PatchMapper.patchAttachment;
import static se.sundsvall.casedata.service.util.mappers.PatchMapper.patchDecision;
import static se.sundsvall.casedata.service.util.mappers.PatchMapper.patchErrand;
import static se.sundsvall.casedata.service.util.mappers.PatchMapper.patchFacility;
import static se.sundsvall.casedata.service.util.mappers.PatchMapper.patchNote;
import static se.sundsvall.casedata.service.util.mappers.PatchMapper.patchStakeholder;

class PatchMapperTest {

	@Test
	void patchErrandTest() {
		final var errand = createErrandEntity();
		final var patch = createPatchErrand();

		final var patchedErrand = patchErrand(errand, patch);

		assertThat(patchedErrand).isNotNull().hasNoNullFieldsOrPropertiesExcept("id", "notifications").satisfies(e -> {
			assertThat(e.getCaseType()).isEqualTo(patch.getCaseType());
			assertThat(e.getDescription()).isEqualTo(patch.getDescription());
			assertThat(e.getExternalCaseId()).isEqualTo(patch.getExternalCaseId());
			assertThat(e.getPriority()).isEqualTo(patch.getPriority());
			assertThat(e.getCaseTitleAddition()).isEqualTo(patch.getCaseTitleAddition());
			assertThat(e.getDiaryNumber()).isEqualTo(patch.getDiaryNumber());
			assertThat(e.getPhase()).isEqualTo(patch.getPhase());
			assertThat(e.getStartDate()).isEqualTo(patch.getStartDate());
			assertThat(e.getEndDate()).isEqualTo(patch.getEndDate());
			assertThat(e.getApplicationReceived()).isEqualTo(patch.getApplicationReceived());
			assertThat(e.getExtraParameters()).hasSize(3).containsAll(patch.getExtraParameters().stream().map(parameter -> ErrandExtraParameterMapper.toErrandParameterEntity(parameter, errand)).toList());
			assertThat(e.getSuspendedFrom()).isEqualTo(patch.getSuspension().getSuspendedFrom());
			assertThat(e.getSuspendedTo()).isEqualTo(patch.getSuspension().getSuspendedTo());
			assertThat(e.getFacilities()).hasSize(2).containsAll(patch.getFacilities().stream().map(facilityDTO -> toFacilityEntity(facilityDTO, MUNICIPALITY_ID, NAMESPACE)).toList());
			assertThat(e.getRelatesTo()).hasSize(2).containsAll(patch.getRelatesTo().stream().map(EntityMapper::toRelatedErrandEntity).toList());
			assertThat(e.getLabels()).isEqualTo(patch.getLabels());
		});
	}

	@Test
	void patchDecisionTest() {
		final var decision = createDecisionEntity();
		final var patch = createPatchDecision();

		final var patchedDecision = patchDecision(decision, patch);

		assertThat(patchedDecision).isNotNull().hasNoNullFieldsOrPropertiesExcept("errand", "municipalityId", "namespace").satisfies(d -> {
			assertThat(d.getDecisionType()).isEqualTo(patch.getDecisionType());
			assertThat(d.getDecisionOutcome()).isEqualTo(patch.getDecisionOutcome());
			assertThat(d.getDescription()).isEqualTo(patch.getDescription());
			assertThat(d.getDecidedAt()).isEqualTo(patch.getDecidedAt());
			assertThat(d.getValidFrom()).isEqualTo(patch.getValidFrom());
			assertThat(d.getValidTo()).isEqualTo(patch.getValidTo());
			assertThat(d.getExtraParameters()).containsAllEntriesOf(patch.getExtraParameters());
		});
	}

	@Test
	void patchStakeholderOrganizationTest() {
		final var stakeholder = createStakeholderEntity();
		final var patch = createStakeholder(StakeholderType.ORGANIZATION, List.of(StakeholderRole.OPERATOR.name()));

		final var patchedStakeholder = patchStakeholder(stakeholder, patch);

		assertThat(patchedStakeholder).isNotNull().hasNoNullFieldsOrPropertiesExcept("errand").satisfies(s -> {
			assertThat(s.getType()).isEqualTo(patch.getType());
			assertThat(s.getOrganizationName()).isEqualTo(patch.getOrganizationName());
			assertThat(s.getOrganizationNumber()).isEqualTo(patch.getOrganizationNumber());
			assertThat(s.getAuthorizedSignatory()).isEqualTo(patch.getAuthorizedSignatory());
			assertThat(s.getAdAccount()).isEqualTo(patch.getAdAccount());
			assertThat(s.getRoles()).isEqualTo(patch.getRoles());
			assertThat(s.getAddresses()).isEqualTo(patch.getAddresses().stream().map(EntityMapper::toAddressEntity).toList());
			assertThat(s.getContactInformation()).isEqualTo(patch.getContactInformation().stream().map(EntityMapper::toContactInformationEntity).toList());
			assertThat(s.getExtraParameters()).containsAllEntriesOf(patch.getExtraParameters());
		});
	}

	@Test
	void patchStakeholderPersonTest() {
		final var stakeholder = createStakeholderEntity();
		final var patch = createStakeholder(StakeholderType.PERSON, List.of(StakeholderRole.OPERATOR.name()));

		final var patchedStakeholder = patchStakeholder(stakeholder, patch);

		assertThat(patchedStakeholder).isNotNull().hasNoNullFieldsOrPropertiesExcept("errand").satisfies(s -> {
			assertThat(s.getType()).isEqualTo(patch.getType());
			assertThat(s.getPersonId()).isEqualTo(patch.getPersonId());
			assertThat(s.getFirstName()).isEqualTo(patch.getFirstName());
			assertThat(s.getLastName()).isEqualTo(patch.getLastName());
			assertThat(s.getAdAccount()).isEqualTo(patch.getAdAccount());
			assertThat(s.getRoles()).isEqualTo(patch.getRoles());
			assertThat(s.getAddresses()).isEqualTo(patch.getAddresses().stream().map(EntityMapper::toAddressEntity).toList());
			assertThat(s.getContactInformation()).isEqualTo(patch.getContactInformation().stream().map(EntityMapper::toContactInformationEntity).toList());
			assertThat(s.getExtraParameters()).containsAllEntriesOf(patch.getExtraParameters());
		});
	}

	@Test
	void patchAttachmentTest() {
		final var attachment = createAttachmentEntity();
		final var patch = createAttachment(AttachmentCategory.POLICE_REPORT);

		final var patchedAttachment = patchAttachment(attachment, patch);

		assertThat(patchedAttachment).isNotNull().hasNoNullFieldsOrPropertiesExcept("municipalityId", "namespace").satisfies(a -> {
			assertThat(a.getCategory()).isEqualTo(patch.getCategory());
			assertThat(a.getName()).isEqualTo(patch.getName());
			assertThat(a.getNote()).isEqualTo(patch.getNote());
			assertThat(a.getExtension()).isEqualTo(patch.getExtension());
			assertThat(a.getMimeType()).isEqualTo(patch.getMimeType());
			assertThat(a.getFile()).isEqualTo(patch.getFile());
			assertThat(a.getExtraParameters()).containsAllEntriesOf(patch.getExtraParameters());
		});
	}

	@Test
	void patchNoteTest() {
		final var note = createNoteEntity();
		final var patch = createNote();

		final var patchedNote = patchNote(note, patch);

		assertThat(patchedNote).isNotNull().hasNoNullFieldsOrPropertiesExcept("municipalityId", "namespace").satisfies(n -> {
			assertThat(n.getTitle()).isEqualTo(patch.getTitle());
			assertThat(n.getText()).isEqualTo(patch.getText());
			assertThat(n.getNoteType()).isEqualTo(patch.getNoteType());
			assertThat(n.getCreatedBy()).isEqualTo(patch.getCreatedBy());
			assertThat(n.getUpdatedBy()).isEqualTo(patch.getUpdatedBy());
			assertThat(n.getExtraParameters()).containsAllEntriesOf(patch.getExtraParameters());
		});
	}

	@Test
	void patchFacilityTest() {
		final var facility = createFacilityEntity();
		final var patch = createFacility();

		final var patchedFacility = patchFacility(facility, patch);

		assertThat(patchedFacility).isNotNull().hasNoNullFieldsOrPropertiesExcept("errand", "municipalityId", "namespace").satisfies(f -> {
			assertThat(f.getAddress()).isEqualTo(toAddressEntity(patch.getAddress()));
			assertThat(f.getDescription()).isEqualTo(patch.getDescription());
			assertThat(f.getFacilityCollectionName()).isEqualTo(patch.getFacilityCollectionName());
			assertThat(f.getFacilityType()).isEqualTo(patch.getFacilityType());
			assertThat(f.isMainFacility()).isEqualTo(patch.isMainFacility());
			assertThat(f.getExtraParameters()).containsAllEntriesOf(patch.getExtraParameters());
		});
	}

	@Test
	void patchNotificationTest() {
		final var notificationEntity = createNotificationEntity(null);
		final var patchNotification = createPatchNotification(null);

		final var patchedNotificationEntity = PatchMapper.patchNotification(notificationEntity, patchNotification);

		assertThat(patchedNotificationEntity).isNotNull().satisfies(patchedEntity -> {
			assertThat(patchedEntity.getContent()).isEqualTo(patchNotification.getContent());
			assertThat(patchedEntity.getDescription()).isEqualTo(patchNotification.getDescription());
			assertThat(patchedEntity.getExpires()).isEqualTo(patchNotification.getExpires());
			assertThat(patchedEntity.getOwnerId()).isEqualTo(patchNotification.getOwnerId());
			assertThat(patchedEntity.getType()).isEqualTo(patchNotification.getType());
			assertThat(patchedEntity.isAcknowledged()).isEqualTo(patchNotification.getAcknowledged());
			assertThat(patchedEntity.isGlobalAcknowledged()).isEqualTo(patchNotification.getGlobalAcknowledged());
		});
	}
}
