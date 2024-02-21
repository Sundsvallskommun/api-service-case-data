package se.sundsvall.casedata.service.util.mappers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.casedata.api.model.enums.AttachmentCategory;
import se.sundsvall.casedata.api.model.enums.FacilityType;
import se.sundsvall.casedata.api.model.enums.StakeholderRole;
import se.sundsvall.casedata.integration.db.model.enums.AddressCategory;
import se.sundsvall.casedata.integration.db.model.enums.ContactType;
import se.sundsvall.casedata.integration.db.model.enums.StakeholderType;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.casedata.TestUtil.createAddress;
import static se.sundsvall.casedata.TestUtil.createAddressDTO;
import static se.sundsvall.casedata.TestUtil.createAppeal;
import static se.sundsvall.casedata.TestUtil.createAppealDTO;
import static se.sundsvall.casedata.TestUtil.createAttachment;
import static se.sundsvall.casedata.TestUtil.createAttachmentDTO;
import static se.sundsvall.casedata.TestUtil.createContactInformation;
import static se.sundsvall.casedata.TestUtil.createContactInformationDTO;
import static se.sundsvall.casedata.TestUtil.createCoordinates;
import static se.sundsvall.casedata.TestUtil.createCoordinatesDTO;
import static se.sundsvall.casedata.TestUtil.createDecision;
import static se.sundsvall.casedata.TestUtil.createDecisionDTO;
import static se.sundsvall.casedata.TestUtil.createErrand;
import static se.sundsvall.casedata.TestUtil.createErrandDTO;
import static se.sundsvall.casedata.TestUtil.createFacility;
import static se.sundsvall.casedata.TestUtil.createFacilityDTO;
import static se.sundsvall.casedata.TestUtil.createLaw;
import static se.sundsvall.casedata.TestUtil.createLawDTO;
import static se.sundsvall.casedata.TestUtil.createNote;
import static se.sundsvall.casedata.TestUtil.createNoteDTO;
import static se.sundsvall.casedata.TestUtil.createStakeholder;
import static se.sundsvall.casedata.TestUtil.createStakeholderDTO;
import static se.sundsvall.casedata.TestUtil.createStatus;
import static se.sundsvall.casedata.TestUtil.createStatusDTO;

@ExtendWith(MockitoExtension.class)
class EntityMapperTest {

	@Test
	void toErrandTest() {
		var errandDto = createErrandDTO();
		var errand = EntityMapper.toErrand(errandDto);

		assertThat(errand).satisfies(e -> {
			assertThat(e.getErrandNumber()).isEqualTo(errandDto.getErrandNumber());
			assertThat(e.getUpdatedByClient()).isEqualTo(errandDto.getUpdatedByClient());
			assertThat(e.getUpdatedBy()).isEqualTo(errandDto.getUpdatedBy());
			assertThat(e.getProcessId()).isEqualTo(errandDto.getProcessId());
			assertThat(e.getCreated()).isEqualTo(errandDto.getCreated());
			assertThat(e.getCreatedByClient()).isEqualTo(errandDto.getCreatedByClient());
			assertThat(e.getCaseTitleAddition()).isEqualTo(errandDto.getCaseTitleAddition());
			assertThat(e.getDescription()).isEqualTo(errandDto.getDescription());
		});
	}

	@Test
	void toErrandDtoTest() {
		var errand = createErrand();
		var errandDto = EntityMapper.toErrandDto(errand);

		assertThat(errandDto).satisfies(e -> {
			assertThat(e.getErrandNumber()).isEqualTo(errand.getErrandNumber());
			assertThat(e.getUpdatedByClient()).isEqualTo(errand.getUpdatedByClient());
			assertThat(e.getUpdatedBy()).isEqualTo(errand.getUpdatedBy());
			assertThat(e.getProcessId()).isEqualTo(errand.getProcessId());
			assertThat(e.getCreated()).isEqualTo(errand.getCreated());
			assertThat(e.getCreatedByClient()).isEqualTo(errand.getCreatedByClient());
			assertThat(e.getCaseTitleAddition()).isEqualTo(errand.getCaseTitleAddition());
			assertThat(e.getDescription()).isEqualTo(errand.getDescription());
		});
	}

	@Test
	void toDecisionTest() {
		var decisionDto = createDecisionDTO();
		var decision = EntityMapper.toDecision(decisionDto);

		assertThat(decision).satisfies(d -> {
			assertThat(d.getDescription()).isEqualTo(decisionDto.getDescription());
			assertThat(d.getDecidedAt()).isEqualTo(decisionDto.getDecidedAt());
			assertThat(d.getUpdated()).isEqualTo(decisionDto.getUpdated());
			assertThat(d.getValidFrom()).isEqualTo(decisionDto.getValidFrom());
			assertThat(d.getValidTo()).isEqualTo(decisionDto.getValidTo());
			assertThat(d.getDecisionType()).isEqualTo(decisionDto.getDecisionType());
			assertThat(d.getDecisionOutcome()).isEqualTo(decisionDto.getDecisionOutcome());
			assertThat(d.getVersion()).isEqualTo(decisionDto.getVersion());
			assertThat(d.getId()).isEqualTo(decisionDto.getId());
		});
	}

	@Test
	void toDecisionDtoTest() {
		var decision = createDecision();
		var decisionDto = EntityMapper.toDecisionDto(decision);

		assertThat(decisionDto).satisfies(d -> {
			assertThat(d.getDescription()).isEqualTo(decision.getDescription());
			assertThat(d.getDecidedAt()).isEqualTo(decision.getDecidedAt());
			assertThat(d.getUpdated()).isEqualTo(decision.getUpdated());
			assertThat(d.getValidFrom()).isEqualTo(decision.getValidFrom());
			assertThat(d.getValidTo()).isEqualTo(decision.getValidTo());
			assertThat(d.getDecisionType()).isEqualTo(decision.getDecisionType());
			assertThat(d.getDecisionOutcome()).isEqualTo(decision.getDecisionOutcome());
			assertThat(d.getVersion()).isEqualTo(decision.getVersion());
			assertThat(d.getId()).isEqualTo(decision.getId());
		});
	}

	@Test
	void toNoteTest() {
		var noteDto = createNoteDTO();
		var note = EntityMapper.toNote(noteDto);

		assertThat(note).satisfies(n -> {
			assertThat(n.getText()).isEqualTo(noteDto.getText());
			assertThat(n.getNoteType()).isEqualTo(noteDto.getNoteType());
			assertThat(n.getExtraParameters()).isEqualTo(noteDto.getExtraParameters());
			assertThat(n.getTitle()).isEqualTo(noteDto.getTitle());
			assertThat(n.getUpdatedBy()).isEqualTo(noteDto.getUpdatedBy());
			assertThat(n.getCreatedBy()).isEqualTo(noteDto.getCreatedBy());
			assertThat(n.getUpdated()).isEqualTo(noteDto.getUpdated());
			assertThat(n.getCreated()).isEqualTo(noteDto.getCreated());
			assertThat(n.getVersion()).isEqualTo(noteDto.getVersion());
			assertThat(n.getId()).isEqualTo(noteDto.getId());
		});
	}

	@Test
	void toNoteDtoTest() {
		var note = createNote();
		var noteDto = EntityMapper.toNoteDto(note);

		assertThat(noteDto).satisfies(n -> {
			assertThat(n.getText()).isEqualTo(note.getText());
			assertThat(n.getNoteType()).isEqualTo(note.getNoteType());
			assertThat(n.getExtraParameters()).isEqualTo(note.getExtraParameters());
			assertThat(n.getTitle()).isEqualTo(note.getTitle());
			assertThat(n.getUpdatedBy()).isEqualTo(note.getUpdatedBy());
			assertThat(n.getCreatedBy()).isEqualTo(note.getCreatedBy());
			assertThat(n.getUpdated()).isEqualTo(note.getUpdated());
			assertThat(n.getCreated()).isEqualTo(note.getCreated());
			assertThat(n.getVersion()).isEqualTo(note.getVersion());
			assertThat(n.getId()).isEqualTo(note.getId());
		});
	}

	@Test
	void toFacilityTest() {
		var facilityDto = createFacilityDTO();
		var facility = EntityMapper.toFacility(facilityDto);

		assertThat(facility).satisfies(f -> {
			assertThat(f.getFacilityType()).isEqualTo(facilityDto.getFacilityType().name());
			assertThat(f.getUpdated()).isEqualTo(facilityDto.getUpdated());
			assertThat(f.getCreated()).isEqualTo(facilityDto.getCreated());
			assertThat(f.getDescription()).isEqualTo(facilityDto.getDescription());
			assertThat(f.getExtraParameters()).isEqualTo(facilityDto.getExtraParameters());
			assertThat(f.getId()).isEqualTo(facilityDto.getId());
			assertThat(f.getVersion()).isEqualTo(facilityDto.getVersion());
			assertThat(f.getFacilityCollectionName()).isEqualTo(facilityDto.getFacilityCollectionName());
		});
	}

	@Test
	void toFacilityDtoTest() {
		var facility = createFacility();
		var facilityDto = EntityMapper.toFacilityDto(facility);

		assertThat(facilityDto).satisfies(f -> {
			assertThat(f.getDescription()).isEqualTo(facility.getDescription());
			assertThat(f.getFacilityType()).isEqualTo(FacilityType.valueOf(facility.getFacilityType()));
			assertThat(f.getUpdated()).isEqualTo(facility.getUpdated());
			assertThat(f.getCreated()).isEqualTo(facility.getCreated());
			assertThat(f.getExtraParameters()).isEqualTo(facility.getExtraParameters());
			assertThat(f.getId()).isEqualTo(facility.getId());
			assertThat(f.getVersion()).isEqualTo(facility.getVersion());
			assertThat(f.getFacilityCollectionName()).isEqualTo(facility.getFacilityCollectionName());
		});
	}

	@Test
	void toStakeholderTest() {
		var stakeholderDto = createStakeholderDTO(StakeholderType.ORGANIZATION, List.of(StakeholderRole.APPLICANT));
		var stakeholder = EntityMapper.toStakeholder(stakeholderDto);

		assertThat(stakeholder).satisfies(s -> {
			assertThat(s.getAdAccount()).isEqualTo(stakeholderDto.getAdAccount());
			assertThat(s.getAuthorizedSignatory()).isEqualTo(stakeholderDto.getAuthorizedSignatory());
			assertThat(s.getCreated()).isEqualTo(stakeholderDto.getCreated());
			assertThat(s.getUpdated()).isEqualTo(stakeholderDto.getUpdated());
			assertThat(s.getFirstName()).isEqualTo(stakeholderDto.getFirstName());
			assertThat(s.getLastName()).isEqualTo(stakeholderDto.getLastName());
			assertThat(s.getOrganizationName()).isEqualTo(stakeholderDto.getOrganizationName());
			assertThat(s.getOrganizationNumber()).isEqualTo(stakeholderDto.getOrganizationNumber());
			assertThat(s.getPersonId()).isEqualTo(stakeholderDto.getPersonId());
			assertThat(s.getRoles()).isEqualTo(stakeholderDto.getRoles().stream().map(StakeholderRole::name).toList());
			assertThat(s.getId()).isEqualTo(stakeholderDto.getId());
			assertThat(s.getVersion()).isEqualTo(stakeholderDto.getVersion());
		});
	}

	@Test
	void toStakeholderDtoTest() {
		var stakeholder = createStakeholder();
		var stakeholderDto = EntityMapper.toStakeholderDto(stakeholder);

		assertThat(stakeholderDto).satisfies(s -> {
			assertThat(s.getAdAccount()).isEqualTo(stakeholder.getAdAccount());
			assertThat(s.getAuthorizedSignatory()).isEqualTo(stakeholder.getAuthorizedSignatory());
			assertThat(s.getCreated()).isEqualTo(stakeholder.getCreated());
			assertThat(s.getUpdated()).isEqualTo(stakeholder.getUpdated());
			assertThat(s.getFirstName()).isEqualTo(stakeholder.getFirstName());
			assertThat(s.getLastName()).isEqualTo(stakeholder.getLastName());
			assertThat(s.getOrganizationName()).isEqualTo(stakeholder.getOrganizationName());
			assertThat(s.getOrganizationNumber()).isEqualTo(stakeholder.getOrganizationNumber());
			assertThat(s.getPersonId()).isEqualTo(stakeholder.getPersonId());
			assertThat(s.getRoles()).isEqualTo(stakeholder.getRoles().stream().map(StakeholderRole::valueOf).toList());
			assertThat(s.getId()).isEqualTo(stakeholder.getId());
			assertThat(s.getVersion()).isEqualTo(stakeholder.getVersion());
		});
	}

	@Test
	void toAttachmentTest() {
		var attachmentDto = createAttachmentDTO(AttachmentCategory.POLICE_REPORT);
		var attachment = EntityMapper.toAttachment(attachmentDto);

		assertThat(attachment).satisfies(a -> {
			assertThat(a.getCategory()).isEqualTo(attachmentDto.getCategory().name());
			assertThat(a.getCreated()).isEqualTo(attachmentDto.getCreated());
			assertThat(a.getUpdated()).isEqualTo(attachmentDto.getUpdated());
			assertThat(a.getId()).isEqualTo(attachmentDto.getId());
			assertThat(a.getVersion()).isEqualTo(attachmentDto.getVersion());
		});
	}

	@Test
	void toAttachmentDtoTest() {
		var attachment = createAttachment();
		var attachmentDto = EntityMapper.toAttachmentDto(attachment);

		assertThat(attachmentDto).satisfies(a -> {
			assertThat(a.getCategory()).isEqualTo(AttachmentCategory.valueOf(attachment.getCategory()));
			assertThat(a.getCreated()).isEqualTo(attachment.getCreated());
			assertThat(a.getUpdated()).isEqualTo(attachment.getUpdated());
			assertThat(a.getId()).isEqualTo(attachment.getId());
			assertThat(a.getVersion()).isEqualTo(attachment.getVersion());
		});
	}

	@Test
	void toAppealTest() {
		var appeal = createAppealDTO();
		var appealDto = EntityMapper.toAppeal(appeal);

		assertThat(appealDto).satisfies(a -> {
			assertThat(a.getCreated()).isEqualTo(appeal.getCreated());
			assertThat(a.getUpdated()).isEqualTo(appeal.getUpdated());
			assertThat(a.getId()).isEqualTo(appeal.getId());
			assertThat(a.getVersion()).isEqualTo(appeal.getVersion());
			assertThat(a.getExtraParameters()).isEqualTo(appeal.getExtraParameters());
		});
	}

	@Test
	void toAppealDtoTest() {
		var appeal = createAppeal();
		var appealDto = EntityMapper.toAppealDto(appeal);

		assertThat(appealDto).satisfies(a -> {
			assertThat(a.getCreated()).isEqualTo(appeal.getCreated());
			assertThat(a.getUpdated()).isEqualTo(appeal.getUpdated());
			assertThat(a.getId()).isEqualTo(appeal.getId());
			assertThat(a.getVersion()).isEqualTo(appeal.getVersion());
			assertThat(a.getExtraParameters()).isEqualTo(appeal.getExtraParameters());
		});
	}

	@Test
	void toStatusTest() {
		var statusDto = createStatusDTO();
		var status = EntityMapper.toStatus(statusDto);

		assertThat(status).satisfies(s -> {
			assertThat(s.getDateTime()).isEqualTo(statusDto.getDateTime());
			assertThat(s.getStatusType()).isEqualTo(statusDto.getStatusType());
			assertThat(s.getDescription()).isEqualTo(statusDto.getDescription());
		});
	}

	@Test
	void toStatusDtoTest() {
		var status = createStatus();
		var statusDto = EntityMapper.toStatusDto(status);

		assertThat(statusDto).satisfies(s -> {
			assertThat(s.getDateTime()).isEqualTo(status.getDateTime());
			assertThat(s.getStatusType()).isEqualTo(status.getStatusType());
			assertThat(s.getDescription()).isEqualTo(status.getDescription());
		});
	}

	@Test
	void toCoordinatesTest() {
		var coordinatesDto = createCoordinatesDTO();
		var coordinates = EntityMapper.toCoordinates(coordinatesDto);

		assertThat(coordinates).satisfies(c -> {
			assertThat(c.getLatitude()).isEqualTo(coordinatesDto.getLatitude());
			assertThat(c.getLongitude()).isEqualTo(coordinatesDto.getLongitude());
		});
	}

	@Test
	void toCoordinatesDtoTest() {
		var coordinates = createCoordinates();
		var coordinatesDto = EntityMapper.toCoordinatesDto(coordinates);

		assertThat(coordinatesDto).satisfies(c -> {
			assertThat(c.getLatitude()).isEqualTo(coordinates.getLatitude());
			assertThat(c.getLongitude()).isEqualTo(coordinates.getLongitude());
		});
	}

	@Test
	void toAddressTest() {
		var addressDto = createAddressDTO(AddressCategory.VISITING_ADDRESS);
		var address = EntityMapper.toAddress(addressDto);

		assertThat(address).satisfies(a -> {
			assertThat(a.getStreet()).isEqualTo(addressDto.getStreet());
			assertThat(a.getCity()).isEqualTo(addressDto.getCity());
			assertThat(a.getCountry()).isEqualTo(addressDto.getCountry());
			assertThat(a.getAttention()).isEqualTo(addressDto.getAttention());
			assertThat(a.getApartmentNumber()).isEqualTo(addressDto.getApartmentNumber());
			assertThat(a.getCountry()).isEqualTo(addressDto.getCountry());
			assertThat(a.getInvoiceMarking()).isEqualTo(addressDto.getInvoiceMarking());
			assertThat(a.getPostalCode()).isEqualTo(addressDto.getPostalCode());
			assertThat(a.getIsZoningPlanArea()).isEqualTo(addressDto.getIsZoningPlanArea());
			assertThat(a.getPropertyDesignation()).isEqualTo(addressDto.getPropertyDesignation());
		});
	}

	@Test
	void toAddressDtoTest() {
		var address = createAddress();
		var addressDto = EntityMapper.toAddressDto(address);

		assertThat(addressDto).satisfies(a -> {
			assertThat(a.getStreet()).isEqualTo(address.getStreet());
			assertThat(a.getCity()).isEqualTo(address.getCity());
			assertThat(a.getCountry()).isEqualTo(address.getCountry());
			assertThat(a.getAttention()).isEqualTo(address.getAttention());
			assertThat(a.getApartmentNumber()).isEqualTo(address.getApartmentNumber());
			assertThat(a.getCountry()).isEqualTo(address.getCountry());
			assertThat(a.getInvoiceMarking()).isEqualTo(address.getInvoiceMarking());
			assertThat(a.getPostalCode()).isEqualTo(address.getPostalCode());
			assertThat(a.getIsZoningPlanArea()).isEqualTo(address.getIsZoningPlanArea());
			assertThat(a.getPropertyDesignation()).isEqualTo(address.getPropertyDesignation());
		});
	}

	@Test
	void toContactInformationTest() {
		var contactInformationDto = createContactInformationDTO(ContactType.EMAIL);
		var contactInformation = EntityMapper.toContactInformation(contactInformationDto);

		assertThat(contactInformation).satisfies(c -> {
			assertThat(c.getContactType()).isEqualTo(contactInformationDto.getContactType());
			assertThat(c.getValue()).isEqualTo(contactInformationDto.getValue());
		});
	}

	@Test
	void toContactInformationDtoTest() {
		var contactInformation = createContactInformation();
		var contactInformationDto = EntityMapper.toContactInformationDto(contactInformation);

		assertThat(contactInformationDto).satisfies(c -> {
			assertThat(c.getContactType()).isEqualTo(contactInformation.getContactType());
			assertThat(c.getValue()).isEqualTo(contactInformation.getValue());
		});
	}

	@Test
	void toLawTest() {
		var lawDto = createLawDTO();
		var law = EntityMapper.toLaw(lawDto);

		assertThat(law).satisfies(l -> {
			assertThat(l.getArticle()).isEqualTo(lawDto.getArticle());
			assertThat(l.getSfs()).isEqualTo(lawDto.getSfs());
			assertThat(l.getChapter()).isEqualTo(lawDto.getChapter());
			assertThat(l.getHeading()).isEqualTo(lawDto.getHeading());
		});
	}

	@Test
	void toLawDtoTest() {
		var law = createLaw();
		var lawDto = EntityMapper.toLawDto(law);

		assertThat(lawDto).satisfies(l -> {
			assertThat(l.getArticle()).isEqualTo(law.getArticle());
			assertThat(l.getSfs()).isEqualTo(law.getSfs());
			assertThat(l.getChapter()).isEqualTo(law.getChapter());
			assertThat(l.getHeading()).isEqualTo(law.getHeading());
		});
	}
}
