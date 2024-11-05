package se.sundsvall.casedata.service.util.mappers;

import static java.time.OffsetDateTime.now;
import static java.util.Collections.emptyList;
import static se.sundsvall.casedata.api.model.validation.enums.StakeholderRole.ADMINISTRATOR;
import static se.sundsvall.casedata.service.util.mappers.ErrandExtraParameterMapper.toErrandParameterEntityList;
import static se.sundsvall.casedata.service.util.mappers.ErrandExtraParameterMapper.toParameterList;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Optional;

import generated.se.sundsvall.employee.PortalPersonData;
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

public final class EntityMapper {

	private static final int DEFAULT_NOTIFICATION_EXPIRATION_TIME_IN_DAYS = 30;

	private EntityMapper() {}

	public static Errand toErrand(final ErrandEntity errandEntity) {
		return Optional.ofNullable(errandEntity)
			.map(obj -> Errand.builder()
				.withId(errandEntity.getId())
				.withVersion(errandEntity.getVersion())
				.withCreated(errandEntity.getCreated())
				.withUpdated(errandEntity.getUpdated())
				.withErrandNumber(errandEntity.getErrandNumber())
				.withExternalCaseId(errandEntity.getExternalCaseId())
				.withCaseType(errandEntity.getCaseType())
				.withChannel(errandEntity.getChannel())
				.withPriority(errandEntity.getPriority())
				.withDescription(errandEntity.getDescription())
				.withCaseTitleAddition(errandEntity.getCaseTitleAddition())
				.withDiaryNumber(errandEntity.getDiaryNumber())
				.withPhase(errandEntity.getPhase())
				.withStartDate(errandEntity.getStartDate())
				.withEndDate(errandEntity.getEndDate())
				.withApplicationReceived(errandEntity.getApplicationReceived())
				.withProcessId(errandEntity.getProcessId())
				.withCreatedByClient(errandEntity.getCreatedByClient())
				.withUpdatedByClient(errandEntity.getUpdatedByClient())
				.withCreatedBy(errandEntity.getCreatedBy())
				.withUpdatedBy(errandEntity.getUpdatedBy())
				.withMunicipalityId(errandEntity.getMunicipalityId())
				.withNamespace(errandEntity.getNamespace())
				.withSuspension(Suspension.builder().withSuspendedFrom(errandEntity.getSuspendedFrom()).withSuspendedTo(errandEntity.getSuspendedTo()).build())
				.withNotes(new ArrayList<>(errandEntity.getNotes().stream().map(EntityMapper::toNote).toList()))
				.withStatuses(new ArrayList<>(errandEntity.getStatuses().stream().map(EntityMapper::toStatus).toList()))
				.withStakeholders(new ArrayList<>(errandEntity.getStakeholders().stream().map(EntityMapper::toStakeholder).toList()))
				.withFacilities(new ArrayList<>(errandEntity.getFacilities().stream().map(EntityMapper::toFacility).toList()))
				.withDecisions(new ArrayList<>(errandEntity.getDecisions().stream().map(EntityMapper::toDecision).toList()))
				.withRelatesTo(new ArrayList<>(errandEntity.getRelatesTo().stream().map(EntityMapper::toRelatedErrand).toList()))
				.withExtraParameters(toParameterList(errandEntity.getExtraParameters()))
				.withLabels(errandEntity.getLabels())
				.build())
			.orElse(null);
	}

	public static ErrandEntity toErrandEntity(final Errand errand, final String municipalityId, final String namespace) {
		final var errandEntity = Optional.ofNullable(errand)
			.map(obj -> ErrandEntity.builder()
				.withId(errand.getId())
				.withVersion(errand.getVersion())
				.withCreated(errand.getCreated())
				.withUpdated(errand.getUpdated())
				.withErrandNumber(errand.getErrandNumber())
				.withExternalCaseId(errand.getExternalCaseId())
				.withCaseType(errand.getCaseType())
				.withChannel(errand.getChannel())
				.withPriority(errand.getPriority())
				.withDescription(errand.getDescription())
				.withCaseTitleAddition(errand.getCaseTitleAddition())
				.withDiaryNumber(errand.getDiaryNumber())
				.withPhase(errand.getPhase())
				.withStartDate(errand.getStartDate())
				.withEndDate(errand.getEndDate())
				.withApplicationReceived(errand.getApplicationReceived())
				.withProcessId(errand.getProcessId())
				.withCreatedByClient(errand.getCreatedByClient())
				.withUpdatedByClient(errand.getUpdatedByClient())
				.withCreatedBy(errand.getCreatedBy())
				.withMunicipalityId(municipalityId)
				.withNamespace(namespace)
				.withUpdatedBy(errand.getUpdatedBy())
				.withSuspendedFrom(Optional.ofNullable(errand.getSuspension()).map(Suspension::getSuspendedFrom).orElse(null))
				.withSuspendedTo(Optional.ofNullable(errand.getSuspension()).map(Suspension::getSuspendedTo).orElse(null))
				.withStatuses(new ArrayList<>(Optional.ofNullable(errand.getStatuses())
					.orElse(emptyList())
					.stream()
					.map(EntityMapper::toStatusEntity)
					.toList()))
				.withStakeholders(new ArrayList<>(Optional.ofNullable(errand.getStakeholders())
					.orElse(emptyList())
					.stream().map(stakeholderDTO -> toStakeholderEntity(stakeholderDTO, municipalityId, namespace))
					.toList()))
				.withFacilities(new ArrayList<>(Optional.ofNullable(errand.getFacilities())
					.orElse(emptyList())
					.stream().map(facilityDTO -> toFacilityEntity(facilityDTO, municipalityId, namespace))
					.toList()))
				.withNotes(new ArrayList<>(Optional.ofNullable(errand.getNotes())
					.orElse(emptyList())
					.stream().map(notesDTO -> toNoteEntity(notesDTO, municipalityId, namespace))
					.toList()))
				.withRelatesTo(new ArrayList<>(Optional.ofNullable(errand.getRelatesTo())
					.orElse(emptyList())
					.stream().map(EntityMapper::toRelatedErrandEntity)
					.toList()))
				.withLabels(errand.getLabels())
				.build());

		errandEntity.ifPresent(entity -> {
			entity.setDecisions(new ArrayList<>(Optional.ofNullable(errand.getDecisions())
				.orElse(emptyList())
				.stream().map(decisionDTO -> toDecisionEntity(decisionDTO, entity, municipalityId, namespace))
				.toList()));
			entity.getStakeholders().forEach(stakeholder -> stakeholder.setErrand(entity));
			entity.getFacilities().forEach(facility -> facility.setErrand(entity));
			entity.getDecisions().forEach(decision -> decision.setErrand(entity));
			entity.getNotes().forEach(note -> note.setErrand(entity));
			entity.setExtraParameters(toErrandParameterEntityList(errand.getExtraParameters(), entity));
		});

		return errandEntity.orElse(null);
	}

	public static StakeholderEntity toStakeholderEntity(final Stakeholder stakeholder, final String municipalityId, final String namespace) {
		return Optional.ofNullable(stakeholder)
			.map(obj -> StakeholderEntity.builder()
				.withId(stakeholder.getId())
				.withVersion(stakeholder.getVersion())
				.withCreated(stakeholder.getCreated())
				.withUpdated(stakeholder.getUpdated())
				.withType(stakeholder.getType())
				.withFirstName(stakeholder.getFirstName())
				.withLastName(stakeholder.getLastName())
				.withPersonId(stakeholder.getPersonId())
				.withOrganizationName(stakeholder.getOrganizationName())
				.withOrganizationNumber(stakeholder.getOrganizationNumber())
				.withAuthorizedSignatory(stakeholder.getAuthorizedSignatory())
				.withAdAccount(stakeholder.getAdAccount())
				.withMunicipalityId(municipalityId)
				.withNamespace(namespace)
				.withAddresses(new ArrayList<>(stakeholder.getAddresses().stream().map(EntityMapper::toAddressEntity).toList()))
				.withContactInformation(new ArrayList<>(stakeholder.getContactInformation().stream().map(EntityMapper::toContactInformationEntity).toList()))
				.withRoles(stakeholder.getRoles())
				.withExtraParameters(Optional.of(stakeholder.getExtraParameters()).orElse(new LinkedHashMap<>()))
				.build())
			.orElse(null);
	}

	public static Stakeholder toStakeholder(final StakeholderEntity stakeholderEntity) {
		return Optional.ofNullable(stakeholderEntity)
			.map(obj -> Stakeholder.builder()
				.withId(stakeholderEntity.getId())
				.withMunicipalityId(stakeholderEntity.getMunicipalityId())
				.withNamespace(stakeholderEntity.getNamespace())
				.withVersion(stakeholderEntity.getVersion())
				.withCreated(stakeholderEntity.getCreated())
				.withUpdated(stakeholderEntity.getUpdated())
				.withType(stakeholderEntity.getType())
				.withFirstName(stakeholderEntity.getFirstName())
				.withLastName(stakeholderEntity.getLastName())
				.withPersonId(stakeholderEntity.getPersonId())
				.withOrganizationName(stakeholderEntity.getOrganizationName())
				.withOrganizationNumber(stakeholderEntity.getOrganizationNumber())
				.withAuthorizedSignatory(stakeholderEntity.getAuthorizedSignatory())
				.withAdAccount(stakeholderEntity.getAdAccount())
				.withAddresses(new ArrayList<>(stakeholderEntity.getAddresses().stream().map(EntityMapper::toAddress).toList()))
				.withContactInformation(new ArrayList<>(stakeholderEntity.getContactInformation().stream().map(EntityMapper::toContactInformation).toList()))
				.withRoles(stakeholderEntity.getRoles())
				.withExtraParameters(Optional.of(stakeholderEntity.getExtraParameters()).orElse(new LinkedHashMap<>()))
				.build())
			.orElse(null);
	}

	public static AttachmentEntity toAttachmentEntity(final Attachment attachment, final String municipalityId, final String namespace) {
		return Optional.ofNullable(attachment)
			.map(obj -> AttachmentEntity.builder()
				.withId(attachment.getId())
				.withVersion(attachment.getVersion())
				.withCreated(attachment.getCreated())
				.withUpdated(attachment.getUpdated())
				.withCategory(attachment.getCategory())
				.withName(attachment.getName())
				.withNote(attachment.getNote())
				.withExtension(attachment.getExtension())
				.withErrandNumber(attachment.getErrandNumber())
				.withMunicipalityId(municipalityId)
				.withNamespace(namespace)
				.withMimeType(attachment.getMimeType())
				.withFile(attachment.getFile())
				.withExtraParameters(Optional.ofNullable(attachment.getExtraParameters()).orElse(new LinkedHashMap<>()))
				.build())
			.orElse(null);
	}

	public static Attachment toAttachment(final AttachmentEntity attachmentEntity) {
		return Optional.ofNullable(attachmentEntity)
			.map(obj -> Attachment.builder()
				.withId(attachmentEntity.getId())
				.withMunicipalityId(attachmentEntity.getMunicipalityId())
				.withNamespace(attachmentEntity.getNamespace())
				.withVersion(attachmentEntity.getVersion())
				.withCreated(attachmentEntity.getCreated())
				.withUpdated(attachmentEntity.getUpdated())
				.withCategory(attachmentEntity.getCategory())
				.withName(attachmentEntity.getName())
				.withNote(attachmentEntity.getNote())
				.withExtension(attachmentEntity.getExtension())
				.withMimeType(attachmentEntity.getMimeType())
				.withErrandNumber(attachmentEntity.getErrandNumber())
				.withFile(attachmentEntity.getFile())
				.withExtraParameters(Optional.ofNullable(attachmentEntity.getExtraParameters()).orElse(new LinkedHashMap<>()))
				.build())
			.orElse(null);
	}

	public static DecisionEntity toDecisionEntity(final Decision decision, final ErrandEntity errand, final String municipalityId, final String namespace) {
		return Optional.ofNullable(decision)
			.map(obj -> DecisionEntity.builder()
				.withId(decision.getId())
				.withVersion(decision.getVersion())
				.withCreated(decision.getCreated())
				.withUpdated(decision.getUpdated())
				.withErrand(errand)
				.withDecisionType(decision.getDecisionType())
				.withDecisionOutcome(decision.getDecisionOutcome())
				.withDescription(decision.getDescription())
				.withDecidedBy(toStakeholderEntity(decision.getDecidedBy(), municipalityId, namespace))
				.withDecidedAt(decision.getDecidedAt())
				.withValidFrom(decision.getValidFrom())
				.withValidTo(decision.getValidTo())
				.withMunicipalityId(municipalityId)
				.withNamespace(namespace)
				.withLaw(new ArrayList<>(decision.getLaw().stream().map(EntityMapper::toLawEntity).toList()))
				.withAttachments(new ArrayList<>(decision.getAttachments().stream().map(e -> toAttachmentEntity(e, municipalityId, namespace)).toList()))
				.withExtraParameters(Optional.ofNullable(decision.getExtraParameters()).orElse(new LinkedHashMap<>()))
				.build())
			.orElse(null);
	}

	public static Decision toDecision(final DecisionEntity decisionEntity) {
		return Optional.ofNullable(decisionEntity)
			.map(obj -> Decision.builder()
				.withId(decisionEntity.getId())
				.withMunicipalityId(decisionEntity.getMunicipalityId())
				.withNamespace(decisionEntity.getNamespace())
				.withVersion(decisionEntity.getVersion())
				.withCreated(decisionEntity.getCreated())
				.withUpdated(decisionEntity.getUpdated())
				.withDecisionType(decisionEntity.getDecisionType())
				.withDecisionOutcome(decisionEntity.getDecisionOutcome())
				.withDescription(decisionEntity.getDescription())
				.withDecidedBy(toStakeholder(decisionEntity.getDecidedBy()))
				.withDecidedAt(decisionEntity.getDecidedAt())
				.withValidFrom(decisionEntity.getValidFrom())
				.withValidTo(decisionEntity.getValidTo())
				.withLaw(new ArrayList<>(decisionEntity.getLaw().stream().map(EntityMapper::toLaw).toList()))
				.withAttachments(new ArrayList<>(decisionEntity.getAttachments().stream().map(EntityMapper::toAttachment).toList()))
				.withExtraParameters(Optional.ofNullable(decisionEntity.getExtraParameters()).orElse(new LinkedHashMap<>()))
				.build())
			.orElse(null);
	}

	public static Note toNote(final NoteEntity noteEntity) {
		return Optional.ofNullable(noteEntity)
			.map(obj -> Note.builder()
				.withId(noteEntity.getId())
				.withMunicipalityId(noteEntity.getMunicipalityId())
				.withNamespace(noteEntity.getNamespace())
				.withVersion(noteEntity.getVersion())
				.withCreated(noteEntity.getCreated())
				.withUpdated(noteEntity.getUpdated())
				.withTitle(noteEntity.getTitle())
				.withText(noteEntity.getText())
				.withNoteType(noteEntity.getNoteType())
				.withCreatedBy(noteEntity.getCreatedBy())
				.withUpdatedBy(noteEntity.getUpdatedBy())
				.withNoteType(noteEntity.getNoteType())
				.withExtraParameters(Optional.ofNullable(noteEntity.getExtraParameters()).orElse(new LinkedHashMap<>()))
				.build())
			.orElse(null);
	}

	public static NoteEntity toNoteEntity(final Note note, final String municipalityId, final String namespace) {
		return Optional.of(note)
			.map(obj -> NoteEntity.builder()
				.withId(note.getId())
				.withVersion(note.getVersion())
				.withCreated(note.getCreated())
				.withUpdated(note.getUpdated())
				.withTitle(note.getTitle())
				.withText(note.getText())
				.withNoteType(note.getNoteType())
				.withCreatedBy(note.getCreatedBy())
				.withUpdatedBy(note.getUpdatedBy())
				.withNoteType(note.getNoteType())
				.withMunicipalityId(municipalityId)
				.withNamespace(namespace)
				.withExtraParameters(Optional.ofNullable(note.getExtraParameters()).orElse(new LinkedHashMap<>()))
				.build())
			.orElse(null);
	}

	public static StatusEntity toStatusEntity(final Status status) {
		return Optional.ofNullable(status)
			.map(obj -> StatusEntity.builder()
				.withStatusType(status.getStatusType())
				.withDescription(status.getDescription())
				.withDateTime(status.getDateTime())
				.build())
			.orElse(null);
	}

	public static Status toStatus(final StatusEntity entity) {
		return Optional.ofNullable(entity)
			.map(obj -> Status.builder()
				.withStatusType(entity.getStatusType())
				.withDescription(entity.getDescription())
				.withDateTime(entity.getDateTime())
				.build())
			.orElse(null);
	}

	public static FacilityEntity toFacilityEntity(final Facility facility, final String municipalityId, final String namespace) {
		return Optional.ofNullable(facility)
			.map(obj -> FacilityEntity.builder()
				.withId(facility.getId())
				.withVersion(facility.getVersion())
				.withCreated(facility.getCreated())
				.withUpdated(facility.getUpdated())
				.withDescription(facility.getDescription())
				.withAddressEntity(toAddressEntity(facility.getAddress()))
				.withFacilityCollectionName(facility.getFacilityCollectionName())
				.withMainFacility(facility.isMainFacility())
				.withFacilityType(facility.getFacilityType())
				.withMunicipalityId(municipalityId)
				.withNamespace(namespace)
				.withExtraParameters(Optional.ofNullable(facility.getExtraParameters()).orElse(new LinkedHashMap<>()))
				.build())
			.orElse(null);
	}

	public static Facility toFacility(final FacilityEntity facilityEntity) {
		return Optional.ofNullable(facilityEntity)
			.map(obj -> Facility.builder()
				.withId(facilityEntity.getId())
				.withMunicipalityId(facilityEntity.getMunicipalityId())
				.withNamespace(facilityEntity.getNamespace())
				.withVersion(facilityEntity.getVersion())
				.withCreated(facilityEntity.getCreated())
				.withUpdated(facilityEntity.getUpdated())
				.withDescription(facilityEntity.getDescription())
				.withAddress(toAddress(facilityEntity.getAddressEntity()))
				.withFacilityCollectionName(facilityEntity.getFacilityCollectionName())
				.withMainFacility(facilityEntity.isMainFacility())
				.withFacilityType(facilityEntity.getFacilityType())
				.withExtraParameters(Optional.ofNullable(facilityEntity.getExtraParameters()).orElse(new LinkedHashMap<>()))
				.build())
			.orElse(null);
	}

	public static CoordinatesEntity toCoordinatesEntity(final Coordinates coordinates) {
		return Optional.ofNullable(coordinates)
			.map(obj -> CoordinatesEntity.builder()
				.withLatitude(coordinates.getLatitude())
				.withLongitude(coordinates.getLongitude())
				.build())
			.orElse(null);
	}

	public static Coordinates toCoordinates(final CoordinatesEntity coordinatesEntity) {
		return Optional.ofNullable(coordinatesEntity)
			.map(obj -> Coordinates.builder()
				.withLatitude(coordinatesEntity.getLatitude())
				.withLongitude(coordinatesEntity.getLongitude())
				.build())
			.orElse(null);
	}

	public static AddressEntity toAddressEntity(final Address address) {
		return Optional.ofNullable(address)
			.map(obj -> AddressEntity.builder()
				.withAddressCategory(address.getAddressCategory())
				.withStreet(address.getStreet())
				.withHouseNumber(address.getHouseNumber())
				.withPostalCode(address.getPostalCode())
				.withCity(address.getCity())
				.withCountry(address.getCountry())
				.withCareOf(address.getCareOf())
				.withAttention(address.getAttention())
				.withPropertyDesignation(address.getPropertyDesignation())
				.withApartmentNumber(address.getApartmentNumber())
				.withIsZoningPlanArea(address.getIsZoningPlanArea())
				.withInvoiceMarking(address.getInvoiceMarking())
				.withLocation(toCoordinatesEntity(address.getLocation()))
				.build())
			.orElse(null);
	}

	public static Address toAddress(final AddressEntity addressEntity) {
		return Optional.ofNullable(addressEntity)
			.map(obj -> Address.builder()
				.withAddressCategory(addressEntity.getAddressCategory())
				.withStreet(addressEntity.getStreet())
				.withHouseNumber(addressEntity.getHouseNumber())
				.withPostalCode(addressEntity.getPostalCode())
				.withCity(addressEntity.getCity())
				.withCountry(addressEntity.getCountry())
				.withCareOf(addressEntity.getCareOf())
				.withAttention(addressEntity.getAttention())
				.withPropertyDesignation(addressEntity.getPropertyDesignation())
				.withApartmentNumber(addressEntity.getApartmentNumber())
				.withIsZoningPlanArea(addressEntity.getIsZoningPlanArea())
				.withInvoiceMarking(addressEntity.getInvoiceMarking())
				.withLocation(toCoordinates(addressEntity.getLocation()))
				.build())
			.orElse(null);
	}

	public static ContactInformation toContactInformation(final ContactInformationEntity contactInformationEntity) {
		return Optional.ofNullable(contactInformationEntity)
			.map(obj -> ContactInformation.builder()
				.withContactType(contactInformationEntity.getContactType())
				.withValue(contactInformationEntity.getValue()).build())
			.orElse(null);
	}

	public static ContactInformationEntity toContactInformationEntity(final ContactInformation contactInformation) {
		return Optional.ofNullable(contactInformation)
			.map(obj -> ContactInformationEntity.builder()
				.withContactType(contactInformation.getContactType())
				.withValue(contactInformation.getValue())
				.build())
			.orElse(null);
	}

	public static LawEntity toLawEntity(final Law law) {
		return Optional.ofNullable(law)
			.map(obj -> LawEntity.builder()
				.withArticle(law.getArticle())
				.withChapter(law.getChapter())
				.withHeading(law.getHeading())
				.withSfs(law.getSfs())
				.build())
			.orElse(null);
	}

	public static Law toLaw(final LawEntity lawEntity) {
		return Optional.ofNullable(lawEntity)
			.map(obj -> Law.builder()
				.withArticle(lawEntity.getArticle())
				.withChapter(lawEntity.getChapter())
				.withHeading(lawEntity.getHeading())
				.withSfs(lawEntity.getSfs())
				.build())
			.orElse(null);
	}

	public static NotificationEntity toNotificationEntity(final Notification notification, final String municipalityId, final String namespace, final ErrandEntity errand, final PortalPersonData creator, final PortalPersonData owner) {
		return Optional.ofNullable(notification)
			.map(obj -> NotificationEntity.builder()
				.withAcknowledged(notification.isAcknowledged())
				.withContent(notification.getContent())
				.withCreated(notification.getCreated())
				.withCreatedBy(notification.getCreatedBy())
				.withCreatedByFullName(Optional.ofNullable(creator).map(PortalPersonData::getFullname).orElse("unknown"))
				.withDescription(notification.getDescription())
				.withExpires(Optional.ofNullable(notification.getExpires()).orElse(now().plusDays(DEFAULT_NOTIFICATION_EXPIRATION_TIME_IN_DAYS)))
				.withErrand(errand)
				.withId(notification.getId())
				.withModified(notification.getModified())
				.withMunicipalityId(municipalityId)
				.withNamespace(namespace)
				.withOwnerFullName(Optional.ofNullable(owner).map(PortalPersonData::getFullname).orElse("unknown"))
				.withOwnerId(notification.getOwnerId())
				.withType(notification.getType())
				.build())
			.orElse(null);
	}

	public static Notification toNotification(final NotificationEntity notificationEntity) {
		return Optional.ofNullable(notificationEntity)
			.map(obj -> Notification.builder()
				.withAcknowledged(notificationEntity.isAcknowledged())
				.withContent(notificationEntity.getContent())
				.withCreated(notificationEntity.getCreated())
				.withCreatedBy(notificationEntity.getCreatedBy())
				.withCreatedByFullName(notificationEntity.getCreatedByFullName())
				.withDescription(notificationEntity.getDescription())
				.withErrandId(notificationEntity.getErrand().getId())
				.withErrandNumber(notificationEntity.getErrand().getErrandNumber())
				.withExpires(notificationEntity.getExpires())
				.withId(notificationEntity.getId())
				.withModified(notificationEntity.getModified())
				.withOwnerFullName(notificationEntity.getOwnerFullName())
				.withOwnerId(notificationEntity.getOwnerId())
				.withType(notificationEntity.getType())
				.withMunicipalityId(notificationEntity.getMunicipalityId())
				.withNamespace(notificationEntity.getNamespace())
				.build())
			.orElse(null);
	}

	public static String toOwnerId(final ErrandEntity errandEntity) {
		return Optional.ofNullable(errandEntity.getStakeholders()).orElse(emptyList()).stream()
			.filter(stakeholder -> Optional.ofNullable(stakeholder.getRoles()).orElse(emptyList()).stream()
				.anyMatch(ADMINISTRATOR.toString()::equalsIgnoreCase))
			.findFirst()
			.map(StakeholderEntity::getAdAccount)
			.orElse(null);
	}

	public static RelatedErrand toRelatedErrand(final RelatedErrandEntity relatedErrandEntity) {
		return Optional.ofNullable(relatedErrandEntity)
			.map(obj -> RelatedErrand.builder()
				.withErrandNumber(relatedErrandEntity.getRelatedErrandNumber())
				.withErrandId(relatedErrandEntity.getRelatedErrandId())
				.withRelationReason(relatedErrandEntity.getRelationReason())
				.build())
			.orElse(null);
	}

	public static RelatedErrandEntity toRelatedErrandEntity(final RelatedErrand relatedErrand) {
		return Optional.ofNullable(relatedErrand)
			.map(obj -> RelatedErrandEntity.builder()
				.withRelatedErrandNumber(relatedErrand.getErrandNumber())
				.withRelatedErrandId(relatedErrand.getErrandId())
				.withRelationReason(relatedErrand.getRelationReason())
				.build())
			.orElse(null);
	}

}
