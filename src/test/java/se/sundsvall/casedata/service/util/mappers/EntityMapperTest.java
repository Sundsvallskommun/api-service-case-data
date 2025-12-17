package se.sundsvall.casedata.service.util.mappers;

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
import static se.sundsvall.casedata.TestUtil.createNotificationEntityList;
import static se.sundsvall.casedata.TestUtil.createStakeholder;
import static se.sundsvall.casedata.TestUtil.createStakeholderEntity;
import static se.sundsvall.casedata.TestUtil.createStatus;
import static se.sundsvall.casedata.TestUtil.createStatusEntity;
import static se.sundsvall.casedata.api.model.validation.enums.StakeholderRole.ADMINISTRATOR;
import static se.sundsvall.casedata.api.model.validation.enums.StakeholderRole.APPLICANT;
import static se.sundsvall.casedata.integration.db.model.enums.NotificationSubType.ATTACHMENT;
import static se.sundsvall.casedata.integration.db.model.enums.NotificationSubType.ERRAND;
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

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.casedata.TestUtil;
import se.sundsvall.casedata.api.model.Errand;
import se.sundsvall.casedata.api.model.Stakeholder;
import se.sundsvall.casedata.api.model.validation.enums.AttachmentCategory;
import se.sundsvall.casedata.api.model.validation.enums.StakeholderRole;
import se.sundsvall.casedata.integration.db.model.ErrandEntity;
import se.sundsvall.casedata.integration.db.model.StakeholderEntity;
import se.sundsvall.casedata.integration.db.model.enums.AddressCategory;
import se.sundsvall.casedata.integration.db.model.enums.ContactType;
import se.sundsvall.casedata.integration.db.model.enums.Priority;
import se.sundsvall.casedata.integration.db.model.enums.StakeholderType;

@ExtendWith(MockitoExtension.class)
class EntityMapperTest {

	@Test
	void toErrandEntityTest() {
		// Arrange
		final var errandDto = createErrand();

		// Act
		final var errand = toErrandEntity(errandDto, MUNICIPALITY_ID, NAMESPACE);

		// Assert
		assertThat(errand).hasNoNullFieldsOrPropertiesExcept("id", "created", "updated", "notifications").satisfies(e -> {
			assertThat(e.getErrandNumber()).isEqualTo(errandDto.getErrandNumber());
			assertThat(e.getUpdatedByClient()).isEqualTo(errandDto.getUpdatedByClient());
			assertThat(e.getUpdatedBy()).isEqualTo(errandDto.getUpdatedBy());
			assertThat(e.getProcessId()).isEqualTo(errandDto.getProcessId());
			assertThat(e.getCreated()).isNull();
			assertThat(e.getCreatedByClient()).isEqualTo(errandDto.getCreatedByClient());
			assertThat(e.getCaseTitleAddition()).isEqualTo(errandDto.getCaseTitleAddition());
			assertThat(e.getDescription()).isEqualTo(errandDto.getDescription());
			assertThat(e.getLabels()).isEqualTo(errandDto.getLabels());
		});
	}

	@Test
	void toErrandEntityWithNullValuesTest() {

		// Arrange
		final var errand = Errand.builder().build();

		// Act
		final var entity = toErrandEntity(errand, MUNICIPALITY_ID, NAMESPACE);

		// Assert
		assertThat(entity).hasAllNullFieldsOrPropertiesExcept(
			"version",
			"statuses",
			"stakeholders",
			"facilities",
			"decisions",
			"notes",
			"suspension",
			"relatesTo",
			"extraParameters",
			"municipalityId",
			"namespace",
			"priority");

		assertThat(entity.getVersion()).isZero();
		assertThat(entity.getStatuses()).isEmpty();
		assertThat(entity.getStakeholders()).isEmpty();
		assertThat(entity.getFacilities()).isEmpty();
		assertThat(entity.getDecisions()).isEmpty();
		assertThat(entity.getNotes()).isEmpty();
		assertThat(entity.getSuspendedFrom()).isNull();
		assertThat(entity.getSuspendedTo()).isNull();
		assertThat(entity.getExtraParameters()).isEmpty();
		assertThat(entity.getMunicipalityId()).isEqualTo(MUNICIPALITY_ID);
		assertThat(entity.getNamespace()).isEqualTo(NAMESPACE);
		assertThat(entity.getPriority()).isEqualTo(Priority.MEDIUM);
	}

	@Test
	void toErrandTest() {

		// Arrange
		final var errand = createErrandEntity();

		// Act
		final var errandDto = toErrand(errand);

		// Assert
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
	void toErrandWithNullValuesTest() {

		// Arrange
		final var entity = ErrandEntity.builder().withId(1L).build();

		// Act
		final var dto = toErrand(entity);

		// Assert
		assertThat(dto).hasAllNullFieldsOrPropertiesExcept("id",
			"version",
			"statuses",
			"stakeholders",
			"facilities",
			"decisions",
			"relatesTo",
			"notes",
			"suspension",
			"notifications",
			"extraParameters");

		assertThat(dto.getId()).isEqualTo(entity.getId());
		assertThat(dto.getVersion()).isEqualTo(entity.getVersion());
		assertThat(dto.getStatuses()).isEmpty();
		assertThat(dto.getStakeholders()).isEmpty();
		assertThat(dto.getFacilities()).isEmpty();
		assertThat(dto.getDecisions()).isEmpty();
		assertThat(dto.getNotes()).isEmpty();
		assertThat(dto.getSuspension().getSuspendedFrom()).isNull();
		assertThat(dto.getSuspension().getSuspendedTo()).isNull();
		assertThat(dto.getExtraParameters()).isEmpty();
	}

	@Test
	void toErrandWithOnlyActiveNotificationsTest() {

		// Arrange
		final var notifications = createNotificationEntityList();
		final var errand = createErrandEntity();

		errand.setNotifications(notifications);

		// Assert that all notifications are returned.
		final var errand1 = toErrand(errand);
		assertThat(errand1.getNotifications()).hasSize(notifications.size());

		// Acknowledge all notifications
		notifications.forEach(notification -> {
			notification.setAcknowledged(true);
			notification.setGlobalAcknowledged(true);
		});

		// Assert that no notifications are returned.
		final var errand2 = toErrand(errand);
		assertThat(errand2.getNotifications()).isEmpty();
	}

	@Test
	void toDecisionEntityTest() {
		// Arrange
		final var decisionDto = createDecision();
		final var errandEntity = createErrandEntity();

		// Act
		final var decision = toDecisionEntity(decisionDto, errandEntity, MUNICIPALITY_ID, NAMESPACE);

		// Assert
		assertThat(decision).hasNoNullFieldsOrPropertiesExcept("id", "created", "updated").satisfies(d -> {
			assertThat(d.getDescription()).isEqualTo(decisionDto.getDescription());
			assertThat(d.getDecidedAt()).isEqualTo(decisionDto.getDecidedAt());
			assertThat(d.getUpdated()).isNull();
			assertThat(d.getCreated()).isNull();
			assertThat(d.getValidFrom()).isEqualTo(decisionDto.getValidFrom());
			assertThat(d.getValidTo()).isEqualTo(decisionDto.getValidTo());
			assertThat(d.getDecisionType()).isEqualTo(decisionDto.getDecisionType());
			assertThat(d.getDecisionOutcome()).isEqualTo(decisionDto.getDecisionOutcome());
			assertThat(d.getVersion()).isEqualTo(decisionDto.getVersion());
			assertThat(d.getId()).isNull();
		});
	}

	@Test
	void toDecisionTest() {
		// Arrange
		final var decision = createDecisionEntity();

		// Act
		final var decisionDto = toDecision(decision);

		// Assert
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
		// Arrange
		final var noteDto = createNote();

		// Act
		final var note = toNoteEntity(noteDto, MUNICIPALITY_ID, NAMESPACE);

		// Assert
		assertThat(note).hasNoNullFieldsOrPropertiesExcept("id", "created", "updated", "errand").satisfies(n -> {
			assertThat(n.getText()).isEqualTo(noteDto.getText());
			assertThat(n.getNoteType()).isEqualTo(noteDto.getNoteType());
			assertThat(n.getExtraParameters()).isEqualTo(noteDto.getExtraParameters());
			assertThat(n.getTitle()).isEqualTo(noteDto.getTitle());
			assertThat(n.getUpdatedBy()).isEqualTo(noteDto.getUpdatedBy());
			assertThat(n.getCreatedBy()).isEqualTo(noteDto.getCreatedBy());
			assertThat(n.getUpdated()).isNull();
			assertThat(n.getCreated()).isNull();
			assertThat(n.getVersion()).isEqualTo(noteDto.getVersion());
			assertThat(n.getId()).isNull();
		});
	}

	@Test
	void toNoteTest() {
		// Arrange
		final var note = createNoteEntity();

		// Act
		final var noteDto = toNote(note);

		// Assert
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
		// Arrange
		final var facilityDto = createFacility();

		// Act
		final var facility = toFacilityEntity(facilityDto, MUNICIPALITY_ID, NAMESPACE);

		// Assert
		assertThat(facility).hasNoNullFieldsOrPropertiesExcept("errand", "id", "created", "updated").satisfies(f -> {
			assertThat(f.getFacilityType()).isEqualTo(facilityDto.getFacilityType());
			assertThat(f.getUpdated()).isNull();
			assertThat(f.getCreated()).isNull();
			assertThat(f.getDescription()).isEqualTo(facilityDto.getDescription());
			assertThat(f.getExtraParameters()).isEqualTo(facilityDto.getExtraParameters());
			assertThat(f.getId()).isNull();
			assertThat(f.getVersion()).isZero();
			assertThat(f.getFacilityCollectionName()).isEqualTo(facilityDto.getFacilityCollectionName());
		});
	}

	@Test
	void toFacilityTest() {
		// Arrange
		final var facility = createFacilityEntity();

		// Act
		final var facilityDto = toFacility(facility);

		// Assert
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
		// Arrange
		final var stakeholderDto = createStakeholder(StakeholderType.ORGANIZATION, List.of(StakeholderRole.APPLICANT.name()));

		// Act
		final var stakeholder = toStakeholderEntity(stakeholderDto, MUNICIPALITY_ID, NAMESPACE);

		// Assert
		assertThat(stakeholder).hasNoNullFieldsOrPropertiesExcept("errand", "firstName", "lastName", "personId", "id", "created", "updated").satisfies(s -> {
			assertThat(s.getAdAccount()).isEqualTo(stakeholderDto.getAdAccount());
			assertThat(s.getAuthorizedSignatory()).isEqualTo(stakeholderDto.getAuthorizedSignatory());
			assertThat(s.getCreated()).isNull();
			assertThat(s.getUpdated()).isNull();
			assertThat(s.getFirstName()).isEqualTo(stakeholderDto.getFirstName());
			assertThat(s.getLastName()).isEqualTo(stakeholderDto.getLastName());
			assertThat(s.getOrganizationName()).isEqualTo(stakeholderDto.getOrganizationName());
			assertThat(s.getOrganizationNumber()).isEqualTo(stakeholderDto.getOrganizationNumber());
			assertThat(s.getPersonId()).isEqualTo(stakeholderDto.getPersonId());
			assertThat(s.getRoles()).isEqualTo(stakeholderDto.getRoles());
			assertThat(s.getId()).isNull();
			assertThat(s.getVersion()).isEqualTo(stakeholderDto.getVersion());
		});
	}

	@Test
	void toStakeholderEntityWitNullValueTest() {

		// Arrange
		final var stakeholder = Stakeholder.builder().build();

		// Act
		final var entity = toStakeholderEntity(stakeholder, MUNICIPALITY_ID, NAMESPACE);

		// Assert
		assertThat(entity).hasAllNullFieldsOrPropertiesExcept(
			"municipalityId",
			"namespace",
			"version",
			"addresses",
			"contactInformation",
			"extraParameters");

		assertThat(entity.getMunicipalityId()).isEqualTo(MUNICIPALITY_ID);
		assertThat(entity.getNamespace()).isEqualTo(NAMESPACE);
		assertThat(entity.getVersion()).isZero();
		assertThat(entity.getAddresses()).isEmpty();
		assertThat(entity.getContactInformation()).isEmpty();
		assertThat(entity.getExtraParameters()).isEmpty();

	}

	@Test
	void toStakeholderTest() {
		// Arrange
		final var stakeholder = createStakeholderEntity();

		// Act
		final var stakeholderDto = toStakeholder(stakeholder);

		// Assert
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
	void toStakeholderWithNullValuesTest() {

		// Arrange
		final var entity = StakeholderEntity.builder().withId(1L).build();

		// Act
		final var dto = toStakeholder(entity);

		// Assert
		assertThat(dto).hasAllNullFieldsOrPropertiesExcept("id",
			"municipalityId",
			"namespace",
			"version",
			"addresses",
			"contactInformation",
			"extraParameters");

		assertThat(dto.getId()).isEqualTo(entity.getId());
		assertThat(dto.getMunicipalityId()).isEqualTo(entity.getMunicipalityId());
		assertThat(dto.getNamespace()).isEqualTo(entity.getNamespace());
		assertThat(dto.getVersion()).isEqualTo(entity.getVersion());
		assertThat(dto.getAddresses()).isEmpty();
		assertThat(dto.getContactInformation()).isEmpty();
		assertThat(dto.getExtraParameters()).isEmpty();
	}

	@Test
	void toAttachmentEntityTest() {
		// Arrange
		final var errandId = 123L;
		final var attachmentDto = createAttachment(AttachmentCategory.POLICE_REPORT);

		// Act
		final var attachment = toAttachmentEntity(errandId, attachmentDto, MUNICIPALITY_ID, NAMESPACE);

		// Assert
		assertThat(attachment).hasNoNullFieldsOrPropertiesExcept("id", "created", "updated").satisfies(a -> {
			assertThat(a.getErrandId()).isEqualTo(errandId);
			assertThat(a.getCategory()).isEqualTo(attachmentDto.getCategory());
			assertThat(a.getVersion()).isEqualTo(attachmentDto.getVersion());
		});
	}

	@Test
	void toAttachmentTest() {
		// Arrange
		final var attachment = createAttachmentEntity();

		// Act
		final var attachmentDto = toAttachment(attachment);

		// Assert
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
		// Arrange
		final var statusDto = createStatus();

		// Act
		final var status = toStatusEntity(statusDto);

		// Assert
		assertThat(status).hasNoNullFieldsOrProperties().satisfies(s -> {
			assertThat(s.getCreated()).isCloseTo(now(), within(2, SECONDS));
			assertThat(s.getStatusType()).isEqualTo(statusDto.getStatusType());
			assertThat(s.getDescription()).isEqualTo(statusDto.getDescription());
		});
	}

	@Test
	void toStatusTest() {
		// Arrange
		final var status = createStatusEntity();

		// Act
		final var statusDto = toStatus(status);

		// Assert
		assertThat(statusDto).hasNoNullFieldsOrProperties().satisfies(s -> {
			assertThat(s.getCreated()).isEqualTo(status.getCreated());
			assertThat(s.getStatusType()).isEqualTo(status.getStatusType());
			assertThat(s.getDescription()).isEqualTo(status.getDescription());
		});
	}

	@Test
	void toCoordinatesEntityTest() {
		// Arrange
		final var coordinatesDto = createCoordinates();

		// Act
		final var coordinates = toCoordinatesEntity(coordinatesDto);

		// Assert
		assertThat(coordinates).hasNoNullFieldsOrProperties().satisfies(c -> {
			assertThat(c.getLatitude()).isEqualTo(coordinatesDto.getLatitude());
			assertThat(c.getLongitude()).isEqualTo(coordinatesDto.getLongitude());
		});
	}

	@Test
	void toCoordinatesTest() {
		// Arrange
		final var coordinates = createCoordinatesEntity();

		// Act
		final var coordinatesDto = toCoordinates(coordinates);

		// Assert
		assertThat(coordinatesDto).hasNoNullFieldsOrProperties().satisfies(c -> {
			assertThat(c.getLatitude()).isEqualTo(coordinates.getLatitude());
			assertThat(c.getLongitude()).isEqualTo(coordinates.getLongitude());
		});
	}

	@Test
	void toAddressEntityTest() {
		// Arrange
		final var addressDto = createAddress(AddressCategory.VISITING_ADDRESS);

		// Act
		final var address = toAddressEntity(addressDto);

		// Assert
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
		// Arrange
		final var address = createAddressEntity();

		// Act
		final var addressDto = toAddress(address);

		// Assert
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
		// Arrange
		final var contactInformationDto = createContactInformation(ContactType.EMAIL);

		// Act
		final var contactInformation = toContactInformationEntity(contactInformationDto);

		// Assert
		assertThat(contactInformation).hasNoNullFieldsOrProperties().satisfies(c -> {
			assertThat(c.getContactType()).isEqualTo(contactInformationDto.getContactType());
			assertThat(c.getValue()).isEqualTo(contactInformationDto.getValue());
		});
	}

	@Test
	void toContactInformationTest() {
		// Arrange
		final var contactInformation = createContactInformationEntity();

		// Act
		final var contactInformationDto = toContactInformation(contactInformation);

		// Assert
		assertThat(contactInformationDto).hasNoNullFieldsOrProperties().satisfies(c -> {
			assertThat(c.getContactType()).isEqualTo(contactInformation.getContactType());
			assertThat(c.getValue()).isEqualTo(contactInformation.getValue());
		});
	}

	@Test
	void toLawEntityTest() {
		// Arrange
		final var lawDto = createLaw();

		// Act
		final var law = toLawEntity(lawDto);

		// Assert
		assertThat(law).hasNoNullFieldsOrProperties().satisfies(l -> {
			assertThat(l.getArticle()).isEqualTo(lawDto.getArticle());
			assertThat(l.getSfs()).isEqualTo(lawDto.getSfs());
			assertThat(l.getChapter()).isEqualTo(lawDto.getChapter());
			assertThat(l.getHeading()).isEqualTo(lawDto.getHeading());
		});
	}

	@Test
	void toLawTest() {
		// Arrange
		final var law = createLawEntity();

		// Act
		final var lawDto = toLaw(law);

		// Assert
		assertThat(lawDto).hasNoNullFieldsOrProperties().satisfies(l -> {
			assertThat(l.getArticle()).isEqualTo(law.getArticle());
			assertThat(l.getSfs()).isEqualTo(law.getSfs());
			assertThat(l.getChapter()).isEqualTo(law.getChapter());
			assertThat(l.getHeading()).isEqualTo(law.getHeading());
		});
	}

	@Test
	void toNotificationEntityTest() {
		// Arrange
		final var notification = createNotification(null);
		final var errand = createErrandEntity();

		// Act
		final var notificationEntity = toNotificationEntity(notification, MUNICIPALITY_ID, NAMESPACE, errand);

		// Assert
		assertThat(notificationEntity).satisfies(entity -> {
			assertThat(entity.isAcknowledged()).isEqualTo(notification.isAcknowledged());
			assertThat(entity.isGlobalAcknowledged()).isEqualTo(notification.isGlobalAcknowledged());
			assertThat(entity.getContent()).isEqualTo(notification.getContent());
			assertThat(entity.getCreated()).isNull();
			assertThat(entity.getCreatedBy()).isEqualTo(notification.getCreatedBy());
			assertThat(entity.getCreatedByFullName()).isNull();
			assertThat(entity.getDescription()).isEqualTo(notification.getDescription());
			assertThat(entity.getExpires()).isEqualTo(notification.getExpires());
			assertThat(entity.getErrand()).isEqualTo(errand);
			assertThat(entity.getId()).isNull();
			assertThat(entity.getModified()).isNull();
			assertThat(entity.getMunicipalityId()).isEqualTo(MUNICIPALITY_ID);
			assertThat(entity.getNamespace()).isEqualTo(NAMESPACE);
			assertThat(entity.getOwnerFullName()).isNull();
			assertThat(entity.getOwnerId()).isEqualTo(notification.getOwnerId());
			assertThat(entity.getType()).isEqualTo(notification.getType());
		});
	}

	@Test
	void toNotificationEntityWhenExpiresIsNullTest() {
		// Arrange
		final var notification = createNotification(null);
		final var errand = createErrandEntity();

		notification.setExpires(null);

		// Act
		final var notificationEntity = toNotificationEntity(notification, MUNICIPALITY_ID, NAMESPACE, errand);

		// Assert
		assertThat(notificationEntity).satisfies(entity -> {
			assertThat(entity.isAcknowledged()).isEqualTo(notification.isAcknowledged());
			assertThat(entity.isGlobalAcknowledged()).isEqualTo(notification.isGlobalAcknowledged());
			assertThat(entity.getContent()).isEqualTo(notification.getContent());
			assertThat(entity.getCreated()).isNull();
			assertThat(entity.getCreatedBy()).isEqualTo(notification.getCreatedBy());
			assertThat(entity.getCreatedByFullName()).isNull();
			assertThat(entity.getDescription()).isEqualTo(notification.getDescription());
			assertThat(entity.getExpires()).isCloseTo(now().plusDays(40), within(1, SECONDS)); // Should be 40 days from now
			assertThat(entity.getErrand()).isEqualTo(errand);
			assertThat(entity.getId()).isNull();
			assertThat(entity.getModified()).isNull();
			assertThat(entity.getMunicipalityId()).isEqualTo(MUNICIPALITY_ID);
			assertThat(entity.getNamespace()).isEqualTo(NAMESPACE);
			assertThat(entity.getOwnerFullName()).isNull();
			assertThat(entity.getOwnerId()).isEqualTo(notification.getOwnerId());
			assertThat(entity.getType()).isEqualTo(notification.getType());
		});
	}

	@Test
	void toNotificationTest() {
		// Arrange
		final var notificationEntity = createNotificationEntity(null);

		// Act
		final var notification = toNotification(notificationEntity);

		// Assert
		assertThat(notification).satisfies(obj -> {
			assertThat(obj.isAcknowledged()).isEqualTo(notificationEntity.isAcknowledged());
			assertThat(obj.isGlobalAcknowledged()).isEqualTo(notificationEntity.isGlobalAcknowledged());
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
		// Arrange
		final var errandEntity = createErrandEntity();
		final var stakeholderList = errandEntity.getStakeholders();
		stakeholderList.add(TestUtil.createAdministratorStakeholderEntity());
		errandEntity.setStakeholders(stakeholderList);

		// Act
		final var ownerId = EntityMapper.toOwnerId(errandEntity);

		// Assert
		assertThat(ownerId).isEqualTo("administratorAdAccount");
	}

	@Test
	void toOwnerIdWhenStakeholdersIsNullTest() {
		// Arrange
		final var errandEntity = createErrandEntity();
		errandEntity.setStakeholders(null);

		// Act
		final var ownerId = EntityMapper.toOwnerId(errandEntity);

		// Assert
		assertThat(ownerId).isNull();
	}

	@Test
	void getUserIdFromRole() {
		// Arrange
		final var errandEntity = createErrandEntity();

		// Act
		final var id = EntityMapper.getUserIdFromRole(errandEntity, APPLICANT);

		// Assert
		assertThat(id).isEqualTo("adAccount");
	}

	@Test
	void toNotificationWithAdministratorStakeholder() {
		// Arrange
		final var adminStakeholder = StakeholderEntity.builder()
			.withAdAccount("adminAdAccount")
			.withRoles(List.of(ADMINISTRATOR.name())).build();

		final var errandEntity = ErrandEntity.builder()
			.withId(1L)
			.withMunicipalityId("municipalityId")
			.withNamespace("namespace")
			.withStakeholders(List.of(adminStakeholder))
			.build();

		final var type = "type";
		final var subType = ERRAND;
		final var description = "description";

		// Act
		final var notification = toNotification(errandEntity, type, description, subType);

		// Assert
		assertThat(notification).isNotNull();
		assertThat(notification.getOwnerId()).isEqualTo("adminAdAccount");
		assertThat(notification.getType()).isEqualTo(type);
		assertThat(notification.getDescription()).isEqualTo(description);
		assertThat(notification.getErrandId()).isEqualTo(1L);
		assertThat(notification.getMunicipalityId()).isEqualTo("municipalityId");
		assertThat(notification.getNamespace()).isEqualTo("namespace");
		assertThat(notification.getSubType()).isEqualTo(subType.toString());
	}

	@Test
	void toNotificationWithoutAdministratorStakeholder() {
		// Arrange
		final var errandEntity = ErrandEntity.builder()
			.withId(1L)
			.withMunicipalityId("municipalityId")
			.withNamespace("namespace")
			.withStakeholders(List.of())
			.build();

		final var type = "type";
		final var subType = ATTACHMENT;
		final var description = "description";

		// Act
		final var notification = toNotification(errandEntity, type, description, subType);

		// Assert
		assertThat(notification).isNotNull();
		assertThat(notification.getOwnerId()).isNull();
		assertThat(notification.getType()).isEqualTo(type);
		assertThat(notification.getDescription()).isEqualTo(description);
		assertThat(notification.getErrandId()).isEqualTo(1L);
		assertThat(notification.getMunicipalityId()).isEqualTo("municipalityId");
		assertThat(notification.getNamespace()).isEqualTo("namespace");
		assertThat(notification.getSubType()).isEqualTo(subType.toString());
	}
}
