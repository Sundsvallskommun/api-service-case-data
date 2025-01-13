package se.sundsvall.casedata.service.util.mappers;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.casedata.TestUtil.createAttachment;
import static se.sundsvall.casedata.TestUtil.createAttachmentEntity;
import static se.sundsvall.casedata.TestUtil.createDecision;
import static se.sundsvall.casedata.TestUtil.createDecisionEntity;
import static se.sundsvall.casedata.TestUtil.createNote;
import static se.sundsvall.casedata.TestUtil.createNoteEntity;
import static se.sundsvall.casedata.TestUtil.createStakeholder;
import static se.sundsvall.casedata.TestUtil.createStakeholderEntity;
import static se.sundsvall.casedata.service.util.mappers.PutMapper.putAttachment;
import static se.sundsvall.casedata.service.util.mappers.PutMapper.putDecision;
import static se.sundsvall.casedata.service.util.mappers.PutMapper.putNote;
import static se.sundsvall.casedata.service.util.mappers.PutMapper.putStakeholder;

import java.util.List;
import org.junit.jupiter.api.Test;
import se.sundsvall.casedata.api.model.validation.enums.AttachmentCategory;
import se.sundsvall.casedata.api.model.validation.enums.StakeholderRole;
import se.sundsvall.casedata.integration.db.model.enums.StakeholderType;

class PutMapperTest {

	@Test
	void putAttachmentTest() {
		final var attachment = createAttachmentEntity();
		final var attachmentDTO = createAttachment(AttachmentCategory.RECEIVED_CONTRACT);

		assertThat(attachment).satisfies(a -> {
			assertThat(a.getCategory()).isNotEqualTo(attachmentDTO.getCategory());
			assertThat(a.getName()).isNotEqualTo(attachmentDTO.getName());
			assertThat(a.getNote()).isNotEqualTo(attachmentDTO.getNote());
			assertThat(a.getExtension()).isNotEqualTo(attachmentDTO.getExtension());
			assertThat(a.getMimeType()).isNotEqualTo(attachmentDTO.getMimeType());
			assertThat(a.getFile()).isNotEqualTo(attachmentDTO.getFile());
		});

		putAttachment(attachment, attachmentDTO);

		assertThat(attachment).satisfies(a -> {
			assertThat(a.getCategory()).isEqualTo(attachmentDTO.getCategory());
			assertThat(a.getName()).isEqualTo(attachmentDTO.getName());
			assertThat(a.getNote()).isEqualTo(attachmentDTO.getNote());
			assertThat(a.getExtension()).isEqualTo(attachmentDTO.getExtension());
			assertThat(a.getMimeType()).isEqualTo(attachmentDTO.getMimeType());
			assertThat(a.getFile()).isEqualTo(attachmentDTO.getFile());
			assertThat(a.getExtraParameters()).containsAllEntriesOf(attachmentDTO.getExtraParameters());
		});
	}

	@Test
	void putStakeholderTest() {
		final var stakeholder = createStakeholderEntity();
		final var stakeholderDTO = createStakeholder(StakeholderType.ORGANIZATION, List.of(StakeholderRole.PROPERTY_OWNER.name()));

		assertThat(stakeholder).satisfies(s -> {
			assertThat(s.getType()).isNotEqualTo(stakeholderDTO.getType());
			assertThat(s.getFirstName()).isNotEqualTo(stakeholderDTO.getFirstName());
			assertThat(s.getLastName()).isNotEqualTo(stakeholderDTO.getLastName());
			assertThat(s.getPersonId()).isNotEqualTo(stakeholderDTO.getPersonId());
			assertThat(s.getOrganizationName()).isNotEqualTo(stakeholderDTO.getOrganizationName());
			assertThat(s.getOrganizationNumber()).isNotEqualTo(stakeholderDTO.getOrganizationNumber());
			assertThat(s.getAuthorizedSignatory()).isNotEqualTo(stakeholderDTO.getAuthorizedSignatory());
			assertThat(s.getAdAccount()).isNotEqualTo(stakeholderDTO.getAdAccount());
			assertThat(s.getRoles()).isNotEqualTo(stakeholderDTO.getRoles());
		});

		putStakeholder(stakeholder, stakeholderDTO);

		assertThat(stakeholder).satisfies(s -> {
			assertThat(s.getType()).isEqualTo(stakeholderDTO.getType());
			assertThat(s.getFirstName()).isEqualTo(stakeholderDTO.getFirstName());
			assertThat(s.getLastName()).isEqualTo(stakeholderDTO.getLastName());
			assertThat(s.getPersonId()).isEqualTo(stakeholderDTO.getPersonId());
			assertThat(s.getOrganizationName()).isEqualTo(stakeholderDTO.getOrganizationName());
			assertThat(s.getOrganizationNumber()).isEqualTo(stakeholderDTO.getOrganizationNumber());
			assertThat(s.getAuthorizedSignatory()).isEqualTo(stakeholderDTO.getAuthorizedSignatory());
			assertThat(s.getAdAccount()).isEqualTo(stakeholderDTO.getAdAccount());
			assertThat(s.getRoles()).isEqualTo(stakeholderDTO.getRoles());
			assertThat(s.getExtraParameters()).containsAllEntriesOf(stakeholderDTO.getExtraParameters());
			assertThat(s.getAddresses()).isEqualTo(stakeholderDTO.getAddresses().stream().map(EntityMapper::toAddressEntity).toList());
			assertThat(s.getContactInformation()).isEqualTo(stakeholderDTO.getContactInformation().stream().map(EntityMapper::toContactInformationEntity).toList());
		});
	}

	@Test
	void putDecisionTest() {
		final var decision = createDecisionEntity();
		final var decisionDto = createDecision();

		assertThat(decision).satisfies(d -> {
			assertThat(d.getDecisionType()).isNotEqualTo(decisionDto.getDecisionType());
			assertThat(d.getDecisionOutcome()).isNotEqualTo(decisionDto.getDecisionOutcome());
			assertThat(d.getDescription()).isNotEqualTo(decisionDto.getDescription());
		});

		putDecision(decision, decisionDto);

		assertThat(decision).satisfies(d -> {
			assertThat(d.getDecisionType()).isEqualTo(decisionDto.getDecisionType());
			assertThat(d.getDecisionOutcome()).isEqualTo(decisionDto.getDecisionOutcome());
			assertThat(d.getDescription()).isEqualTo(decisionDto.getDescription());
			assertThat(d.getDecidedAt()).isEqualTo(decisionDto.getDecidedAt());
			assertThat(d.getValidFrom()).isEqualTo(decisionDto.getValidFrom());
			assertThat(d.getValidTo()).isEqualTo(decisionDto.getValidTo());
			assertThat(d.getExtraParameters()).containsAllEntriesOf(decisionDto.getExtraParameters());
		});
	}

	@Test
	void putNoteTest() {
		final var note = createNoteEntity();
		final var noteDto = createNote();

		assertThat(note).satisfies(n -> {
			assertThat(n.getTitle()).isNotEqualTo(noteDto.getTitle());
			assertThat(n.getText()).isNotEqualTo(noteDto.getText());
			assertThat(n.getCreatedBy()).isNotEqualTo(noteDto.getCreatedBy());
			assertThat(n.getUpdatedBy()).isNotEqualTo(noteDto.getUpdatedBy());
		});

		putNote(note, noteDto);

		assertThat(note).satisfies(n -> {
			assertThat(n.getTitle()).isEqualTo(noteDto.getTitle());
			assertThat(n.getText()).isEqualTo(noteDto.getText());
			assertThat(n.getCreatedBy()).isEqualTo(noteDto.getCreatedBy());
			assertThat(n.getUpdatedBy()).isEqualTo(noteDto.getUpdatedBy());
			assertThat(n.getExtraParameters()).containsAllEntriesOf(noteDto.getExtraParameters());
		});
	}

}
