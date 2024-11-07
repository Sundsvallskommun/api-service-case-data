package se.sundsvall.casedata.service.util.mappers;

import generated.se.sundsvall.employee.PortalPersonData;
import org.junit.jupiter.api.Test;
import se.sundsvall.casedata.api.model.ExtraParameter;
import se.sundsvall.casedata.api.model.PatchErrand;
import se.sundsvall.casedata.api.model.validation.enums.AttachmentCategory;
import se.sundsvall.casedata.api.model.validation.enums.StakeholderRole;
import se.sundsvall.casedata.integration.db.model.ErrandEntity;
import se.sundsvall.casedata.integration.db.model.ExtraParameterEntity;
import se.sundsvall.casedata.integration.db.model.enums.StakeholderType;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static se.sundsvall.casedata.TestUtil.*;
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
			assertThat(e.getCaseType()).isEqualTo(patch.getCaseType().name());
			assertThat(e.getDescription()).isEqualTo(patch.getDescription());
			assertThat(e.getExternalCaseId()).isEqualTo(patch.getExternalCaseId());
			assertThat(e.getPriority()).isEqualTo(patch.getPriority());
			assertThat(e.getCaseTitleAddition()).isEqualTo(patch.getCaseTitleAddition());
			assertThat(e.getDiaryNumber()).isEqualTo(patch.getDiaryNumber());
			assertThat(e.getPhase()).isEqualTo(patch.getPhase());
			assertThat(e.getStartDate()).isEqualTo(patch.getStartDate());
			assertThat(e.getEndDate()).isEqualTo(patch.getEndDate());
			assertThat(e.getApplicationReceived()).isEqualTo(patch.getApplicationReceived());
			assertThat(e.getExtraParameters()).hasSize(5).containsAll(patch.getExtraParameters().stream().map(parameter -> ErrandExtraParameterMapper.toErrandParameterEntity(parameter).withErrandEntity(errand)).toList());
			assertThat(e.getSuspendedFrom()).isEqualTo(patch.getSuspension().getSuspendedFrom());
			assertThat(e.getSuspendedTo()).isEqualTo(patch.getSuspension().getSuspendedTo());
			assertThat(e.getFacilities()).hasSize(2).containsAll(patch.getFacilities().stream().map(facilityDTO -> toFacilityEntity(facilityDTO, MUNICIPALITY_ID, NAMESPACE)).toList());
		});
	}

	@Test
	void patchErrandExtraParameterTest() {
		final var errand = ErrandEntity.builder().withId(1L).withExtraParameters(createExtraParameterEntityList()).build();

		final var newExtraParameter = (ExtraParameter.builder().withKey("key3").withValues(List.of("newValue3")).build());
		final var updateExtraParameter = (ExtraParameter.builder().withKey("key1").withValues(List.of("newValueOfKey1")).build());
		final var patch = PatchErrand.builder().withExtraParameters(List.of(newExtraParameter, updateExtraParameter)).build();

		final var patchedErrand = patchErrand(errand, patch);

		assertThat(patchedErrand.getExtraParameters()).hasSize(3).extracting(ExtraParameterEntity::getKey, ExtraParameterEntity::getValues).containsExactlyInAnyOrder(
			tuple("key1", List.of("newValueOfKey1")),
					tuple("key2", null),
					tuple("key3", List.of("newValue3")));
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
			assertThat(f.getAddressEntity()).isEqualTo(toAddressEntity(patch.getAddress()));
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
		final var fullName = "Test Testsson";
		final var owner = new PortalPersonData().fullname(fullName);

		final var patchedNotificationEntity = PatchMapper.patchNotification(notificationEntity, patchNotification, owner);

		assertThat(patchedNotificationEntity).isNotNull().satisfies(patchedEntity -> {
			assertThat(patchedEntity.getContent()).isEqualTo(patchNotification.getContent());
			assertThat(patchedEntity.getDescription()).isEqualTo(patchNotification.getDescription());
			assertThat(patchedEntity.getExpires()).isEqualTo(patchNotification.getExpires());
			assertThat(patchedEntity.getOwnerFullName()).isEqualTo(fullName);
			assertThat(patchedEntity.getOwnerId()).isEqualTo(patchNotification.getOwnerId());
			assertThat(patchedEntity.getType()).isEqualTo(patchNotification.getType());
			assertThat(patchedEntity.isAcknowledged()).isEqualTo(patchNotification.getAcknowledged());
		});
	}
}
