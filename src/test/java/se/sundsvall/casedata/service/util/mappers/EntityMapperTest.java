package se.sundsvall.casedata.service.util.mappers;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.casedata.TestUtil.MUNICIPALITY_ID;
import static se.sundsvall.casedata.TestUtil.createAddress;
import static se.sundsvall.casedata.TestUtil.createAddressDTO;
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
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toAddress;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toAddressDto;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toAttachment;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toAttachmentDto;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toContactInformation;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toContactInformationDto;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toCoordinates;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toCoordinatesDto;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toDecision;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toDecisionDto;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toErrand;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toErrandDto;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toFacility;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toFacilityDto;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toLaw;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toLawDto;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toNote;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toNoteDto;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toStakeholder;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toStakeholderDto;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toStatus;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toStatusDto;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import se.sundsvall.casedata.api.model.validation.enums.AttachmentCategory;
import se.sundsvall.casedata.api.model.validation.enums.StakeholderRole;
import se.sundsvall.casedata.integration.db.model.enums.AddressCategory;
import se.sundsvall.casedata.integration.db.model.enums.ContactType;
import se.sundsvall.casedata.integration.db.model.enums.StakeholderType;

@ExtendWith(MockitoExtension.class)
class EntityMapperTest {

	@Test
	void toErrandTest() {
		final var errandDto = createErrandDTO(MUNICIPALITY_ID);
		final var errand = toErrand(errandDto, MUNICIPALITY_ID);

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
		final var errand = createErrand();
		final var errandDto = toErrandDto(errand);

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
		final var decisionDto = createDecisionDTO(MUNICIPALITY_ID);
		final var decision = toDecision(decisionDto, MUNICIPALITY_ID);

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
		final var decision = createDecision();
		final var decisionDto = toDecisionDto(decision);

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
		final var noteDto = createNoteDTO(MUNICIPALITY_ID);
		final var note = toNote(noteDto, MUNICIPALITY_ID);

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
		final var note = createNote();
		final var noteDto = toNoteDto(note);

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
		final var facilityDto = createFacilityDTO(MUNICIPALITY_ID);
		final var facility = toFacility(facilityDto, MUNICIPALITY_ID);

		assertThat(facility).satisfies(f -> {
			assertThat(f.getFacilityType()).isEqualTo(facilityDto.getFacilityType());
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
		final var facility = createFacility();
		final var facilityDto = toFacilityDto(facility);

		assertThat(facilityDto).satisfies(f -> {
			assertThat(f.getDescription()).isEqualTo(facility.getDescription());
			assertThat(f.getFacilityType()).isEqualTo(facility.getFacilityType());
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
		final var stakeholderDto = createStakeholderDTO(StakeholderType.ORGANIZATION, List.of(StakeholderRole.APPLICANT.name()), MUNICIPALITY_ID);
		final var stakeholder = toStakeholder(stakeholderDto, MUNICIPALITY_ID);

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
			assertThat(s.getRoles()).isEqualTo(stakeholderDto.getRoles());
			assertThat(s.getId()).isEqualTo(stakeholderDto.getId());
			assertThat(s.getVersion()).isEqualTo(stakeholderDto.getVersion());
		});
	}

	@Test
	void toStakeholderDtoTest() {
		final var stakeholder = createStakeholder();
		final var stakeholderDto = toStakeholderDto(stakeholder);

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
			assertThat(s.getRoles()).isEqualTo(stakeholder.getRoles());
			assertThat(s.getId()).isEqualTo(stakeholder.getId());
			assertThat(s.getVersion()).isEqualTo(stakeholder.getVersion());
		});
	}

	@Test
	void toAttachmentTest() {
		final var attachmentDto = createAttachmentDTO(AttachmentCategory.POLICE_REPORT, MUNICIPALITY_ID);
		final var attachment = toAttachment(attachmentDto, MUNICIPALITY_ID);

		assertThat(attachment).satisfies(a -> {
			assertThat(a.getCategory()).isEqualTo(attachmentDto.getCategory());
			assertThat(a.getCreated()).isEqualTo(attachmentDto.getCreated());
			assertThat(a.getUpdated()).isEqualTo(attachmentDto.getUpdated());
			assertThat(a.getId()).isEqualTo(attachmentDto.getId());
			assertThat(a.getVersion()).isEqualTo(attachmentDto.getVersion());
		});
	}

	@Test
	void toAttachmentDtoTest() {
		final var attachment = createAttachment();
		final var attachmentDto = toAttachmentDto(attachment);

		assertThat(attachmentDto).satisfies(a -> {
			assertThat(a.getCategory()).isEqualTo(attachment.getCategory());
			assertThat(a.getCreated()).isEqualTo(attachment.getCreated());
			assertThat(a.getUpdated()).isEqualTo(attachment.getUpdated());
			assertThat(a.getId()).isEqualTo(attachment.getId());
			assertThat(a.getVersion()).isEqualTo(attachment.getVersion());
		});
	}

	@Test
	void toStatusTest() {
		final var statusDto = createStatusDTO();
		final var status = toStatus(statusDto);

		assertThat(status).satisfies(s -> {
			assertThat(s.getDateTime()).isEqualTo(statusDto.getDateTime());
			assertThat(s.getStatusType()).isEqualTo(statusDto.getStatusType());
			assertThat(s.getDescription()).isEqualTo(statusDto.getDescription());
		});
	}

	@Test
	void toStatusDtoTest() {
		final var status = createStatus();
		final var statusDto = toStatusDto(status);

		assertThat(statusDto).satisfies(s -> {
			assertThat(s.getDateTime()).isEqualTo(status.getDateTime());
			assertThat(s.getStatusType()).isEqualTo(status.getStatusType());
			assertThat(s.getDescription()).isEqualTo(status.getDescription());
		});
	}

	@Test
	void toCoordinatesTest() {
		final var coordinatesDto = createCoordinatesDTO();
		final var coordinates = toCoordinates(coordinatesDto);

		assertThat(coordinates).satisfies(c -> {
			assertThat(c.getLatitude()).isEqualTo(coordinatesDto.getLatitude());
			assertThat(c.getLongitude()).isEqualTo(coordinatesDto.getLongitude());
		});
	}

	@Test
	void toCoordinatesDtoTest() {
		final var coordinates = createCoordinates();
		final var coordinatesDto = toCoordinatesDto(coordinates);

		assertThat(coordinatesDto).satisfies(c -> {
			assertThat(c.getLatitude()).isEqualTo(coordinates.getLatitude());
			assertThat(c.getLongitude()).isEqualTo(coordinates.getLongitude());
		});
	}

	@Test
	void toAddressTest() {
		final var addressDto = createAddressDTO(AddressCategory.VISITING_ADDRESS);
		final var address = toAddress(addressDto);

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
		final var address = createAddress();
		final var addressDto = toAddressDto(address);

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
		final var contactInformationDto = createContactInformationDTO(ContactType.EMAIL);
		final var contactInformation = toContactInformation(contactInformationDto);

		assertThat(contactInformation).satisfies(c -> {
			assertThat(c.getContactType()).isEqualTo(contactInformationDto.getContactType());
			assertThat(c.getValue()).isEqualTo(contactInformationDto.getValue());
		});
	}

	@Test
	void toContactInformationDtoTest() {
		final var contactInformation = createContactInformation();
		final var contactInformationDto = toContactInformationDto(contactInformation);

		assertThat(contactInformationDto).satisfies(c -> {
			assertThat(c.getContactType()).isEqualTo(contactInformation.getContactType());
			assertThat(c.getValue()).isEqualTo(contactInformation.getValue());
		});
	}

	@Test
	void toLawTest() {
		final var lawDto = createLawDTO();
		final var law = toLaw(lawDto);

		assertThat(law).satisfies(l -> {
			assertThat(l.getArticle()).isEqualTo(lawDto.getArticle());
			assertThat(l.getSfs()).isEqualTo(lawDto.getSfs());
			assertThat(l.getChapter()).isEqualTo(lawDto.getChapter());
			assertThat(l.getHeading()).isEqualTo(lawDto.getHeading());
		});
	}

	@Test
	void toLawDtoTest() {
		final var law = createLaw();
		final var lawDto = toLawDto(law);

		assertThat(lawDto).satisfies(l -> {
			assertThat(l.getArticle()).isEqualTo(law.getArticle());
			assertThat(l.getSfs()).isEqualTo(law.getSfs());
			assertThat(l.getChapter()).isEqualTo(law.getChapter());
			assertThat(l.getHeading()).isEqualTo(law.getHeading());
		});
	}

}
