package se.sundsvall.casedata.service.util.mappers;

import generated.se.sundsvall.employee.PortalPersonData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.casedata.TestUtil;
import se.sundsvall.casedata.api.model.validation.enums.AttachmentCategory;
import se.sundsvall.casedata.api.model.validation.enums.StakeholderRole;
import se.sundsvall.casedata.integration.db.model.enums.AddressCategory;
import se.sundsvall.casedata.integration.db.model.enums.ContactType;
import se.sundsvall.casedata.integration.db.model.enums.StakeholderType;

import java.util.List;

import static java.time.OffsetDateTime.now;
import static java.time.temporal.ChronoUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static se.sundsvall.casedata.TestUtil.MUNICIPALITY_ID;
import static se.sundsvall.casedata.TestUtil.NAMESPACE;
import static se.sundsvall.casedata.TestUtil.createAddress;
import static se.sundsvall.casedata.TestUtil.createAddressEntity;
import static se.sundsvall.casedata.TestUtil.createAttachment;
import static se.sundsvall.casedata.TestUtil.createAttachmentEntity;
import static se.sundsvall.casedata.TestUtil.createContactInformation;
import static se.sundsvall.casedata.TestUtil.createContactInformationEntity;
import static se.sundsvall.casedata.TestUtil.createCoordinates;
import static se.sundsvall.casedata.TestUtil.createCoordinatesEntity;
import static se.sundsvall.casedata.TestUtil.createDecision;
import static se.sundsvall.casedata.TestUtil.createDecisionEntity;
import static se.sundsvall.casedata.TestUtil.createErrand;
import static se.sundsvall.casedata.TestUtil.createErrandEntity;
import static se.sundsvall.casedata.TestUtil.createFacility;
import static se.sundsvall.casedata.TestUtil.createFacilityEntity;
import static se.sundsvall.casedata.TestUtil.createLaw;
import static se.sundsvall.casedata.TestUtil.createLawEntity;
import static se.sundsvall.casedata.TestUtil.createNote;
import static se.sundsvall.casedata.TestUtil.createNoteEntity;
import static se.sundsvall.casedata.TestUtil.createNotification;
import static se.sundsvall.casedata.TestUtil.createNotificationEntity;
import static se.sundsvall.casedata.TestUtil.createStakeholder;
import static se.sundsvall.casedata.TestUtil.createStakeholderEntity;
import static se.sundsvall.casedata.TestUtil.createStatus;
import static se.sundsvall.casedata.TestUtil.createStatusEntity;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toAddress;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toAddressEntity;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toAttachment;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toAttachmentEntity;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toContactInformation;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toContactInformationEntity;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toCoordinates;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toCoordinatesEntity;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toDecision;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toDecisionEntity;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toErrand;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toErrandEntity;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toFacility;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toFacilityEntity;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toLaw;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toLawEntity;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toNote;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toNoteEntity;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toNotification;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toNotificationEntity;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toStakeholder;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toStakeholderEntity;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toStatus;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toStatusEntity;

@ExtendWith(MockitoExtension.class)
class EntityMapperTest {

	@Test
	void toErrandEntityTest() {
		final var errandDto = createErrand();
		final var errand = toErrandEntity(errandDto, MUNICIPALITY_ID, NAMESPACE);

		assertThat(errand).hasNoNullFieldsOrPropertiesExcept("notifications").satisfies(e -> {
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
	void toErrandTest() {
		final var errand = createErrandEntity();
		final var errandDto = toErrand(errand);

		assertThat(errandDto).hasNoNullFieldsOrPropertiesExcept("messageIds").satisfies(e -> {
			assertThat(e.getErrandNumber()).isEqualTo(errand.getErrandNumber());
			assertThat(e.getUpdatedByClient()).isEqualTo(errand.getUpdatedByClient());
			assertThat(e.getUpdatedBy()).isEqualTo(errand.getUpdatedBy());
			assertThat(e.getProcessId()).isEqualTo(errand.getProcessId());
			assertThat(e.getCreated()).isEqualTo(errand.getCreated());
			assertThat(e.getCreatedByClient()).isEqualTo(errand.getCreatedByClient());
			assertThat(e.getCaseTitleAddition()).isEqualTo(errand.getCaseTitleAddition());
			assertThat(e.getDescription()).isEqualTo(errand.getDescription());
			assertThat(e.getNamespace()).isEqualTo(errand.getNamespace());
			assertThat(e.getMunicipalityId()).isEqualTo(errand.getMunicipalityId());
		});
	}

	@Test
	void toDecisionEntityTest() {
		final var decisionDto = createDecision();
		final var errandEntity = createErrandEntity();
		final var decision = toDecisionEntity(decisionDto, errandEntity, MUNICIPALITY_ID, NAMESPACE);

		assertThat(decision).hasNoNullFieldsOrProperties().satisfies(d -> {
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
	void toDecisionTest() {
		final var decision = createDecisionEntity();
		final var decisionDto = toDecision(decision);

		assertThat(decisionDto).hasNoNullFieldsOrProperties().satisfies(d -> {
			assertThat(d.getDescription()).isEqualTo(decision.getDescription());
			assertThat(d.getDecidedAt()).isEqualTo(decision.getDecidedAt());
			assertThat(d.getUpdated()).isEqualTo(decision.getUpdated());
			assertThat(d.getValidFrom()).isEqualTo(decision.getValidFrom());
			assertThat(d.getValidTo()).isEqualTo(decision.getValidTo());
			assertThat(d.getDecisionType()).isEqualTo(decision.getDecisionType());
			assertThat(d.getDecisionOutcome()).isEqualTo(decision.getDecisionOutcome());
			assertThat(d.getVersion()).isEqualTo(decision.getVersion());
			assertThat(d.getId()).isEqualTo(decision.getId());
			assertThat(d.getMunicipalityId()).isEqualTo(decision.getMunicipalityId());
			assertThat(d.getNamespace()).isEqualTo(decision.getNamespace());
		});
	}

	@Test
	void toNoteEntityTest() {
		final var noteDto = createNote();
		final var note = toNoteEntity(noteDto, MUNICIPALITY_ID, NAMESPACE);

		assertThat(note).hasNoNullFieldsOrPropertiesExcept("errand").satisfies(n -> {
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
	void toNoteTest() {
		final var note = createNoteEntity();
		final var noteDto = toNote(note);

		assertThat(noteDto).hasNoNullFieldsOrProperties().satisfies(n -> {
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
			assertThat(n.getMunicipalityId()).isEqualTo(note.getMunicipalityId());
			assertThat(n.getNamespace()).isEqualTo(note.getNamespace());
		});
	}

	@Test
	void toFacilityEntityTest() {
		final var facilityDto = createFacility();
		final var facility = toFacilityEntity(facilityDto, MUNICIPALITY_ID, NAMESPACE);

		assertThat(facility).hasNoNullFieldsOrPropertiesExcept("errand").satisfies(f -> {
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
	void toFacilityTest() {
		final var facility = createFacilityEntity();
		final var facilityDto = toFacility(facility);

		assertThat(facilityDto).hasNoNullFieldsOrProperties().satisfies(f -> {
			assertThat(f.getDescription()).isEqualTo(facility.getDescription());
			assertThat(f.getFacilityType()).isEqualTo(facility.getFacilityType());
			assertThat(f.getUpdated()).isEqualTo(facility.getUpdated());
			assertThat(f.getCreated()).isEqualTo(facility.getCreated());
			assertThat(f.getExtraParameters()).isEqualTo(facility.getExtraParameters());
			assertThat(f.getId()).isEqualTo(facility.getId());
			assertThat(f.getVersion()).isEqualTo(facility.getVersion());
			assertThat(f.getFacilityCollectionName()).isEqualTo(facility.getFacilityCollectionName());
			assertThat(f.getMunicipalityId()).isEqualTo(facility.getMunicipalityId());
			assertThat(f.getNamespace()).isEqualTo(facility.getNamespace());
		});
	}

	@Test
	void toStakeholderEntityTest() {
		final var stakeholderDto = createStakeholder(StakeholderType.ORGANIZATION, List.of(StakeholderRole.APPLICANT.name()));
		final var stakeholder = toStakeholderEntity(stakeholderDto, MUNICIPALITY_ID, NAMESPACE);

		assertThat(stakeholder).hasNoNullFieldsOrPropertiesExcept("errand", "firstName", "lastName", "personId").satisfies(s -> {
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
	void toStakeholderTest() {
		final var stakeholder = createStakeholderEntity();
		final var stakeholderDto = toStakeholder(stakeholder);

		assertThat(stakeholderDto).hasNoNullFieldsOrProperties().satisfies(s -> {
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
			assertThat(s.getMunicipalityId()).isEqualTo(stakeholder.getMunicipalityId());
			assertThat(s.getNamespace()).isEqualTo(stakeholder.getNamespace());
		});
	}

	@Test
	void toAttachmentEntityTest() {
		final var attachmentDto = createAttachment(AttachmentCategory.POLICE_REPORT);
		final var attachment = toAttachmentEntity(attachmentDto, MUNICIPALITY_ID, NAMESPACE);

		assertThat(attachment).hasNoNullFieldsOrProperties().satisfies(a -> {
			assertThat(a.getCategory()).isEqualTo(attachmentDto.getCategory());
			assertThat(a.getCreated()).isEqualTo(attachmentDto.getCreated());
			assertThat(a.getUpdated()).isEqualTo(attachmentDto.getUpdated());
			assertThat(a.getId()).isEqualTo(attachmentDto.getId());
			assertThat(a.getVersion()).isEqualTo(attachmentDto.getVersion());
		});
	}

	@Test
	void toAttachmentTest() {
		final var attachment = createAttachmentEntity();
		final var attachmentDto = toAttachment(attachment);

		assertThat(attachmentDto).hasNoNullFieldsOrProperties().satisfies(a -> {
			assertThat(a.getCategory()).isEqualTo(attachment.getCategory());
			assertThat(a.getCreated()).isEqualTo(attachment.getCreated());
			assertThat(a.getUpdated()).isEqualTo(attachment.getUpdated());
			assertThat(a.getId()).isEqualTo(attachment.getId());
			assertThat(a.getVersion()).isEqualTo(attachment.getVersion());
			assertThat(a.getMunicipalityId()).isEqualTo(attachment.getMunicipalityId());
			assertThat(a.getNamespace()).isEqualTo(attachment.getNamespace());
		});
	}

	@Test
	void toStatusEntityTest() {
		final var statusDto = createStatus();
		final var status = toStatusEntity(statusDto);

		assertThat(status).hasNoNullFieldsOrProperties().satisfies(s -> {
			assertThat(s.getDateTime()).isEqualTo(statusDto.getDateTime());
			assertThat(s.getStatusType()).isEqualTo(statusDto.getStatusType());
			assertThat(s.getDescription()).isEqualTo(statusDto.getDescription());
		});
	}

	@Test
	void toStatusTest() {
		final var status = createStatusEntity();
		final var statusDto = toStatus(status);

		assertThat(statusDto).hasNoNullFieldsOrProperties().satisfies(s -> {
			assertThat(s.getDateTime()).isEqualTo(status.getDateTime());
			assertThat(s.getStatusType()).isEqualTo(status.getStatusType());
			assertThat(s.getDescription()).isEqualTo(status.getDescription());
		});
	}

	@Test
	void toCoordinatesEntityTest() {
		final var coordinatesDto = createCoordinates();
		final var coordinates = toCoordinatesEntity(coordinatesDto);

		assertThat(coordinates).hasNoNullFieldsOrProperties().satisfies(c -> {
			assertThat(c.getLatitude()).isEqualTo(coordinatesDto.getLatitude());
			assertThat(c.getLongitude()).isEqualTo(coordinatesDto.getLongitude());
		});
	}

	@Test
	void toCoordinatesTest() {
		final var coordinates = createCoordinatesEntity();
		final var coordinatesDto = toCoordinates(coordinates);

		assertThat(coordinatesDto).hasNoNullFieldsOrProperties().satisfies(c -> {
			assertThat(c.getLatitude()).isEqualTo(coordinates.getLatitude());
			assertThat(c.getLongitude()).isEqualTo(coordinates.getLongitude());
		});
	}

	@Test
	void toAddressEntityTest() {
		final var addressDto = createAddress(AddressCategory.VISITING_ADDRESS);
		final var address = toAddressEntity(addressDto);

		assertThat(address).hasNoNullFieldsOrProperties().satisfies(a -> {
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
	void toAddressTest() {
		final var address = createAddressEntity();
		final var addressDto = toAddress(address);

		assertThat(addressDto).hasNoNullFieldsOrProperties().satisfies(a -> {
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
	void toContactInformationEntityTest() {
		final var contactInformationDto = createContactInformation(ContactType.EMAIL);
		final var contactInformation = toContactInformationEntity(contactInformationDto);

		assertThat(contactInformation).hasNoNullFieldsOrProperties().satisfies(c -> {
			assertThat(c.getContactType()).isEqualTo(contactInformationDto.getContactType());
			assertThat(c.getValue()).isEqualTo(contactInformationDto.getValue());
		});
	}

	@Test
	void toContactInformationTest() {
		final var contactInformation = createContactInformationEntity();
		final var contactInformationDto = toContactInformation(contactInformation);

		assertThat(contactInformationDto).hasNoNullFieldsOrProperties().satisfies(c -> {
			assertThat(c.getContactType()).isEqualTo(contactInformation.getContactType());
			assertThat(c.getValue()).isEqualTo(contactInformation.getValue());
		});
	}

	@Test
	void toLawEntityTest() {
		final var lawDto = createLaw();
		final var law = toLawEntity(lawDto);

		assertThat(law).hasNoNullFieldsOrProperties().satisfies(l -> {
			assertThat(l.getArticle()).isEqualTo(lawDto.getArticle());
			assertThat(l.getSfs()).isEqualTo(lawDto.getSfs());
			assertThat(l.getChapter()).isEqualTo(lawDto.getChapter());
			assertThat(l.getHeading()).isEqualTo(lawDto.getHeading());
		});
	}

	@Test
	void toLawTest() {
		final var law = createLawEntity();
		final var lawDto = toLaw(law);

		assertThat(lawDto).hasNoNullFieldsOrProperties().satisfies(l -> {
			assertThat(l.getArticle()).isEqualTo(law.getArticle());
			assertThat(l.getSfs()).isEqualTo(law.getSfs());
			assertThat(l.getChapter()).isEqualTo(law.getChapter());
			assertThat(l.getHeading()).isEqualTo(law.getHeading());
		});
	}

	@Test
	void toNotificationEntityTest() {
		final var notification = createNotification(null);
		final var errand = createErrandEntity();
		final var creator = new PortalPersonData().fullname("creatorFullName");
		final var owner = new PortalPersonData().fullname("ownerFullName");

		final var notificationEntity = toNotificationEntity(notification, MUNICIPALITY_ID, NAMESPACE, errand, creator, owner);

		assertThat(notificationEntity).satisfies(entity -> {
			assertThat(entity.getContent()).isEqualTo(notification.getContent());
			assertThat(entity.getCreated()).isEqualTo(notification.getCreated());
			assertThat(entity.getCreatedBy()).isEqualTo(notification.getCreatedBy());
			assertThat(entity.getCreatedByFullName()).isEqualTo("creatorFullName");
			assertThat(entity.getDescription()).isEqualTo(notification.getDescription());
			assertThat(entity.getExpires()).isEqualTo(notification.getExpires());
			assertThat(entity.getErrand()).isEqualTo(errand);
			assertThat(entity.getId()).isEqualTo(notification.getId());
			assertThat(entity.getModified()).isEqualTo(notification.getModified());
			assertThat(entity.getMunicipalityId()).isEqualTo(MUNICIPALITY_ID);
			assertThat(entity.getNamespace()).isEqualTo(NAMESPACE);
			assertThat(entity.getOwnerFullName()).isEqualTo("ownerFullName");
			assertThat(entity.getOwnerId()).isEqualTo(notification.getOwnerId());
			assertThat(entity.getType()).isEqualTo(notification.getType());
		});
	}

	@Test
	void toNotificationEntityWhenExpriresIsNullTest() {
		final var notification = createNotification(null);
		final var errand = createErrandEntity();
		final var creator = new PortalPersonData().fullname("creatorFullName");
		final var owner = new PortalPersonData().fullname("ownerFullName");

		notification.setExpires(null);

		final var notificationEntity = toNotificationEntity(notification, MUNICIPALITY_ID, NAMESPACE, errand, creator, owner);

		assertThat(notificationEntity).satisfies(entity -> {
			assertThat(entity.getContent()).isEqualTo(notification.getContent());
			assertThat(entity.getCreated()).isEqualTo(notification.getCreated());
			assertThat(entity.getCreatedBy()).isEqualTo(notification.getCreatedBy());
			assertThat(entity.getCreatedByFullName()).isEqualTo("creatorFullName");
			assertThat(entity.getDescription()).isEqualTo(notification.getDescription());
			assertThat(entity.getExpires()).isCloseTo(now().plusDays(30), within(1, SECONDS)); // Should be 30 days from now
			assertThat(entity.getErrand()).isEqualTo(errand);
			assertThat(entity.getId()).isEqualTo(notification.getId());
			assertThat(entity.getModified()).isEqualTo(notification.getModified());
			assertThat(entity.getMunicipalityId()).isEqualTo(MUNICIPALITY_ID);
			assertThat(entity.getNamespace()).isEqualTo(NAMESPACE);
			assertThat(entity.getOwnerFullName()).isEqualTo("ownerFullName");
			assertThat(entity.getOwnerId()).isEqualTo(notification.getOwnerId());
			assertThat(entity.getType()).isEqualTo(notification.getType());
		});
	}

	@Test
	void toNotificationEntityTestWhenOwnerAndCreatorAreNull() {
		final var notification = createNotification(null);
		final var errand = createErrandEntity();

		final var notificationEntity = toNotificationEntity(notification, MUNICIPALITY_ID, NAMESPACE, errand, null, null); // owner and creator null

		assertThat(notificationEntity).satisfies(entity -> {
			assertThat(entity.getContent()).isEqualTo(notification.getContent());
			assertThat(entity.getCreated()).isEqualTo(notification.getCreated());
			assertThat(entity.getCreatedBy()).isEqualTo(notification.getCreatedBy());
			assertThat(entity.getCreatedByFullName()).isEqualTo("unknown");
			assertThat(entity.getDescription()).isEqualTo(notification.getDescription());
			assertThat(entity.getExpires()).isEqualTo(notification.getExpires());
			assertThat(entity.getErrand()).isEqualTo(errand);
			assertThat(entity.getId()).isEqualTo(notification.getId());
			assertThat(entity.getModified()).isEqualTo(notification.getModified());
			assertThat(entity.getMunicipalityId()).isEqualTo(MUNICIPALITY_ID);
			assertThat(entity.getNamespace()).isEqualTo(NAMESPACE);
			assertThat(entity.getOwnerFullName()).isEqualTo("unknown");
			assertThat(entity.getOwnerId()).isEqualTo(notification.getOwnerId());
			assertThat(entity.getType()).isEqualTo(notification.getType());
		});
	}

	@Test
	void toNotificationTest() {
		final var notificationEntity = createNotificationEntity(null);
		final var notification = toNotification(notificationEntity);

		assertThat(notification).satisfies(obj -> {
			assertThat(obj.getContent()).isEqualTo(notificationEntity.getContent());
			assertThat(obj.getCreated()).isEqualTo(notificationEntity.getCreated());
			assertThat(obj.getCreatedBy()).isEqualTo(notificationEntity.getCreatedBy());
			assertThat(obj.getCreatedByFullName()).isEqualTo(notificationEntity.getCreatedByFullName());
			assertThat(obj.getDescription()).isEqualTo(notificationEntity.getDescription());
			assertThat(obj.getExpires()).isEqualTo(notificationEntity.getExpires());
			assertThat(obj.getErrandId()).isEqualTo(notificationEntity.getErrand().getId());
			assertThat(obj.getErrandNumber()).isEqualTo(notificationEntity.getErrand().getErrandNumber());
			assertThat(obj.getId()).isEqualTo(notificationEntity.getId());
			assertThat(obj.getModified()).isEqualTo(notificationEntity.getModified());
			assertThat(obj.getOwnerFullName()).isEqualTo(notificationEntity.getOwnerFullName());
			assertThat(obj.getOwnerId()).isEqualTo(notificationEntity.getOwnerId());
			assertThat(obj.getType()).isEqualTo(notificationEntity.getType());
			assertThat(obj.getMunicipalityId()).isEqualTo(notificationEntity.getMunicipalityId());
			assertThat(obj.getNamespace()).isEqualTo(notificationEntity.getNamespace());
		});
	}

	@Test
	void toOwnerIdTest() {

		final var errandEntity = createErrandEntity();
		final var stakeholderList = errandEntity.getStakeholders();
		stakeholderList.add(TestUtil.createAdministratorStakeholderEntity());
		errandEntity.setStakeholders(stakeholderList);

		final var ownerId = EntityMapper.toOwnerId(errandEntity);

		assertThat(ownerId).isEqualTo("administratorAdAccount");
	}

	@Test
	void toOwnerIdWhenStakeholdersIsNullTest() {

		final var errandEntity = createErrandEntity();
		errandEntity.setStakeholders(null);

		final var ownerId = EntityMapper.toOwnerId(errandEntity);

		assertThat(ownerId).isNull();
	}
}
