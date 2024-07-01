package se.sundsvall.casedata;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static se.sundsvall.dept44.util.DateUtils.toOffsetDateTimeWithLocalOffset;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.function.Consumer;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.commons.lang3.RandomStringUtils;

import se.sundsvall.casedata.api.model.AddressDTO;
import se.sundsvall.casedata.api.model.AppealDTO;
import se.sundsvall.casedata.api.model.AttachmentDTO;
import se.sundsvall.casedata.api.model.ContactInformationDTO;
import se.sundsvall.casedata.api.model.CoordinatesDTO;
import se.sundsvall.casedata.api.model.DecisionDTO;
import se.sundsvall.casedata.api.model.ErrandDTO;
import se.sundsvall.casedata.api.model.FacilityDTO;
import se.sundsvall.casedata.api.model.GetParkingPermitDTO;
import se.sundsvall.casedata.api.model.LawDTO;
import se.sundsvall.casedata.api.model.NoteDTO;
import se.sundsvall.casedata.api.model.PatchDecisionDTO;
import se.sundsvall.casedata.api.model.PatchErrandDTO;
import se.sundsvall.casedata.api.model.StakeholderDTO;
import se.sundsvall.casedata.api.model.StatusDTO;
import se.sundsvall.casedata.api.model.validation.enums.AttachmentCategory;
import se.sundsvall.casedata.api.model.validation.enums.CaseType;
import se.sundsvall.casedata.api.model.validation.enums.FacilityType;
import se.sundsvall.casedata.api.model.validation.enums.StakeholderRole;
import se.sundsvall.casedata.integration.db.model.Address;
import se.sundsvall.casedata.integration.db.model.Appeal;
import se.sundsvall.casedata.integration.db.model.Attachment;
import se.sundsvall.casedata.integration.db.model.ContactInformation;
import se.sundsvall.casedata.integration.db.model.Coordinates;
import se.sundsvall.casedata.integration.db.model.Decision;
import se.sundsvall.casedata.integration.db.model.Errand;
import se.sundsvall.casedata.integration.db.model.Facility;
import se.sundsvall.casedata.integration.db.model.Law;
import se.sundsvall.casedata.integration.db.model.Note;
import se.sundsvall.casedata.integration.db.model.Stakeholder;
import se.sundsvall.casedata.integration.db.model.Status;
import se.sundsvall.casedata.integration.db.model.enums.AddressCategory;
import se.sundsvall.casedata.integration.db.model.enums.AppealStatus;
import se.sundsvall.casedata.integration.db.model.enums.Channel;
import se.sundsvall.casedata.integration.db.model.enums.ContactType;
import se.sundsvall.casedata.integration.db.model.enums.DecisionOutcome;
import se.sundsvall.casedata.integration.db.model.enums.DecisionType;
import se.sundsvall.casedata.integration.db.model.enums.NoteType;
import se.sundsvall.casedata.integration.db.model.enums.Priority;
import se.sundsvall.casedata.integration.db.model.enums.StakeholderType;
import se.sundsvall.casedata.integration.db.model.enums.TimelinessReview;
import se.sundsvall.casedata.integration.parkingpermit.ParkingPermitClient;

import generated.se.sundsvall.parkingpermit.StartProcessResponse;

public class TestUtil {

	public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
		.enable(SerializationFeature.INDENT_OUTPUT)
		.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
		.configure(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE, false)
		.registerModule(new JavaTimeModule());

	public static ErrandDTO createErrandDTO() {
		final var errandDTO = new ErrandDTO();
		errandDTO.setExternalCaseId(UUID.randomUUID().toString());
		errandDTO.setCaseType(CaseType.PARKING_PERMIT.name());
		errandDTO.setChannel(Channel.EMAIL);
		errandDTO.setPriority(Priority.HIGH);
		errandDTO.setErrandNumber(RandomStringUtils.random(10, true, true));
		errandDTO.setDescription(RandomStringUtils.random(20, true, false));
		errandDTO.setCaseTitleAddition(RandomStringUtils.random(10, true, false));
		errandDTO.setDiaryNumber(RandomStringUtils.random(10, true, true));
		errandDTO.setPhase(RandomStringUtils.random(10, true, true));
		errandDTO.setMunicipalityId(RandomStringUtils.random(10, false, true));
		errandDTO.setStartDate(LocalDate.now().minusDays(3));
		errandDTO.setEndDate(LocalDate.now().plusDays(10));
		errandDTO.setApplicationReceived(getRandomOffsetDateTime());
		errandDTO.setFacilities(createFacilities(true, new ArrayList<>(List.of(FacilityType.GARAGE))));
		errandDTO.setStatuses(new ArrayList<>(List.of(createStatusDTO())));
		errandDTO.setDecisions(new ArrayList<>(List.of(createDecisionDTO())));
		errandDTO.setAppeals(new ArrayList<>(List.of(createAppealDTO())));
		errandDTO.setNotes(new ArrayList<>(List.of(createNoteDTO(), createNoteDTO(), createNoteDTO())));
		errandDTO.setStakeholders(new ArrayList<>(List.of(
			createStakeholderDTO(StakeholderType.PERSON, new ArrayList<>(List.of(getRandomStakeholderRole(), getRandomStakeholderRole()))),
			createStakeholderDTO(StakeholderType.ORGANIZATION, new ArrayList<>(List.of(getRandomStakeholderRole(), getRandomStakeholderRole()))))));
		errandDTO.setMessageIds(new ArrayList<>(List.of(
			RandomStringUtils.random(10, true, true),
			RandomStringUtils.random(10, true, true),
			RandomStringUtils.random(10, true, true))));

		errandDTO.setExtraParameters(createExtraParameters());

		return errandDTO;
	}

	public static String getRandomStakeholderRole() {
		return StakeholderRole.values()[new Random().nextInt(StakeholderRole.values().length)].name();
	}

	public static StakeholderType getRandomStakeholderType() {
		return StakeholderType.values()[new Random().nextInt(StakeholderType.values().length)];
	}

	public static DecisionType getRandomDecisionType() {
		return DecisionType.values()[new Random().nextInt(DecisionType.values().length)];
	}

	public static OffsetDateTime getRandomOffsetDateTime() {
		return toOffsetDateTimeWithLocalOffset(OffsetDateTime.now().minusDays(new Random().nextInt(10000)).truncatedTo(ChronoUnit.MILLIS));
	}

	public static StatusDTO createStatusDTO() {
		final var statusDTO = new StatusDTO();
		statusDTO.setStatusType(RandomStringUtils.random(10, true, false));
		statusDTO.setDescription(RandomStringUtils.random(20, true, false));
		statusDTO.setDateTime(getRandomOffsetDateTime());

		return statusDTO;
	}

	public static DecisionDTO createDecisionDTO() {
		final var decisionDTO = new DecisionDTO();
		decisionDTO.setDecisionType(getRandomDecisionType());
		decisionDTO.setDecisionOutcome(DecisionOutcome.CANCELLATION);
		decisionDTO.setDescription(RandomStringUtils.random(30, true, false));
		decisionDTO.setDecidedBy(createStakeholderDTO(StakeholderType.PERSON, List.of(StakeholderRole.OPERATOR.name())));
		decisionDTO.setDecidedAt(getRandomOffsetDateTime());
		decisionDTO.setValidFrom(getRandomOffsetDateTime());
		decisionDTO.setValidTo(getRandomOffsetDateTime());
		decisionDTO.setLaw(new ArrayList<>(List.of(createLawDTO())));
		decisionDTO.setAttachments(new ArrayList<>(List.of(createAttachmentDTO(AttachmentCategory.POLICE_REPORT))));
		decisionDTO.setExtraParameters(createExtraParameters());

		return decisionDTO;
	}

	public static AppealDTO createAppealDTO() {
		final var appealDTO = new AppealDTO();
		appealDTO.setDescription("Appeal description");
		appealDTO.setRegisteredAt(getRandomOffsetDateTime());
		appealDTO.setAppealConcernCommunicatedAt(getRandomOffsetDateTime());
		appealDTO.setStatus(AppealStatus.COMPLETED.toString());
		appealDTO.setTimelinessReview(TimelinessReview.NOT_RELEVANT.toString());
		appealDTO.setDecisionId(123L);

		return appealDTO;
	}

	public static FacilityDTO createFacilityDTO() {
		return FacilityDTO.builder()
			.withDescription("description")
			.withCreated(getRandomOffsetDateTime())
			.withUpdated(getRandomOffsetDateTime())
			.withVersion(1)
			.withExtraParameters(createExtraParameters())
			.withAddress(createAddressDTO(AddressCategory.VISITING_ADDRESS))
			.withFacilityType(FacilityType.GARAGE.name())
			.withFacilityCollectionName("facilityCollectionName")
			.withMainFacility(true)
			.build();
	}

	public static AttachmentDTO createAttachmentDTO(final AttachmentCategory category) {
		final var attachmentDTO = new AttachmentDTO();
		attachmentDTO.setCategory(category.toString());
		attachmentDTO.setName(RandomStringUtils.random(10, true, false) + ".pdf");
		attachmentDTO.setNote(RandomStringUtils.random(20, true, false));
		attachmentDTO.setExtension(".pdf");
		attachmentDTO.setMimeType("application/pdf");
		attachmentDTO.setFile("dGVzdA==");
		attachmentDTO.setExtraParameters(createExtraParameters());

		return attachmentDTO;
	}

	public static LawDTO createLawDTO() {
		final var lawDTO = new LawDTO();
		lawDTO.setHeading(RandomStringUtils.random(10, true, false));
		lawDTO.setSfs(RandomStringUtils.random(10, true, false));
		lawDTO.setChapter(RandomStringUtils.random(10, true, false));
		lawDTO.setArticle(RandomStringUtils.random(10, true, false));

		return lawDTO;
	}

	public static List<FacilityDTO> createFacilities(final boolean oneMainFacility, final List<FacilityType> facilityTypes) {
		final var facilityList = new ArrayList<FacilityDTO>();

		facilityTypes.forEach(facilityType -> {
			final FacilityDTO facilityDTO = new FacilityDTO();
			facilityDTO.setFacilityType(facilityType.name());
			facilityDTO.setMainFacility(oneMainFacility && facilityList.isEmpty());
			facilityDTO.setDescription(RandomStringUtils.random(20, true, false));
			facilityDTO.setFacilityCollectionName(RandomStringUtils.random(10, true, false));

			final AddressDTO address = new AddressDTO();
			address.setAddressCategory(AddressCategory.VISITING_ADDRESS);
			final Random random = new Random();
			address.setPropertyDesignation(RandomStringUtils.random(20, true, false).toUpperCase() + " " + random.nextInt(99) + ":" + random.nextInt(999));
			facilityDTO.setAddress(address);

			facilityList.add(facilityDTO);
		});

		return facilityList;
	}

	public static Map<String, String> createExtraParameters() {
		final var extraParams = new HashMap<String, String>();
		extraParams.put(RandomStringUtils.random(10, true, false), RandomStringUtils.random(20, true, false));
		extraParams.put(RandomStringUtils.random(10, true, false), RandomStringUtils.random(20, true, false));
		extraParams.put(RandomStringUtils.random(10, true, false), RandomStringUtils.random(20, true, false));

		return extraParams;
	}

	public static StakeholderDTO createStakeholderDTO(final StakeholderType stakeholderType, final List<String> stakeholderRoles) {
		if (stakeholderType.equals(StakeholderType.PERSON)) {
			final var person = new StakeholderDTO();
			person.setType(StakeholderType.PERSON);
			person.setPersonId(UUID.randomUUID().toString());
			person.setAdAccount(RandomStringUtils.random(10, true, false));
			person.setFirstName(RandomStringUtils.random(10, true, false));
			person.setLastName(RandomStringUtils.random(10, true, false));
			person.setRoles(stakeholderRoles);
			person.setContactInformation(List.of(createContactInformationDTO(ContactType.EMAIL), createContactInformationDTO(ContactType.PHONE), createContactInformationDTO(ContactType.CELLPHONE)));
			person.setAddresses(List.of(createAddressDTO(AddressCategory.VISITING_ADDRESS)));
			person.setExtraParameters(createExtraParameters());
			return person;
		} else {
			final var organization = new StakeholderDTO();
			organization.setType(StakeholderType.ORGANIZATION);
			organization.setOrganizationNumber((new Random().nextInt(999999 - 111111) + 111111) + "-" + (new Random().nextInt(9999 - 1111) + 1111));
			organization.setOrganizationName(RandomStringUtils.random(20, true, false));
			organization.setRoles(stakeholderRoles);
			organization.setContactInformation(List.of(createContactInformationDTO(ContactType.EMAIL), createContactInformationDTO(ContactType.PHONE), createContactInformationDTO(ContactType.CELLPHONE)));
			organization.setAddresses(List.of(createAddressDTO(AddressCategory.VISITING_ADDRESS)));
			organization.setExtraParameters(createExtraParameters());
			organization.setAuthorizedSignatory(RandomStringUtils.random(10, true, false));
			organization.setAdAccount(RandomStringUtils.random(10, true, false));
			return organization;
		}
	}

	public static ContactInformationDTO createContactInformationDTO(final ContactType contactType) {
		final var contactInformationDTO = new ContactInformationDTO();
		contactInformationDTO.setContactType(contactType);
		contactInformationDTO.setValue(RandomStringUtils.random(10, false, true));

		return contactInformationDTO;
	}

	public static AddressDTO createAddressDTO(final AddressCategory addressCategory) {
		final var address = new AddressDTO();
		address.setAddressCategory(addressCategory);
		address.setCity(RandomStringUtils.random(10, true, false));
		address.setCountry("Sverige");
		address.setPropertyDesignation(RandomStringUtils.random(10, true, false));
		address.setStreet(RandomStringUtils.random(10, true, false));
		address.setHouseNumber(RandomStringUtils.random(10, true, false));
		address.setCareOf(RandomStringUtils.random(10, true, false));
		address.setPostalCode(RandomStringUtils.random(10, true, false));
		address.setApartmentNumber(RandomStringUtils.random(10, true, false));
		address.setAttention(RandomStringUtils.random(10, true, false));
		address.setInvoiceMarking(RandomStringUtils.random(10, true, false));
		address.setIsZoningPlanArea(false);
		final CoordinatesDTO coordinates = createCoordinatesDTO();
		address.setLocation(coordinates);

		return address;
	}

	public static CoordinatesDTO createCoordinatesDTO() {
		final var coordinates = new CoordinatesDTO();
		coordinates.setLatitude(new Random().nextDouble());
		coordinates.setLongitude(new Random().nextDouble());

		return coordinates;
	}

	public static NoteDTO createNoteDTO() {
		final var noteDTO = new NoteDTO();
		noteDTO.setTitle(RandomStringUtils.random(10, true, false));
		noteDTO.setText(RandomStringUtils.random(10, true, false));
		noteDTO.setExtraParameters(createExtraParameters());
		noteDTO.setCreatedBy(RandomStringUtils.random(10, true, false));
		noteDTO.setUpdatedBy(RandomStringUtils.random(10, true, false));
		noteDTO.setNoteType(NoteType.PUBLIC);

		return noteDTO;
	}

	public static GetParkingPermitDTO createGetParkingPermitDTO() {
		return GetParkingPermitDTO.builder()
			.errandId(new Random().nextLong(1, 100000))
			.artefactPermitNumber(RandomStringUtils.random(10, true, true))
			.artefactPermitStatus(RandomStringUtils.random(10, true, false))
			.errandDecision(createDecisionDTO())
			.build();
	}

	public static void mockStartProcess(final ParkingPermitClient parkingPermitClientMock) {
		final var response = new StartProcessResponse();
		response.setProcessId(UUID.randomUUID().toString());

		doReturn(response).when(parkingPermitClientMock).startProcess(any());
	}

	public static PatchErrandDTO createPatchErrandDto() {
		return PatchErrandDTO.builder()
			.withExternalCaseId("externalCaseId")
			.withCaseType(CaseType.ANMALAN_ATTEFALL)
			.withPriority(Priority.HIGH)
			.withDescription("description")
			.withCaseTitleAddition("caseTitleAddition")
			.withDiaryNumber("diaryNumber")
			.withPhase("phase")
			.withStartDate(LocalDate.now())
			.withEndDate(LocalDate.now())
			.withApplicationReceived(getRandomOffsetDateTime())
			.withExtraParameters(createExtraParameters())
			.withFacilities(new ArrayList<>(List.of(createFacilityDTO())))
			.build();
	}

	public static PatchDecisionDTO createPatchDecisionDto() {
		return PatchDecisionDTO.builder()
			.withDecisionType(DecisionType.PROPOSED)
			.withDecisionOutcome(DecisionOutcome.APPROVAL)
			.withDescription("description")
			.withDecidedAt(getRandomOffsetDateTime())
			.withValidFrom(getRandomOffsetDateTime())
			.withValidTo(getRandomOffsetDateTime())
			.withExtraParameters(createExtraParameters())
			.build();
	}

	public static Note createNote() {
		return createNote(null);
	}

	public static Note createNote(final Consumer<Note> modifier) {
		final var note = Note.builder()
			.withUpdatedBy("updatedBy")
			.withCreatedBy("createdBy")
			.withUpdated(getRandomOffsetDateTime())
			.withCreated(getRandomOffsetDateTime())
			.withText("text")
			.withTitle("title")
			.withErrand(new Errand())
			.withNoteType(NoteType.PUBLIC)
			.withVersion(1)
			.withId(1L)
			.withExtraParameters(createExtraParameters())
			.build();

		if (modifier != null) {
			modifier.accept(note);
		}
		return note;
	}

	public static Status createStatus() {
		return Status.builder()
			.withDescription("description")
			.withDateTime(getRandomOffsetDateTime())
			.withStatusType("statusType")
			.build();
	}

	public static Facility createFacility() {
		return Facility.builder()
			.withCreated(getRandomOffsetDateTime())
			.withUpdated(getRandomOffsetDateTime())
			.withDescription("description")
			.withId(1L)
			.withVersion(1)
			.withExtraParameters(createExtraParameters())
			.withAddress(createAddress())
			.withFacilityType(FacilityType.GARAGE.name())
			.withErrand(null)
			.withMainFacility(true)
			.build();
	}

	public static Stakeholder createStakeholder() {
		return Stakeholder.builder()
			.withRoles(new ArrayList<>(List.of(StakeholderRole.APPLICANT.name())))
			.withType(StakeholderType.PERSON)
			.withCreated(getRandomOffsetDateTime())
			.withUpdated(getRandomOffsetDateTime())
			.withVersion(1)
			.withId(1L)
			.withExtraParameters(createExtraParameters())
			.withAddresses(new ArrayList<>(List.of(createAddress())))
			.withContactInformation(new ArrayList<>(List.of(createContactInformation())))
			.withErrand(null)
			.withAdAccount("adAccount")
			.withAuthorizedSignatory("authorizedSignatory")
			.withFirstName("firstName")
			.withLastName("lastName")
			.withOrganizationName("organizationName")
			.withOrganizationNumber("organizationNumber")
			.withPersonId("personId")
			.build();
	}

	public static ContactInformation createContactInformation() {
		return ContactInformation.builder()
			.withContactType(ContactType.EMAIL)
			.withValue("value")
			.build();
	}

	public static Address createAddress() {
		return Address.builder()
			.withAddressCategory(AddressCategory.VISITING_ADDRESS)
			.withApartmentNumber("apartmentNumber")
			.withAttention("attention")
			.withCareOf("careOf")
			.withCity("city")
			.withCountry("country")
			.withHouseNumber("houseNumber")
			.withInvoiceMarking("invoiceMarking")
			.withIsZoningPlanArea(true)
			.withPostalCode("postalCode")
			.withPropertyDesignation("propertyDesignation")
			.withStreet("street")
			.withLocation(createCoordinates())
			.build();
	}

	public static Coordinates createCoordinates() {
		return Coordinates.builder()
			.withLatitude(1.0)
			.withLongitude(1.0)
			.build();
	}

	public static Decision createDecision() {
		return Decision.builder()
			.withExtraParameters(createExtraParameters())
			.withCreated(getRandomOffsetDateTime())
			.withUpdated(getRandomOffsetDateTime())
			.withId(1L)
			.withVersion(1)
			.withDecidedAt(getRandomOffsetDateTime())
			.withDecisionOutcome(DecisionOutcome.APPROVAL)
			.withLaw(new ArrayList<>(List.of(createLaw())))
			.withAttachments(new ArrayList<>(List.of(createAttachment())))
			.build();
	}

	public static Appeal createAppeal() {
		return Appeal.builder()
			.withCreated(getRandomOffsetDateTime())
			.withUpdated(getRandomOffsetDateTime())
			.withId(1L)
			.withVersion(1)
			.withRegisteredAt(getRandomOffsetDateTime())
			.withAppealConcernCommunicatedAt(getRandomOffsetDateTime())
			.withStatus(AppealStatus.NEW)
			.withDescription("description")
			.withTimelinessReview(TimelinessReview.NOT_RELEVANT)
			.withDecision(createDecision())
			.build();
	}

	public static Law createLaw() {
		return Law.builder()
			.withArticle("article")
			.withChapter("chapter")
			.withHeading("heading")
			.withSfs("sfs")
			.build();
	}

	public static Attachment createAttachment() {
		return Attachment.builder()
			.withCreated(getRandomOffsetDateTime())
			.withUpdated(getRandomOffsetDateTime())
			.withVersion(1)
			.withId(1L)
			.withExtraParameters(createExtraParameters())
			.withFile("file")
			.withName("name")
			.withExtension("extension")
			.withMimeType("mimeType")
			.withNote("note")
			.withErrandNumber("errandNumber")
			.withCategory(AttachmentCategory.POLICE_REPORT.name())
			.build();
	}

	public static Errand createErrand() {
		return Errand.builder()
			.withStatuses(new ArrayList<>(List.of(createStatus())))
			.withNotes(new ArrayList<>(List.of(createNote())))
			.withFacilities(new ArrayList<>(List.of(createFacility())))
			.withStakeholders(new ArrayList<>(List.of(createStakeholder())))
			.withDecisions(new ArrayList<>(List.of(createDecision())))
			.withAppeals(new ArrayList<>(List.of(createAppeal())))
			.withExtraParameters(createExtraParameters())
			.withErrandNumber("errandNumber")
			.withExternalCaseId("externalCaseId")
			.withProcessId("processId")
			.withUpdatedBy("updatedBy")
			.withCreatedBy("createdBy")
			.withUpdatedByClient("updatedByClient")
			.withCreatedByClient("createdByClient")
			.withCaseTitleAddition("caseTitleAddition")
			.withDiaryNumber("diaryNumber")
			.withDescription("description")
			.withMunicipalityId("municipalityId")
			.withPhase("phase")
			.withUpdated(getRandomOffsetDateTime())
			.withCreated(getRandomOffsetDateTime())
			.withApplicationReceived(getRandomOffsetDateTime())
			.withEndDate(LocalDate.now())
			.withStartDate(LocalDate.now())
			.withId(1L)
			.withVersion(1)
			.withPriority(Priority.HIGH)
			.withChannel(Channel.EMAIL)
			.withCaseType(CaseType.PARKING_PERMIT.name())
			.build();
	}

}
