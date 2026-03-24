package se.sundsvall.casedata.service.util.mappers;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import se.sundsvall.casedata.api.model.Address;
import se.sundsvall.casedata.api.model.Attachment;
import se.sundsvall.casedata.api.model.ContactInformation;
import se.sundsvall.casedata.api.model.Coordinates;
import se.sundsvall.casedata.api.model.Decision;
import se.sundsvall.casedata.api.model.Errand;
import se.sundsvall.casedata.api.model.Facility;
import se.sundsvall.casedata.api.model.Law;
import se.sundsvall.casedata.api.model.Note;
import se.sundsvall.casedata.api.model.Notification;
import se.sundsvall.casedata.api.model.RelatedErrand;
import se.sundsvall.casedata.api.model.Stakeholder;
import se.sundsvall.casedata.api.model.Status;
import se.sundsvall.casedata.api.model.Suspension;
import se.sundsvall.casedata.integration.db.model.AddressEntity;
import se.sundsvall.casedata.integration.db.model.AttachmentEntity;
import se.sundsvall.casedata.integration.db.model.ContactInformationEntity;
import se.sundsvall.casedata.integration.db.model.CoordinatesEntity;
import se.sundsvall.casedata.integration.db.model.DecisionEntity;
import se.sundsvall.casedata.integration.db.model.ErrandEntity;
import se.sundsvall.casedata.integration.db.model.FacilityEntity;
import se.sundsvall.casedata.integration.db.model.LawEntity;
import se.sundsvall.casedata.integration.db.model.NoteEntity;
import se.sundsvall.casedata.integration.db.model.NotificationEntity;
import se.sundsvall.casedata.integration.db.model.RelatedErrandEntity;
import se.sundsvall.casedata.integration.db.model.StakeholderEntity;
import se.sundsvall.casedata.integration.db.model.StatusEntity;
import se.sundsvall.casedata.integration.db.model.enums.NotificationSubType;

import static java.time.OffsetDateTime.now;
import static java.time.ZoneId.systemDefault;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static se.sundsvall.casedata.api.model.validation.enums.StakeholderRole.ADMINISTRATOR;
import static se.sundsvall.casedata.service.util.mappers.ErrandExtraParameterMapper.toErrandParameterEntityList;
import static se.sundsvall.casedata.service.util.mappers.ErrandExtraParameterMapper.toParameterList;
import static se.sundsvall.casedata.service.util.mappers.JsonParameterMapper.toJsonParameterEntityList;
import static se.sundsvall.casedata.service.util.mappers.JsonParameterMapper.toJsonParameterList;
import static se.sundsvall.dept44.util.LogUtils.sanitizeForLogging;

public final class EntityMapper {

	private static final int DEFAULT_NOTIFICATION_EXPIRATION_TIME_IN_DAYS = 40;

	private EntityMapper() {}

	public static Errand toErrand(final ErrandEntity errandEntity) {
		return ofNullable(errandEntity)
			.map(obj -> Errand.builder()
				.withId(obj.getId())
				.withVersion(obj.getVersion())
				.withCreated(obj.getCreated())
				.withUpdated(obj.getUpdated())
				.withErrandNumber(obj.getErrandNumber())
				.withExternalCaseId(obj.getExternalCaseId())
				.withCaseType(obj.getCaseType())
				.withChannel(obj.getChannel())
				.withPriority(obj.getPriority())
				.withDescription(obj.getDescription())
				.withCaseTitleAddition(obj.getCaseTitleAddition())
				.withDiaryNumber(obj.getDiaryNumber())
				.withPhase(obj.getPhase())
				.withStartDate(obj.getStartDate())
				.withEndDate(obj.getEndDate())
				.withApplicationReceived(obj.getApplicationReceived())
				.withProcessId(obj.getProcessId())
				.withCreatedByClient(obj.getCreatedByClient())
				.withUpdatedByClient(obj.getUpdatedByClient())
				.withCreatedBy(obj.getCreatedBy())
				.withUpdatedBy(obj.getUpdatedBy())
				.withMunicipalityId(obj.getMunicipalityId())
				.withNamespace(obj.getNamespace())
				.withStatus(EntityMapper.toStatus(obj.getStatus()))
				.withSuspension(Suspension.builder().withSuspendedFrom(obj.getSuspendedFrom()).withSuspendedTo(obj.getSuspendedTo()).build())
				.withConfidentiality(obj.isConfidentiality())
				.withNotes(new ArrayList<>(ofNullable(obj.getNotes()).orElse(emptyList()).stream().map(EntityMapper::toNote).toList()))
				.withStatuses(new ArrayList<>(ofNullable(obj.getStatuses()).orElse(emptyList()).stream().map(EntityMapper::toStatus).toList()))
				.withStakeholders(new ArrayList<>(ofNullable(obj.getStakeholders()).orElse(emptyList()).stream().map(EntityMapper::toStakeholder).toList()))
				.withFacilities(new ArrayList<>(ofNullable(obj.getFacilities()).orElse(emptyList()).stream().map(EntityMapper::toFacility).toList()))
				.withDecisions(new ArrayList<>(ofNullable(obj.getDecisions()).orElse(emptyList()).stream().map(EntityMapper::toDecision).toList()))
				.withRelatesTo(new ArrayList<>(ofNullable(obj.getRelatesTo()).orElse(emptyList()).stream().map(EntityMapper::toRelatedErrand).toList()))
				.withNotifications(toNotifications(obj.getNotifications()))
				.withExtraParameters(toParameterList(obj.getExtraParameters()))
				.withJsonParameters(toJsonParameterList(obj.getJsonParameters()))
				.withLabels(obj.getLabels())
				.build())
			.orElse(null);
	}

	public static ErrandEntity toErrandEntity(final Errand errand, final String municipalityId, final String namespace) {
		final var errandEntity = ofNullable(errand)
			.map(obj -> ErrandEntity.builder()
				.withErrandNumber(obj.getErrandNumber())
				.withExternalCaseId(obj.getExternalCaseId())
				.withCaseType(sanitizeForLogging(obj.getCaseType()))
				.withChannel(obj.getChannel())
				.withPriority(obj.getPriority())
				.withDescription(obj.getDescription())
				.withCaseTitleAddition(obj.getCaseTitleAddition())
				.withDiaryNumber(obj.getDiaryNumber())
				.withPhase(obj.getPhase())
				.withStartDate(obj.getStartDate())
				.withEndDate(obj.getEndDate())
				.withApplicationReceived(obj.getApplicationReceived())
				.withProcessId(obj.getProcessId())
				.withCreatedByClient(obj.getCreatedByClient())
				.withUpdatedByClient(obj.getUpdatedByClient())
				.withCreatedBy(obj.getCreatedBy())
				.withMunicipalityId(municipalityId)
				.withNamespace(namespace)
				.withUpdatedBy(obj.getUpdatedBy())
				.withSuspendedFrom(ofNullable(obj.getSuspension()).map(Suspension::getSuspendedFrom).orElse(null))
				.withSuspendedTo(ofNullable(obj.getSuspension()).map(Suspension::getSuspendedTo).orElse(null))
				.withStatus(EntityMapper.toStatusEntity(obj.getStatus()))
				.withConfidentiality(obj.isConfidentiality())
				.withStatuses(new ArrayList<>(ofNullable(obj.getStatuses())
					.orElse(emptyList())
					.stream()
					.map(EntityMapper::toStatusEntity)
					.toList()))

				.withStakeholders(new ArrayList<>(ofNullable(obj.getStakeholders())
					.orElse(emptyList())
					.stream().map(stakeholderDTO -> toStakeholderEntity(stakeholderDTO, municipalityId, namespace))
					.toList()))
				.withFacilities(new ArrayList<>(ofNullable(obj.getFacilities())
					.orElse(emptyList())
					.stream().map(facilityDTO -> toFacilityEntity(facilityDTO, municipalityId, namespace))
					.toList()))
				.withNotes(new ArrayList<>(ofNullable(obj.getNotes())
					.orElse(emptyList())
					.stream().map(notesDTO -> toNoteEntity(notesDTO, municipalityId, namespace))
					.toList()))
				.withRelatesTo(new ArrayList<>(ofNullable(obj.getRelatesTo())
					.orElse(emptyList())
					.stream().map(EntityMapper::toRelatedErrandEntity)
					.toList()))
				.withLabels(obj.getLabels())
				.build());

		errandEntity.ifPresent(entity -> {
			entity.setDecisions(new ArrayList<>(ofNullable(errand.getDecisions())
				.orElse(emptyList())
				.stream().map(decisionDTO -> toDecisionEntity(decisionDTO, entity, municipalityId, namespace))
				.toList()));
			entity.getStakeholders().forEach(stakeholder -> stakeholder.setErrand(entity));
			entity.getFacilities().forEach(facility -> facility.setErrand(entity));
			entity.getDecisions().forEach(decision -> decision.setErrand(entity));
			entity.getNotes().forEach(note -> note.setErrand(entity));
			entity.setExtraParameters(toErrandParameterEntityList(errand.getExtraParameters(), entity));
			entity.setJsonParameters(toJsonParameterEntityList(errand.getJsonParameters(), entity));
		});

		return errandEntity.orElse(null);
	}

	public static StakeholderEntity toStakeholderEntity(final Stakeholder stakeholder, final String municipalityId, final String namespace) {
		return ofNullable(stakeholder)
			.map(obj -> StakeholderEntity.builder()
				.withType(obj.getType())
				.withFirstName(obj.getFirstName())
				.withLastName(obj.getLastName())
				.withPersonId(obj.getPersonId())
				.withOrganizationName(obj.getOrganizationName())
				.withOrganizationNumber(obj.getOrganizationNumber())
				.withAuthorizedSignatory(obj.getAuthorizedSignatory())
				.withAdAccount(obj.getAdAccount())
				.withMunicipalityId(municipalityId)
				.withNamespace(namespace)
				.withAddresses(new ArrayList<>(ofNullable(obj.getAddresses()).orElse(emptyList()).stream().map(EntityMapper::toAddressEntity).toList()))
				.withContactInformation(new ArrayList<>(ofNullable(obj.getContactInformation()).orElse(emptyList()).stream().map(EntityMapper::toContactInformationEntity).toList()))
				.withRoles(obj.getRoles())
				.withExtraParameters(ofNullable(stakeholder.getExtraParameters()).orElse(new LinkedHashMap<>()))
				.build())
			.orElse(null);
	}

	public static Stakeholder toStakeholder(final StakeholderEntity stakeholderEntity) {
		return ofNullable(stakeholderEntity)
			.map(obj -> Stakeholder.builder()
				.withId(obj.getId())
				.withMunicipalityId(obj.getMunicipalityId())
				.withNamespace(obj.getNamespace())
				.withVersion(obj.getVersion())
				.withCreated(obj.getCreated())
				.withUpdated(obj.getUpdated())
				.withType(obj.getType())
				.withFirstName(obj.getFirstName())
				.withLastName(obj.getLastName())
				.withPersonId(obj.getPersonId())
				.withOrganizationName(obj.getOrganizationName())
				.withOrganizationNumber(obj.getOrganizationNumber())
				.withAuthorizedSignatory(obj.getAuthorizedSignatory())
				.withAdAccount(obj.getAdAccount())
				.withAddresses(new ArrayList<>(ofNullable(obj.getAddresses()).orElse(emptyList()).stream().map(EntityMapper::toAddress).toList()))
				.withContactInformation(new ArrayList<>(ofNullable(obj.getContactInformation()).orElse(emptyList()).stream().map(EntityMapper::toContactInformation).toList()))
				.withRoles(obj.getRoles())
				.withExtraParameters(Optional.of(obj.getExtraParameters()).orElse(new LinkedHashMap<>()))
				.build())
			.orElse(null);
	}

	public static AttachmentEntity toAttachmentEntity(final Long errandId, final Attachment attachment, final String municipalityId, final String namespace) {
		return ofNullable(attachment)
			.map(obj -> AttachmentEntity.builder()
				.withCategory(obj.getCategory())
				.withName(obj.getName())
				.withNote(obj.getNote())
				.withExtension(obj.getExtension())
				.withErrandId(errandId)
				.withMunicipalityId(municipalityId)
				.withNamespace(namespace)
				.withMimeType(obj.getMimeType())
				.withFile(obj.getFile())
				.withExtraParameters(ofNullable(obj.getExtraParameters()).orElse(new LinkedHashMap<>()))
				.build())
			.orElse(null);
	}

	public static Attachment toAttachment(final AttachmentEntity attachmentEntity) {
		return ofNullable(attachmentEntity)
			.map(obj -> Attachment.builder()
				.withId(obj.getId())
				.withMunicipalityId(obj.getMunicipalityId())
				.withNamespace(obj.getNamespace())
				.withVersion(obj.getVersion())
				.withCreated(obj.getCreated())
				.withUpdated(obj.getUpdated())
				.withCategory(obj.getCategory())
				.withName(obj.getName())
				.withNote(obj.getNote())
				.withExtension(obj.getExtension())
				.withMimeType(obj.getMimeType())
				.withErrandId(obj.getErrandId())
				.withFile(obj.getFile())
				.withExtraParameters(ofNullable(obj.getExtraParameters()).orElse(new LinkedHashMap<>()))
				.build())
			.orElse(null);
	}

	public static DecisionEntity toDecisionEntity(final Decision decision, final ErrandEntity errand, final String municipalityId, final String namespace) {
		return ofNullable(decision)
			.map(obj -> DecisionEntity.builder()
				.withErrand(errand)
				.withDecisionType(obj.getDecisionType())
				.withDecisionOutcome(obj.getDecisionOutcome())
				.withDescription(obj.getDescription())
				.withDecidedBy(toStakeholderEntity(obj.getDecidedBy(), municipalityId, namespace))
				.withDecidedAt(obj.getDecidedAt())
				.withValidFrom(obj.getValidFrom())
				.withValidTo(obj.getValidTo())
				.withMunicipalityId(municipalityId)
				.withNamespace(namespace)
				.withLaw(new ArrayList<>(ofNullable(obj.getLaw()).orElse(emptyList()).stream().map(EntityMapper::toLawEntity).toList()))
				.withAttachments(new ArrayList<>(ofNullable(obj.getAttachments()).orElse(emptyList()).stream().map(e -> toAttachmentEntity(errand.getId(), e, municipalityId, namespace)).toList()))
				.withExtraParameters(ofNullable(obj.getExtraParameters()).orElse(new LinkedHashMap<>()))
				.build())
			.orElse(null);
	}

	public static Decision toDecision(final DecisionEntity decisionEntity) {
		return ofNullable(decisionEntity)
			.map(obj -> Decision.builder()
				.withId(obj.getId())
				.withMunicipalityId(obj.getMunicipalityId())
				.withNamespace(obj.getNamespace())
				.withVersion(obj.getVersion())
				.withCreated(obj.getCreated())
				.withUpdated(obj.getUpdated())
				.withDecisionType(obj.getDecisionType())
				.withDecisionOutcome(obj.getDecisionOutcome())
				.withDescription(obj.getDescription())
				.withDecidedBy(toStakeholder(obj.getDecidedBy()))
				.withDecidedAt(obj.getDecidedAt())
				.withValidFrom(obj.getValidFrom())
				.withValidTo(obj.getValidTo())
				.withLaw(new ArrayList<>(obj.getLaw().stream().map(EntityMapper::toLaw).toList()))
				.withAttachments(new ArrayList<>(obj.getAttachments().stream().map(EntityMapper::toAttachment).toList()))
				.withExtraParameters(ofNullable(obj.getExtraParameters()).orElse(new LinkedHashMap<>()))
				.build())
			.orElse(null);
	}

	public static Note toNote(final NoteEntity noteEntity) {
		return ofNullable(noteEntity)
			.map(obj -> Note.builder()
				.withId(obj.getId())
				.withMunicipalityId(obj.getMunicipalityId())
				.withNamespace(obj.getNamespace())
				.withVersion(obj.getVersion())
				.withCreated(obj.getCreated())
				.withUpdated(obj.getUpdated())
				.withTitle(obj.getTitle())
				.withText(obj.getText())
				.withCreatedBy(obj.getCreatedBy())
				.withUpdatedBy(obj.getUpdatedBy())
				.withNoteType(obj.getNoteType())
				.withExtraParameters(ofNullable(obj.getExtraParameters()).orElse(new LinkedHashMap<>()))
				.build())
			.orElse(null);
	}

	public static NoteEntity toNoteEntity(final Note note, final String municipalityId, final String namespace) {
		return Optional.of(note)
			.map(obj -> NoteEntity.builder()
				.withTitle(obj.getTitle())
				.withText(obj.getText())
				.withNoteType(obj.getNoteType())
				.withCreatedBy(obj.getCreatedBy())
				.withUpdatedBy(obj.getUpdatedBy())
				.withNoteType(obj.getNoteType())
				.withMunicipalityId(municipalityId)
				.withNamespace(namespace)
				.withExtraParameters(ofNullable(obj.getExtraParameters()).orElse(new LinkedHashMap<>()))
				.build())
			.orElse(null);
	}

	public static StatusEntity toStatusEntity(final Status status) {
		return ofNullable(status)
			.map(obj -> StatusEntity.builder()
				.withStatusType(obj.getStatusType())
				.withDescription(obj.getDescription())
				.withCreated(now(systemDefault()))
				.build())
			.orElse(null);
	}

	public static Status toStatus(final StatusEntity entity) {
		return ofNullable(entity)
			.map(obj -> Status.builder()
				.withStatusType(obj.getStatusType())
				.withDescription(obj.getDescription())
				.withCreated(obj.getCreated())
				.build())
			.orElse(null);
	}

	public static FacilityEntity toFacilityEntity(final Facility facility, final String municipalityId, final String namespace) {
		return ofNullable(facility)
			.map(obj -> FacilityEntity.builder()
				.withDescription(obj.getDescription())
				.withAddress(toAddressEntity(obj.getAddress()))
				.withFacilityCollectionName(obj.getFacilityCollectionName())
				.withMainFacility(obj.isMainFacility())
				.withFacilityType(obj.getFacilityType())
				.withMunicipalityId(municipalityId)
				.withNamespace(namespace)
				.withExtraParameters(ofNullable(obj.getExtraParameters()).orElse(new LinkedHashMap<>()))
				.build())
			.orElse(null);
	}

	public static Facility toFacility(final FacilityEntity facilityEntity) {
		return ofNullable(facilityEntity)
			.map(obj -> Facility.builder()
				.withId(obj.getId())
				.withMunicipalityId(obj.getMunicipalityId())
				.withNamespace(obj.getNamespace())
				.withVersion(obj.getVersion())
				.withCreated(obj.getCreated())
				.withUpdated(obj.getUpdated())
				.withDescription(obj.getDescription())
				.withAddress(toAddress(obj.getAddress()))
				.withFacilityCollectionName(obj.getFacilityCollectionName())
				.withMainFacility(obj.isMainFacility())
				.withFacilityType(obj.getFacilityType())
				.withExtraParameters(ofNullable(obj.getExtraParameters()).orElse(new LinkedHashMap<>()))
				.build())
			.orElse(null);
	}

	public static CoordinatesEntity toCoordinatesEntity(final Coordinates coordinates) {
		return ofNullable(coordinates)
			.map(obj -> CoordinatesEntity.builder()
				.withLatitude(obj.getLatitude())
				.withLongitude(obj.getLongitude())
				.build())
			.orElse(null);
	}

	public static Coordinates toCoordinates(final CoordinatesEntity coordinatesEntity) {
		return ofNullable(coordinatesEntity)
			.map(obj -> Coordinates.builder()
				.withLatitude(obj.getLatitude())
				.withLongitude(obj.getLongitude())
				.build())
			.orElse(null);
	}

	public static AddressEntity toAddressEntity(final Address address) {
		return ofNullable(address)
			.map(obj -> AddressEntity.builder()
				.withAddressCategory(obj.getAddressCategory())
				.withStreet(obj.getStreet())
				.withHouseNumber(obj.getHouseNumber())
				.withPostalCode(obj.getPostalCode())
				.withCity(obj.getCity())
				.withCountry(obj.getCountry())
				.withCareOf(obj.getCareOf())
				.withAttention(obj.getAttention())
				.withPropertyDesignation(obj.getPropertyDesignation())
				.withApartmentNumber(obj.getApartmentNumber())
				.withIsZoningPlanArea(obj.getIsZoningPlanArea())
				.withInvoiceMarking(obj.getInvoiceMarking())
				.withLocation(toCoordinatesEntity(obj.getLocation()))
				.build())
			.orElse(null);
	}

	public static Address toAddress(final AddressEntity addressEntity) {
		return ofNullable(addressEntity)
			.map(obj -> Address.builder()
				.withAddressCategory(obj.getAddressCategory())
				.withStreet(obj.getStreet())
				.withHouseNumber(obj.getHouseNumber())
				.withPostalCode(obj.getPostalCode())
				.withCity(obj.getCity())
				.withCountry(obj.getCountry())
				.withCareOf(obj.getCareOf())
				.withAttention(obj.getAttention())
				.withPropertyDesignation(obj.getPropertyDesignation())
				.withApartmentNumber(obj.getApartmentNumber())
				.withIsZoningPlanArea(obj.getIsZoningPlanArea())
				.withInvoiceMarking(obj.getInvoiceMarking())
				.withLocation(toCoordinates(obj.getLocation()))
				.build())
			.orElse(null);
	}

	public static ContactInformation toContactInformation(final ContactInformationEntity contactInformationEntity) {
		return ofNullable(contactInformationEntity)
			.map(obj -> ContactInformation.builder()
				.withContactType(obj.getContactType())
				.withValue(obj.getValue()).build())
			.orElse(null);
	}

	public static ContactInformationEntity toContactInformationEntity(final ContactInformation contactInformation) {
		return ofNullable(contactInformation)
			.map(obj -> ContactInformationEntity.builder()
				.withContactType(obj.getContactType())
				.withValue(obj.getValue())
				.build())
			.orElse(null);
	}

	public static LawEntity toLawEntity(final Law law) {
		return ofNullable(law)
			.map(obj -> LawEntity.builder()
				.withArticle(obj.getArticle())
				.withChapter(obj.getChapter())
				.withHeading(obj.getHeading())
				.withSfs(obj.getSfs())
				.build())
			.orElse(null);
	}

	public static Law toLaw(final LawEntity lawEntity) {
		return ofNullable(lawEntity)
			.map(obj -> Law.builder()
				.withArticle(obj.getArticle())
				.withChapter(obj.getChapter())
				.withHeading(obj.getHeading())
				.withSfs(obj.getSfs())
				.build())
			.orElse(null);
	}

	public static NotificationEntity toNotificationEntity(final Notification notification, final String municipalityId, final String namespace, final ErrandEntity errand) {
		return ofNullable(notification)
			.map(obj -> NotificationEntity.builder()
				.withAcknowledged(obj.isAcknowledged())
				.withGlobalAcknowledged(obj.isGlobalAcknowledged())
				.withContent(obj.getContent())
				.withCreatedBy(obj.getCreatedBy())
				.withDescription(obj.getDescription())
				.withExpires(ofNullable(obj.getExpires()).orElse(now().plusDays(DEFAULT_NOTIFICATION_EXPIRATION_TIME_IN_DAYS)))
				.withErrand(errand)
				.withMunicipalityId(municipalityId)
				.withNamespace(namespace)
				.withOwnerId(obj.getOwnerId())
				.withType(obj.getType())
				.withSubType(obj.getSubType())
				.build())
			.orElse(null);
	}

	public static List<Notification> toNotifications(final List<NotificationEntity> notificationEntityList) {
		return new ArrayList<>(Optional.ofNullable(notificationEntityList).orElse(emptyList())
			.stream()
			.filter(notification -> !notification.isGlobalAcknowledged() || !notification.isAcknowledged())
			.map(EntityMapper::toNotification)
			.toList());
	}

	public static Notification toNotification(final NotificationEntity notificationEntity) {
		return ofNullable(notificationEntity)
			.map(obj -> Notification.builder()
				.withAcknowledged(obj.isAcknowledged())
				.withGlobalAcknowledged(obj.isGlobalAcknowledged())
				.withContent(obj.getContent())
				.withCreated(obj.getCreated())
				.withCreatedBy(obj.getCreatedBy())
				.withCreatedByFullName(obj.getCreatedByFullName())
				.withDescription(obj.getDescription())
				.withErrandId(obj.getErrand().getId())
				.withErrandNumber(obj.getErrand().getErrandNumber())
				.withExpires(obj.getExpires())
				.withId(obj.getId())
				.withModified(obj.getModified())
				.withOwnerFullName(obj.getOwnerFullName())
				.withOwnerId(obj.getOwnerId())
				.withType(obj.getType())
				.withSubType(obj.getSubType())
				.withMunicipalityId(obj.getMunicipalityId())
				.withNamespace(obj.getNamespace())
				.build())
			.orElse(null);
	}

	public static String toOwnerId(final ErrandEntity errandEntity) {
		return ofNullable(errandEntity.getStakeholders()).orElse(emptyList()).stream()
			.filter(stakeholder -> ofNullable(stakeholder.getRoles()).orElse(emptyList()).stream()
				.anyMatch(ADMINISTRATOR.toString()::equalsIgnoreCase))
			.findFirst()
			.map(StakeholderEntity::getAdAccount)
			.orElse(null);
	}

	public static RelatedErrand toRelatedErrand(final RelatedErrandEntity relatedErrandEntity) {
		return ofNullable(relatedErrandEntity)
			.map(obj -> RelatedErrand.builder()
				.withErrandNumber(obj.getRelatedErrandNumber())
				.withErrandId(obj.getRelatedErrandId())
				.withRelationReason(obj.getRelationReason())
				.build())
			.orElse(null);
	}

	public static RelatedErrandEntity toRelatedErrandEntity(final RelatedErrand relatedErrand) {
		return ofNullable(relatedErrand)
			.map(obj -> RelatedErrandEntity.builder()
				.withRelatedErrandNumber(obj.getErrandNumber())
				.withRelatedErrandId(obj.getErrandId())
				.withRelationReason(obj.getRelationReason())
				.build())
			.orElse(null);
	}

	public static Notification toNotification(final ErrandEntity errand, final String type, final String description, final NotificationSubType subType) {

		final var stakeholder = errand.getStakeholders().stream()
			.filter(stakeholderEntity -> stakeholderEntity.getRoles().contains(ADMINISTRATOR.name()))
			.findFirst()
			.orElse(new StakeholderEntity());

		return Notification.builder()
			.withOwnerId(stakeholder.getAdAccount())
			.withType(type)
			.withSubType(subType.name())
			.withDescription(description)
			.withErrandId(errand.getId())
			.withMunicipalityId(errand.getMunicipalityId())
			.withNamespace(errand.getNamespace())
			.build();
	}
}
