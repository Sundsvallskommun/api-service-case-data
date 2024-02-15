package se.sundsvall.casedata.service.util.mappers;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.casedata.TestUtil.createAttachment;
import static se.sundsvall.casedata.TestUtil.createAttachmentDTO;
import static se.sundsvall.casedata.TestUtil.createDecision;
import static se.sundsvall.casedata.TestUtil.createErrand;
import static se.sundsvall.casedata.TestUtil.createNote;
import static se.sundsvall.casedata.TestUtil.createNoteDTO;
import static se.sundsvall.casedata.TestUtil.createPatchDecisionDto;
import static se.sundsvall.casedata.TestUtil.createPatchErrandDto;
import static se.sundsvall.casedata.TestUtil.createStakeholder;
import static se.sundsvall.casedata.TestUtil.createStakeholderDTO;
import static se.sundsvall.casedata.service.util.mappers.PatchMapper.patchAttachment;
import static se.sundsvall.casedata.service.util.mappers.PatchMapper.patchDecision;
import static se.sundsvall.casedata.service.util.mappers.PatchMapper.patchErrand;
import static se.sundsvall.casedata.service.util.mappers.PatchMapper.patchNote;
import static se.sundsvall.casedata.service.util.mappers.PatchMapper.patchStakeholder;

import java.util.List;

import org.junit.jupiter.api.Test;

import se.sundsvall.casedata.integration.db.model.enums.AttachmentCategory;
import se.sundsvall.casedata.integration.db.model.enums.StakeholderRole;
import se.sundsvall.casedata.integration.db.model.enums.StakeholderType;

class PatchMapperTest {

	@Test
	void patchErrandTest() {
		final var errand = createErrand();
		final var patch = createPatchErrandDto();

		var patchedErrand = patchErrand(errand, patch);

		assertThat(patchedErrand).isNotNull().satisfies(e -> {
			assertThat(e.getCaseType()).isEqualTo(patch.getCaseType());
			assertThat(e.getDescription()).isEqualTo(patch.getDescription());
			assertThat(e.getExternalCaseId()).isEqualTo(patch.getExternalCaseId());
			assertThat(e.getPriority()).isEqualTo(patch.getPriority());
			assertThat(e.getCaseTitleAddition()).isEqualTo(patch.getCaseTitleAddition());
			assertThat(e.getDiaryNumber()).isEqualTo(patch.getDiaryNumber());
			assertThat(e.getPhase()).isEqualTo(patch.getPhase());
			assertThat(e.getMunicipalityId()).isEqualTo(patch.getMunicipalityId());
			assertThat(e.getStartDate()).isEqualTo(patch.getStartDate());
			assertThat(e.getEndDate()).isEqualTo(patch.getEndDate());
			assertThat(e.getApplicationReceived()).isEqualTo(patch.getApplicationReceived());
			assertThat(e.getExtraParameters()).containsAllEntriesOf(patch.getExtraParameters());
		});
	}

	@Test
	void patchDecisionTest() {
		final var decision = createDecision();
		final var patch = createPatchDecisionDto();

		var patchedDecision = patchDecision(decision, patch);

		assertThat(patchedDecision).isNotNull().satisfies(d -> {
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
		final var stakeholder = createStakeholder();
		final var patch = createStakeholderDTO(StakeholderType.ORGANIZATION, List.of(StakeholderRole.OPERATOR));

		var patchedStakeholder = patchStakeholder(stakeholder, patch);

		assertThat(patchedStakeholder).isNotNull().satisfies(s -> {
			assertThat(s.getType()).isEqualTo(patch.getType());
			assertThat(s.getOrganizationName()).isEqualTo(patch.getOrganizationName());
			assertThat(s.getOrganizationNumber()).isEqualTo(patch.getOrganizationNumber());
			assertThat(s.getAuthorizedSignatory()).isEqualTo(patch.getAuthorizedSignatory());
			assertThat(s.getAdAccount()).isEqualTo(patch.getAdAccount());
			assertThat(s.getRoles()).isEqualTo(patch.getRoles());
			assertThat(s.getAddresses()).isEqualTo(patch.getAddresses().stream().map(EntityMapper::toAddress).toList());
			assertThat(s.getContactInformation()).isEqualTo(patch.getContactInformation().stream().map(EntityMapper::toContactInformation).toList());
			assertThat(s.getExtraParameters()).containsAllEntriesOf(patch.getExtraParameters());
		});
	}

	@Test
	void patchStakeholderPersonTest() {
		final var stakeholder = createStakeholder();
		final var patch = createStakeholderDTO(StakeholderType.PERSON, List.of(StakeholderRole.OPERATOR));

		var patchedStakeholder = patchStakeholder(stakeholder, patch);

		assertThat(patchedStakeholder).isNotNull().satisfies(s -> {
			assertThat(s.getType()).isEqualTo(patch.getType());
			assertThat(s.getPersonId()).isEqualTo(patch.getPersonId());
			assertThat(s.getFirstName()).isEqualTo(patch.getFirstName());
			assertThat(s.getLastName()).isEqualTo(patch.getLastName());
			assertThat(s.getAdAccount()).isEqualTo(patch.getAdAccount());
			assertThat(s.getRoles()).isEqualTo(patch.getRoles());
			assertThat(s.getAddresses()).isEqualTo(patch.getAddresses().stream().map(EntityMapper::toAddress).toList());
			assertThat(s.getContactInformation()).isEqualTo(patch.getContactInformation().stream().map(EntityMapper::toContactInformation).toList());
			assertThat(s.getExtraParameters()).containsAllEntriesOf(patch.getExtraParameters());
		});
	}

	@Test
	void patchAttachmentTest() {
		final var attachment = createAttachment();
		final var patch = createAttachmentDTO(AttachmentCategory.POLICE_REPORT);

		var patchedAttachment = patchAttachment(attachment, patch);

		assertThat(patchedAttachment).isNotNull().satisfies(a -> {
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
		final var note = createNote();
		final var patch = createNoteDTO();

		var patchedNote = patchNote(note, patch);

		assertThat(patchedNote).isNotNull().satisfies(n -> {
			assertThat(n.getTitle()).isEqualTo(patch.getTitle());
			assertThat(n.getText()).isEqualTo(patch.getText());
			assertThat(n.getNoteType()).isEqualTo(patch.getNoteType());
			assertThat(n.getCreatedBy()).isEqualTo(patch.getCreatedBy());
			assertThat(n.getUpdatedBy()).isEqualTo(patch.getUpdatedBy());
			assertThat(n.getExtraParameters()).containsAllEntriesOf(patch.getExtraParameters());
		});
	}
}
