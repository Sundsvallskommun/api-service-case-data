package se.sundsvall.casedata;

import static java.util.UUID.randomUUID;
import static se.sundsvall.casedata.api.model.validation.enums.CaseType.MEX_BUY_LAND_FROM_THE_MUNICIPALITY;
import static se.sundsvall.casedata.api.model.validation.enums.StakeholderRole.ADMINISTRATOR;
import static se.sundsvall.casedata.integration.db.model.enums.Priority.HIGH;
import static se.sundsvall.dept44.util.DateUtils.toOffsetDateTimeWithLocalOffset;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
import org.apache.commons.lang3.RandomStringUtils;
import se.sundsvall.casedata.api.model.Address;
import se.sundsvall.casedata.api.model.Attachment;
import se.sundsvall.casedata.api.model.ContactInformation;
import se.sundsvall.casedata.api.model.Coordinates;
import se.sundsvall.casedata.api.model.Decision;
import se.sundsvall.casedata.api.model.Errand;
import se.sundsvall.casedata.api.model.ExtraParameter;
import se.sundsvall.casedata.api.model.Facility;
import se.sundsvall.casedata.api.model.Law;
import se.sundsvall.casedata.api.model.Note;
import se.sundsvall.casedata.api.model.Notification;
import se.sundsvall.casedata.api.model.PatchDecision;
import se.sundsvall.casedata.api.model.PatchErrand;
import se.sundsvall.casedata.api.model.PatchNotification;
import se.sundsvall.casedata.api.model.RelatedErrand;
import se.sundsvall.casedata.api.model.Stakeholder;
import se.sundsvall.casedata.api.model.Status;
import se.sundsvall.casedata.api.model.Suspension;
import se.sundsvall.casedata.api.model.validation.enums.AttachmentCategory;
import se.sundsvall.casedata.api.model.validation.enums.CaseType;
import se.sundsvall.casedata.api.model.validation.enums.FacilityType;
import se.sundsvall.casedata.api.model.validation.enums.StakeholderRole;
import se.sundsvall.casedata.integration.db.model.AddressEntity;
import se.sundsvall.casedata.integration.db.model.AttachmentEntity;
import se.sundsvall.casedata.integration.db.model.ContactInformationEntity;
import se.sundsvall.casedata.integration.db.model.CoordinatesEntity;
import se.sundsvall.casedata.integration.db.model.DecisionEntity;
import se.sundsvall.casedata.integration.db.model.ErrandEntity;
import se.sundsvall.casedata.integration.db.model.ExtraParameterEntity;
import se.sundsvall.casedata.integration.db.model.FacilityEntity;
import se.sundsvall.casedata.integration.db.model.LawEntity;
import se.sundsvall.casedata.integration.db.model.NoteEntity;
import se.sundsvall.casedata.integration.db.model.NotificationEntity;
import se.sundsvall.casedata.integration.db.model.RelatedErrandEntity;
import se.sundsvall.casedata.integration.db.model.StakeholderEntity;
import se.sundsvall.casedata.integration.db.model.StatusEntity;
import se.sundsvall.casedata.integration.db.model.enums.AddressCategory;
import se.sundsvall.casedata.integration.db.model.enums.Channel;
import se.sundsvall.casedata.integration.db.model.enums.ContactType;
import se.sundsvall.casedata.integration.db.model.enums.DecisionOutcome;
import se.sundsvall.casedata.integration.db.model.enums.DecisionType;
import se.sundsvall.casedata.integration.db.model.enums.NoteType;
import se.sundsvall.casedata.integration.db.model.enums.StakeholderType;

public final class TestUtil {

	public static final String MUNICIPALITY_ID = "2281";
	public static final String NAMESPACE = "my.namespace";

	public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
		.enable(SerializationFeature.INDENT_OUTPUT)
		.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
		.configure(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE, false)
		.registerModule(new JavaTimeModule());

	public static Errand createErrand() {
		return Errand.builder()
			.withId(new Random().nextLong(1, 100000))
			.withExternalCaseId(randomUUID().toString())
			.withCaseType(CaseType.PARKING_PERMIT.name())
			.withChannel(Channel.EMAIL)
			.withPriority(HIGH)
			.withErrandNumber(RandomStringUtils.secure().next(10, true, true))
			.withDescription(RandomStringUtils.secure().next(20, true, false))
			.withCaseTitleAddition(RandomStringUtils.secure().next(10, true, false))
			.withDiaryNumber(RandomStringUtils.secure().next(10, true, true))
			.withPhase(RandomStringUtils.secure().next(10, true, true))
			.withStartDate(LocalDate.now().minusDays(3))
			.withEndDate(LocalDate.now().plusDays(10))
			.withApplicationReceived(getRandomOffsetDateTime())
			.withFacilities(createFacilities(true, new ArrayList<>(List.of(FacilityType.GARAGE))))
			.withStatuses(new ArrayList<>(List.of(createStatus())))
			.withDecisions(new ArrayList<>(List.of(createDecision())))
			.withUpdatedBy(RandomStringUtils.secure().next(10, true, false))
			.withCreatedBy(RandomStringUtils.secure().next(10, true, false))
			.withUpdatedByClient(RandomStringUtils.secure().next(10, true, false))
			.withCreatedByClient(RandomStringUtils.secure().next(10, true, false))
			.withProcessId(RandomStringUtils.secure().next(10, true, true))
			.withCreated(getRandomOffsetDateTime())
			.withUpdated(getRandomOffsetDateTime())
			.withRelatesTo(new ArrayList<>(List.of(RelatedErrand.builder().withErrandId(new Random().nextLong()).withErrandNumber("ErrandNumber").withRelationReason("Relation reason").build())))
			.withNotes(new ArrayList<>(List.of(createNote(), createNote(), createNote())))
			.withStakeholders(new ArrayList<>(List.of(
				createStakeholder(StakeholderType.PERSON, new ArrayList<>(List.of(getRandomStakeholderRole(), getRandomStakeholderRole(), ADMINISTRATOR.toString()))),
				createStakeholder(StakeholderType.ORGANIZATION, new ArrayList<>(List.of(getRandomStakeholderRole(), getRandomStakeholderRole()))))))
			.withMessageIds(new ArrayList<>(List.of(
				RandomStringUtils.secure().next(10, true, true),
				RandomStringUtils.secure().next(10, true, true),
				RandomStringUtils.secure().next(10, true, true))))
			.withSuspension(Suspension.builder().withSuspendedFrom(OffsetDateTime.now()).withSuspendedTo(OffsetDateTime.now().plusDays(5)).build())
			.withExtraParameters(createExtraParametersList())
			.withLabels(new ArrayList<>(List.of("label1", "label2")))
			.build();
	}

	public static String getRandomStakeholderRole() {
		return StakeholderRole.values()[new Random().nextInt(StakeholderRole.values().length)].name();
	}

	public static StakeholderType getRandomStakeholderType() {
		return StakeholderType.values()[new Random().nextInt(StakeholderType.values().length)];
	}

	public static OffsetDateTime getRandomOffsetDateTime() {
		return toOffsetDateTimeWithLocalOffset(OffsetDateTime.now().minusDays(new Random().nextInt(10000)).truncatedTo(ChronoUnit.MILLIS));
	}

	public static Status createStatus() {

		return Status.builder()
			.withStatusType(RandomStringUtils.secure().next(10, true, false))
			.withDescription(RandomStringUtils.secure().next(20, true, false))
			.withDateTime(getRandomOffsetDateTime())
			.build();
	}

	public static Decision createDecision() {
		return Decision.builder()
			.withId(1L)
			.withMunicipalityId(MUNICIPALITY_ID)
			.withNamespace(NAMESPACE)
			.withCreated(getRandomOffsetDateTime())
			.withUpdated(getRandomOffsetDateTime())
			.withDecisionType(DecisionType.FINAL)
			.withDecisionOutcome(DecisionOutcome.CANCELLATION)
			.withDescription(RandomStringUtils.secure().next(30, true, false))
			.withDecidedBy(createStakeholder(StakeholderType.PERSON, List.of(StakeholderRole.OPERATOR.name())))
			.withDecidedAt(getRandomOffsetDateTime())
			.withValidFrom(getRandomOffsetDateTime())
			.withValidTo(getRandomOffsetDateTime())
			.withLaw(new ArrayList<>(List.of(createLaw())))
			.withAttachments(new ArrayList<>(List.of(createAttachment(AttachmentCategory.POLICE_REPORT))))
			.withExtraParameters(createExtraParameters())
			.build();
	}

	public static Facility createFacility() {
		return Facility.builder()
			.withMunicipalityId(MUNICIPALITY_ID)
			.withNamespace(NAMESPACE)
			.withDescription("description")
			.withCreated(getRandomOffsetDateTime())
			.withUpdated(getRandomOffsetDateTime())
			.withVersion(1)
			.withExtraParameters(createExtraParameters())
			.withAddress(createAddress(AddressCategory.VISITING_ADDRESS))
			.withFacilityType(FacilityType.GARAGE.name())
			.withFacilityCollectionName("facilityCollectionName")
			.withMainFacility(true)
			.build();
	}

	public static Attachment createAttachment(final AttachmentCategory category) {

		return Attachment.builder()
			.withMunicipalityId(MUNICIPALITY_ID)
			.withNamespace(NAMESPACE)
			.withCreated(getRandomOffsetDateTime())
			.withUpdated(getRandomOffsetDateTime())
			.withId(new Random().nextLong(1, 100000))
			.withCategory(category.toString())
			.withName(RandomStringUtils.secure().next(10, true, false) + ".pdf")
			.withNote(RandomStringUtils.secure().next(20, true, false))
			.withExtension(".pdf")
			.withMimeType("application/pdf")
			.withFile("dGVzdA==")
			.withErrandId(new Random().nextLong(1, 100000))
			.withExtraParameters(createExtraParameters())
			.build();
	}

	public static Law createLaw() {
		return Law.builder()
			.withHeading(RandomStringUtils.secure().next(10, true, false))
			.withSfs(RandomStringUtils.secure().next(10, true, false))
			.withChapter(RandomStringUtils.secure().next(10, true, false))
			.withArticle(RandomStringUtils.secure().next(10, true, false))
			.build();
	}

	public static List<Facility> createFacilities(final boolean oneMainFacility, final List<FacilityType> facilityTypes) {

		return facilityTypes.stream()
			.map(facilityType -> Facility.builder()
				.withFacilityType(facilityType.name())
				.withMainFacility(oneMainFacility && (facilityTypes.indexOf(facilityType) == 0))
				.withDescription(RandomStringUtils.secure().next(20, true, false))
				.withFacilityCollectionName(RandomStringUtils.secure().next(10, true, false))
				.withAddress(createAddress(AddressCategory.VISITING_ADDRESS))
				.build())
			.toList();
	}

	public static Map<String, String> createExtraParameters() {
		final var extraParams = new HashMap<String, String>();
		extraParams.put(RandomStringUtils.secure().next(10, true, false), RandomStringUtils.secure().next(20, true, false));
		extraParams.put(RandomStringUtils.secure().next(10, true, false), RandomStringUtils.secure().next(20, true, false));
		extraParams.put(RandomStringUtils.secure().next(10, true, false), RandomStringUtils.secure().next(20, true, false));

		return extraParams;
	}

	public static List<ExtraParameter> createExtraParametersList() {
		final var extraParams = new ArrayList<ExtraParameter>();
		extraParams.add(ExtraParameter.builder().withKey(RandomStringUtils.secure().next(10, true, false))
			.withValues(List.of(RandomStringUtils.secure().next(20, true, false))).build());
		extraParams.add(ExtraParameter.builder().withKey(RandomStringUtils.secure().next(10, true, false))
			.withValues(List.of(RandomStringUtils.secure().next(20, true, false))).build());
		extraParams.add(ExtraParameter.builder().withKey(RandomStringUtils.secure().next(10, true, false))
			.withValues(List.of(RandomStringUtils.secure().next(20, true, false))).build());

		return extraParams;
	}

	public static Stakeholder createStakeholder(final StakeholderType stakeholderType, final List<String> stakeholderRoles) {
		if (StakeholderType.PERSON.equals(stakeholderType)) {
			return Stakeholder.builder()
				.withMunicipalityId(MUNICIPALITY_ID)
				.withNamespace(NAMESPACE)
				.withCreated(getRandomOffsetDateTime())
				.withUpdated(getRandomOffsetDateTime())
				.withType(StakeholderType.PERSON)
				.withPersonId(UUID.randomUUID().toString())
				.withAdAccount(RandomStringUtils.secure().next(10, true, false))
				.withFirstName(RandomStringUtils.secure().next(10, true, false))
				.withLastName(RandomStringUtils.secure().next(10, true, false))
				.withRoles(stakeholderRoles)
				.withContactInformation(List.of(createContactInformation(ContactType.EMAIL), createContactInformation(ContactType.PHONE), createContactInformation(ContactType.CELLPHONE)))
				.withAddresses(List.of(createAddress(AddressCategory.VISITING_ADDRESS)))
				.withExtraParameters(createExtraParameters())
				.build();
		}
		return Stakeholder.builder()
			.withCreated(getRandomOffsetDateTime())
			.withUpdated(getRandomOffsetDateTime())
			.withType(StakeholderType.ORGANIZATION)
			.withOrganizationNumber((new Random().nextInt(999999 - 111111) + 111111) + "-" + (new Random().nextInt(9999 - 1111) + 1111))
			.withOrganizationName(RandomStringUtils.secure().next(20, true, false))
			.withRoles(stakeholderRoles)
			.withContactInformation(List.of(createContactInformation(ContactType.EMAIL), createContactInformation(ContactType.PHONE), createContactInformation(ContactType.CELLPHONE)))
			.withAddresses(List.of(createAddress(AddressCategory.VISITING_ADDRESS)))
			.withExtraParameters(createExtraParameters())
			.withAuthorizedSignatory(RandomStringUtils.secure().next(10, true, false))
			.withAdAccount(RandomStringUtils.secure().next(10, true, false))
			.build();
	}

	public static ContactInformation createContactInformation(final ContactType contactType) {
		return ContactInformation.builder()
			.withContactType(contactType)
			.withValue(RandomStringUtils.secure().next(10, false, true))
			.build();
	}

	public static Address createAddress(final AddressCategory addressCategory) {
		return Address.builder()
			.withAddressCategory(addressCategory)
			.withCity(RandomStringUtils.secure().next(10, true, false))
			.withCountry("Sverige")
			.withPropertyDesignation(RandomStringUtils.secure().next(10, true, false))
			.withStreet(RandomStringUtils.secure().next(10, true, false))
			.withHouseNumber(RandomStringUtils.secure().next(10, true, false))
			.withCareOf(RandomStringUtils.secure().next(10, true, false))
			.withPostalCode(RandomStringUtils.secure().next(10, true, false))
			.withApartmentNumber(RandomStringUtils.secure().next(10, true, false))
			.withAttention(RandomStringUtils.secure().next(10, true, false))
			.withInvoiceMarking(RandomStringUtils.secure().next(10, true, false))
			.withIsZoningPlanArea(false)
			.withLocation(createCoordinates())
			.build();
	}

	public static Coordinates createCoordinates() {
		return Coordinates.builder()
			.withLatitude(new Random().nextDouble())
			.withLongitude(new Random().nextDouble())
			.build();
	}

	public static Note createNote() {
		return Note.builder()
			.withMunicipalityId(MUNICIPALITY_ID)
			.withNamespace(NAMESPACE)
			.withCreated(getRandomOffsetDateTime())
			.withUpdated(getRandomOffsetDateTime())
			.withId(new Random().nextLong(1, 100000))
			.withTitle(RandomStringUtils.secure().next(10, true, false))
			.withText(RandomStringUtils.secure().next(10, true, false))
			.withExtraParameters(createExtraParameters())
			.withCreatedBy(RandomStringUtils.secure().next(10, true, false))
			.withUpdatedBy(RandomStringUtils.secure().next(10, true, false))
			.withNoteType(NoteType.PUBLIC)
			.build();
	}

	public static PatchErrand createPatchErrand() {
		return PatchErrand.builder()
			.withExternalCaseId("externalCaseId")
			.withCaseType(MEX_BUY_LAND_FROM_THE_MUNICIPALITY)
			.withPriority(HIGH)
			.withDescription("description")
			.withCaseTitleAddition("caseTitleAddition")
			.withDiaryNumber("diaryNumber")
			.withPhase("phase")
			.withStartDate(LocalDate.now())
			.withEndDate(LocalDate.now())
			.withApplicationReceived(getRandomOffsetDateTime())
			.withExtraParameters(createExtraParametersList())
			.withFacilities(new ArrayList<>(List.of(createFacility())))
			.withSuspension(Suspension.builder().withSuspendedFrom(OffsetDateTime.now()).withSuspendedTo(OffsetDateTime.now().plusDays(5)).build())
			.withRelatesTo(new ArrayList<>(List.of(new RelatedErrand())))
			.withLabels(new ArrayList<>(List.of("label-xxx", "label-yyy")))
			.build();
	}

	public static PatchDecision createPatchDecision() {
		return PatchDecision.builder()
			.withDecisionType(DecisionType.PROPOSED)
			.withDecisionOutcome(DecisionOutcome.APPROVAL)
			.withDescription("description")
			.withDecidedAt(getRandomOffsetDateTime())
			.withValidFrom(getRandomOffsetDateTime())
			.withValidTo(getRandomOffsetDateTime())
			.withExtraParameters(createExtraParameters())
			.build();
	}

	public static NoteEntity createNoteEntity() {
		return createNoteEntity(null);
	}

	public static NoteEntity createNoteEntity(final Consumer<NoteEntity> modifier) {
		final var note = NoteEntity.builder()
			.withUpdatedBy("updatedBy")
			.withCreatedBy("createdBy")
			.withUpdated(getRandomOffsetDateTime())
			.withCreated(getRandomOffsetDateTime())
			.withText("text")
			.withTitle("title")
			.withMunicipalityId(MUNICIPALITY_ID)
			.withNamespace(NAMESPACE)
			.withErrand(new ErrandEntity())
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

	public static NotificationEntity createNotificationEntity(final Consumer<NotificationEntity> modifier) {
		final var notification = NotificationEntity.builder()
			.withAcknowledged(true)
			.withContent(RandomStringUtils.secure().next(10, true, false))
			.withCreated(getRandomOffsetDateTime())
			.withCreatedBy(RandomStringUtils.secure().next(10, true, false))
			.withCreatedByFullName(RandomStringUtils.secure().next(10, true, false))
			.withDescription(RandomStringUtils.secure().next(10, true, false))
			.withErrand(createErrandEntity())
			.withExpires(getRandomOffsetDateTime())
			.withId(randomUUID().toString())
			.withModified(getRandomOffsetDateTime())
			.withMunicipalityId(MUNICIPALITY_ID)
			.withNamespace(NAMESPACE)
			.withOwnerFullName(RandomStringUtils.secure().next(10, true, false))
			.withOwnerId(RandomStringUtils.secure().next(10, true, false))
			.withType(RandomStringUtils.secure().next(10, true, false))
			.build();

		if (modifier != null) {
			modifier.accept(notification);
		}
		return notification;
	}

	public static Notification createNotification(final Consumer<Notification> modifier) {
		final var notification = Notification.builder()
			.withAcknowledged(true)
			.withContent(RandomStringUtils.secure().next(10, true, false))
			.withCreated(getRandomOffsetDateTime())
			.withCreatedBy(RandomStringUtils.secure().next(10, true, false))
			.withCreatedByFullName(RandomStringUtils.secure().next(10, true, false))
			.withDescription(RandomStringUtils.secure().next(10, true, false))
			.withErrandId(1L)
			.withErrandNumber(RandomStringUtils.secure().next(10, true, false))
			.withExpires(getRandomOffsetDateTime())
			.withId(randomUUID().toString())
			.withModified(getRandomOffsetDateTime())
			.withOwnerFullName(RandomStringUtils.secure().next(10, true, false))
			.withOwnerId(RandomStringUtils.secure().next(10, true, false))
			.withType(RandomStringUtils.secure().next(10, true, false))
			.build();

		if (modifier != null) {
			modifier.accept(notification);
		}
		return notification;
	}

	public static PatchNotification createPatchNotification(final Consumer<PatchNotification> modifier) {
		final var patchNotification = PatchNotification.builder()
			.withAcknowledged(true)
			.withContent(RandomStringUtils.secure().next(10, true, false))
			.withDescription(RandomStringUtils.secure().next(10, true, false))
			.withExpires(getRandomOffsetDateTime())
			.withId(randomUUID().toString())
			.withOwnerId(RandomStringUtils.secure().next(10, true, false))
			.withType(RandomStringUtils.secure().next(10, true, false))
			.withErrandId(1L)
			.build();

		if (modifier != null) {
			modifier.accept(patchNotification);
		}
		return patchNotification;
	}

	public static StatusEntity createStatusEntity() {
		return StatusEntity.builder()
			.withDescription("description")
			.withDateTime(getRandomOffsetDateTime())
			.withStatusType("statusType")
			.build();
	}

	public static FacilityEntity createFacilityEntity() {
		return FacilityEntity.builder()
			.withFacilityCollectionName("facilityCollectionName")
			.withCreated(getRandomOffsetDateTime())
			.withUpdated(getRandomOffsetDateTime())
			.withDescription("description")
			.withId(1L)
			.withVersion(1)
			.withExtraParameters(createExtraParameters())
			.withAddress(createAddressEntity())
			.withFacilityType(FacilityType.GARAGE.name())
			.withErrand(null)
			.withMainFacility(true)
			.withMunicipalityId(MUNICIPALITY_ID)
			.withNamespace(NAMESPACE)
			.build();
	}

	public static StakeholderEntity createStakeholderEntity() {
		return StakeholderEntity.builder()
			.withRoles(new ArrayList<>(List.of(StakeholderRole.APPLICANT.name())))
			.withType(StakeholderType.PERSON)
			.withCreated(getRandomOffsetDateTime())
			.withUpdated(getRandomOffsetDateTime())
			.withVersion(1)
			.withId(1L)
			.withExtraParameters(createExtraParameters())
			.withAddresses(new ArrayList<>(List.of(createAddressEntity())))
			.withContactInformation(new ArrayList<>(List.of(createContactInformationEntity())))
			.withErrand(null)
			.withAdAccount("adAccount")
			.withAuthorizedSignatory("authorizedSignatory")
			.withFirstName("firstName")
			.withLastName("lastName")
			.withOrganizationName("organizationName")
			.withOrganizationNumber("organizationNumber")
			.withPersonId("personId")
			.withMunicipalityId(MUNICIPALITY_ID)
			.withNamespace(NAMESPACE)
			.build();
	}

	public static StakeholderEntity createAdministratorStakeholderEntity() {
		return StakeholderEntity.builder()
			.withRoles(new ArrayList<>(List.of(ADMINISTRATOR.name())))
			.withType(StakeholderType.PERSON)
			.withCreated(getRandomOffsetDateTime())
			.withUpdated(getRandomOffsetDateTime())
			.withVersion(1)
			.withId(1L)
			.withExtraParameters(createExtraParameters())
			.withAddresses(new ArrayList<>(List.of(createAddressEntity())))
			.withContactInformation(new ArrayList<>(List.of(createContactInformationEntity())))
			.withErrand(null)
			.withAdAccount("administratorAdAccount")
			.withAuthorizedSignatory("authorizedSignatory")
			.withFirstName("firstName")
			.withLastName("lastName")
			.withOrganizationName("organizationName")
			.withOrganizationNumber("organizationNumber")
			.withPersonId("personId")
			.withMunicipalityId(MUNICIPALITY_ID)
			.withNamespace(NAMESPACE)
			.build();
	}

	public static ContactInformationEntity createContactInformationEntity() {
		return ContactInformationEntity.builder()
			.withContactType(ContactType.EMAIL)
			.withValue("value")
			.build();
	}

	public static AddressEntity createAddressEntity() {
		return AddressEntity.builder()
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
			.withLocation(createCoordinatesEntity())
			.build();
	}

	public static CoordinatesEntity createCoordinatesEntity() {
		return CoordinatesEntity.builder()
			.withLatitude(1.0)
			.withLongitude(1.0)
			.build();
	}

	public static DecisionEntity createDecisionEntity() {
		return DecisionEntity.builder()
			.withDecisionType(DecisionType.PROPOSED)
			.withDescription("description")
			.withDecidedBy(createStakeholderEntity())
			.withValidFrom(getRandomOffsetDateTime())
			.withValidTo(getRandomOffsetDateTime())
			.withExtraParameters(createExtraParameters())
			.withCreated(getRandomOffsetDateTime())
			.withUpdated(getRandomOffsetDateTime())
			.withId(1L)
			.withVersion(1)
			.withDecidedAt(getRandomOffsetDateTime())
			.withDecisionOutcome(DecisionOutcome.APPROVAL)
			.withMunicipalityId(MUNICIPALITY_ID)
			.withNamespace(NAMESPACE)
			.withLaw(new ArrayList<>(List.of(createLawEntity())))
			.withAttachments(new ArrayList<>(List.of(createAttachmentEntity())))
			.build();
	}

	public static LawEntity createLawEntity() {
		return LawEntity.builder()
			.withArticle("article")
			.withChapter("chapter")
			.withHeading("heading")
			.withSfs("sfs")
			.build();
	}

	public static AttachmentEntity createAttachmentEntity() {
		return AttachmentEntity.builder()
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
			.withMunicipalityId(MUNICIPALITY_ID)
			.withNamespace(NAMESPACE)
			.withErrandId(new Random().nextLong(1, 100000))
			.withCategory(AttachmentCategory.POLICE_REPORT.name())
			.build();
	}

	public static ErrandEntity createErrandEntity() {
		return ErrandEntity.builder()
			.withId(new Random().nextLong(1, 1000))
			.withStatuses(new ArrayList<>(List.of(createStatusEntity())))
			.withNotes(new ArrayList<>(List.of(createNoteEntity())))
			.withFacilities(new ArrayList<>(List.of(createFacilityEntity())))
			.withStakeholders(new ArrayList<>(List.of(createStakeholderEntity())))
			.withDecisions(new ArrayList<>(List.of(createDecisionEntity())))
			.withExtraParameters(createExtraParameterEntityList())
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
			.withPhase("phase")
			.withMunicipalityId(MUNICIPALITY_ID)
			.withNamespace(NAMESPACE)
			.withUpdated(getRandomOffsetDateTime())
			.withCreated(getRandomOffsetDateTime())
			.withApplicationReceived(getRandomOffsetDateTime())
			.withEndDate(LocalDate.now())
			.withStartDate(LocalDate.now())
			.withId(1L)
			.withVersion(1)
			.withPriority(HIGH)
			.withChannel(Channel.EMAIL)
			.withCaseType(CaseType.PARKING_PERMIT.name())
			.withRelatesTo(new ArrayList<>(List.of(RelatedErrandEntity.builder().build())))
			.withLabels(new ArrayList<>(List.of("label1", "label2")))
			.build();
	}

	private static List<ExtraParameterEntity> createExtraParameterEntityList() {
		final var list = new ArrayList<ExtraParameterEntity>();
		list.add(
			ExtraParameterEntity.builder()
				.withKey("key1")
				.withValues(List.of("value1"))
				.build());
		list.add(
			ExtraParameterEntity.builder()
				.withKey("key2")
				.build());
		return list;
	}
}
