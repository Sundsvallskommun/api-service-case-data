package se.sundsvall.casedata.service.util.mappers;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Optional;

import se.sundsvall.casedata.api.model.AddressDTO;
import se.sundsvall.casedata.api.model.AppealDTO;
import se.sundsvall.casedata.api.model.AttachmentDTO;
import se.sundsvall.casedata.api.model.ContactInformationDTO;
import se.sundsvall.casedata.api.model.CoordinatesDTO;
import se.sundsvall.casedata.api.model.DecisionDTO;
import se.sundsvall.casedata.api.model.ErrandDTO;
import se.sundsvall.casedata.api.model.FacilityDTO;
import se.sundsvall.casedata.api.model.LawDTO;
import se.sundsvall.casedata.api.model.NoteDTO;
import se.sundsvall.casedata.api.model.StakeholderDTO;
import se.sundsvall.casedata.api.model.StatusDTO;
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
import se.sundsvall.casedata.integration.db.model.enums.AppealStatus;
import se.sundsvall.casedata.integration.db.model.enums.TimelinessReview;

public final class EntityMapper {

	private EntityMapper() {}

	public static ErrandDTO toErrandDto(final Errand errand) {
		return Optional.ofNullable(errand)
			.map(dto -> ErrandDTO.builder()
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
				.withMunicipalityId(errand.getMunicipalityId())
				.withStartDate(errand.getStartDate())
				.withEndDate(errand.getEndDate())
				.withApplicationReceived(errand.getApplicationReceived())
				.withProcessId(errand.getProcessId())
				.withCreatedByClient(errand.getCreatedByClient())
				.withUpdatedByClient(errand.getUpdatedByClient())
				.withCreatedBy(errand.getCreatedBy())
				.withUpdatedBy(errand.getUpdatedBy())
				.withMunicipalityId(errand.getMunicipalityId())
				.withNotes(new ArrayList<>(errand.getNotes().stream().map(EntityMapper::toNoteDto).toList()))
				.withStatuses(new ArrayList<>(errand.getStatuses().stream().map(EntityMapper::toStatusDto).toList()))
				.withStakeholders(new ArrayList<>(errand.getStakeholders().stream().map(EntityMapper::toStakeholderDto).toList()))
				.withFacilities(new ArrayList<>(errand.getFacilities().stream().map(EntityMapper::toFacilityDto).toList()))
				.withDecisions(new ArrayList<>(errand.getDecisions().stream().map(EntityMapper::toDecisionDto).toList()))
				.withAppeals(new ArrayList<>(errand.getAppeals().stream().map(EntityMapper::toAppealDto).toList()))
				.withExtraParameters(Optional.of(errand.getExtraParameters()).orElse(new LinkedHashMap<>()))
				.build())
			.orElse(null);
	}

	public static Errand toErrand(final ErrandDTO dto, final String municipalityId) {
		final var errand = Optional.ofNullable(dto)
			.map(errand1 -> Errand.builder()
				.withId(dto.getId())
				.withVersion(dto.getVersion())
				.withCreated(dto.getCreated())
				.withUpdated(dto.getUpdated())
				.withErrandNumber(dto.getErrandNumber())
				.withExternalCaseId(dto.getExternalCaseId())
				.withCaseType(dto.getCaseType())
				.withChannel(dto.getChannel())
				.withPriority(dto.getPriority())
				.withDescription(dto.getDescription())
				.withCaseTitleAddition(dto.getCaseTitleAddition())
				.withDiaryNumber(dto.getDiaryNumber())
				.withPhase(dto.getPhase())
				.withMunicipalityId(dto.getMunicipalityId())
				.withStartDate(dto.getStartDate())
				.withEndDate(dto.getEndDate())
				.withApplicationReceived(dto.getApplicationReceived())
				.withProcessId(dto.getProcessId())
				.withCreatedByClient(dto.getCreatedByClient())
				.withUpdatedByClient(dto.getUpdatedByClient())
				.withCreatedBy(dto.getCreatedBy())
				.withMunicipalityId(municipalityId)
				.withUpdatedBy(dto.getUpdatedBy())
				.withExtraParameters(Optional.of(dto.getExtraParameters()).orElse(new LinkedHashMap<>()))
				.withStatuses(new ArrayList<>(dto.getStatuses().stream().map(EntityMapper::toStatus).toList()))
				.withStakeholders(new ArrayList<>(dto.getStakeholders().stream().map(stakeholderDTO -> toStakeholder(stakeholderDTO, municipalityId)).toList()))
				.withFacilities(new ArrayList<>(dto.getFacilities().stream().map(facilityDTO -> toFacility(facilityDTO, municipalityId)).toList()))
				.withDecisions(new ArrayList<>(dto.getDecisions().stream().map(decisionDTO -> toDecision(decisionDTO, municipalityId)).toList()))
				.withNotes(new ArrayList<>(dto.getNotes().stream().map(notesDTO -> toNote(notesDTO, municipalityId)).toList()))
				.withAppeals(new ArrayList<>(dto.getAppeals().stream().map(appealsDTO -> toAppeal(appealsDTO, municipalityId)).toList()))
				.build());

		errand.ifPresent(errand1 -> {
			errand1.getStakeholders().forEach(stakeholder -> stakeholder.setErrand(errand1));
			errand1.getFacilities().forEach(facility -> facility.setErrand(errand1));
			errand1.getDecisions().forEach(decision -> decision.setErrand(errand1));
			errand1.getNotes().forEach(note -> note.setErrand(errand1));
			errand1.getAppeals().forEach(appeal -> appeal.setErrand(errand1));
		});

		return errand.orElse(null);
	}

	public static Stakeholder toStakeholder(final StakeholderDTO dto, final String municipalityId) {
		return Optional.ofNullable(dto)
			.map(stakeholder -> Stakeholder.builder()
				.withId(dto.getId())
				.withVersion(dto.getVersion())
				.withCreated(dto.getCreated())
				.withUpdated(dto.getUpdated())
				.withType(dto.getType())
				.withFirstName(dto.getFirstName())
				.withLastName(dto.getLastName())
				.withPersonId(dto.getPersonId())
				.withOrganizationName(dto.getOrganizationName())
				.withOrganizationNumber(dto.getOrganizationNumber())
				.withAuthorizedSignatory(dto.getAuthorizedSignatory())
				.withAdAccount(dto.getAdAccount())
				.withMunicipalityId(municipalityId)
				.withAddresses(new ArrayList<>(dto.getAddresses().stream().map(EntityMapper::toAddress).toList()))
				.withContactInformation(new ArrayList<>(dto.getContactInformation().stream().map(EntityMapper::toContactInformation).toList()))
				.withRoles(dto.getRoles())
				.withExtraParameters(Optional.of(dto.getExtraParameters()).orElse(new LinkedHashMap<>()))
				.build())
			.orElse(null);
	}

	public static StakeholderDTO toStakeholderDto(final Stakeholder stakeholder) {
		return Optional.ofNullable(stakeholder).map(dto -> StakeholderDTO.builder()
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
				.withMunicipalityId(stakeholder.getMunicipalityId())
				.withAddresses(new ArrayList<>(stakeholder.getAddresses().stream().map(EntityMapper::toAddressDto).toList()))
				.withContactInformation(new ArrayList<>(stakeholder.getContactInformation().stream().map(EntityMapper::toContactInformationDto).toList()))
				.withRoles(stakeholder.getRoles())
				.withExtraParameters(Optional.of(stakeholder.getExtraParameters()).orElse(new LinkedHashMap<>()))
				.build())
			.orElse(null);
	}

	public static Attachment toAttachment(final AttachmentDTO dto, final String municipalityId) {
		return Optional.ofNullable(dto).map(attachment -> Attachment.builder()
				.withId(dto.getId())
				.withVersion(dto.getVersion())
				.withCreated(dto.getCreated())
				.withUpdated(dto.getUpdated())
				.withCategory(dto.getCategory())
				.withName(dto.getName())
				.withNote(dto.getNote())
				.withExtension(dto.getExtension())
				.withErrandNumber(dto.getErrandNumber())
				.withMunicipalityId(municipalityId)
				.withMimeType(dto.getMimeType())
				.withFile(dto.getFile())
				.withExtraParameters(Optional.ofNullable(dto.getExtraParameters()).orElse(new LinkedHashMap<>()))
				.build())
			.orElse(null);
	}

	public static AttachmentDTO toAttachmentDto(final Attachment attachment) {
		return Optional.ofNullable(attachment).map(dto -> AttachmentDTO.builder()
				.withId(attachment.getId())
				.withVersion(attachment.getVersion())
				.withCreated(attachment.getCreated())
				.withUpdated(attachment.getUpdated())
				.withCategory(dto.getCategory())
				.withName(attachment.getName())
				.withNote(attachment.getNote())
				.withExtension(attachment.getExtension())
				.withMimeType(attachment.getMimeType())
				.withErrandNumber(attachment.getErrandNumber())
				.withFile(attachment.getFile())
				.withMunicipalityId(attachment.getMunicipalityId())
				.withExtraParameters(Optional.ofNullable(attachment.getExtraParameters()).orElse(new LinkedHashMap<>()))
				.build())
			.orElse(null);
	}

	public static Decision toDecision(final DecisionDTO dto, final String municipalityId) {
		return Optional.ofNullable(dto).map(decision -> Decision.builder()
				.withId(dto.getId())
				.withVersion(dto.getVersion())
				.withCreated(dto.getCreated())
				.withUpdated(dto.getUpdated())
				.withDecisionType(dto.getDecisionType())
				.withDecisionOutcome(dto.getDecisionOutcome())
				.withDescription(dto.getDescription())
				.withDecidedBy(toStakeholder(dto.getDecidedBy(), municipalityId))
				.withDecidedAt(dto.getDecidedAt())
				.withValidFrom(dto.getValidFrom())
				.withValidTo(dto.getValidTo())
				.withMunicipalityId(municipalityId)
				.withLaw(new ArrayList<>(dto.getLaw().stream().map(EntityMapper::toLaw).toList()))
				.withAttachments(new ArrayList<>(dto.getAttachments().stream().map(e -> toAttachment(e, dto.getMunicipalityId())).toList()))
				.withExtraParameters(Optional.ofNullable(dto.getExtraParameters()).orElse(new LinkedHashMap<>()))
				.build())
			.orElse(null);
	}

	public static DecisionDTO toDecisionDto(final Decision decision) {
		return Optional.ofNullable(decision).map(dto -> DecisionDTO.builder()
				.withId(decision.getId())
				.withVersion(decision.getVersion())
				.withCreated(decision.getCreated())
				.withUpdated(decision.getUpdated())
				.withDecisionType(decision.getDecisionType())
				.withDecisionOutcome(decision.getDecisionOutcome())
				.withDescription(decision.getDescription())
				.withDecidedBy(toStakeholderDto(decision.getDecidedBy()))
				.withDecidedAt(decision.getDecidedAt())
				.withValidFrom(decision.getValidFrom())
				.withValidTo(decision.getValidTo())
				.withMunicipalityId(decision.getMunicipalityId())
				.withLaw(new ArrayList<>(decision.getLaw().stream().map(EntityMapper::toLawDto).toList()))
				.withAttachments(new ArrayList<>(decision.getAttachments().stream().map(EntityMapper::toAttachmentDto).toList()))
				.withExtraParameters(Optional.ofNullable(decision.getExtraParameters()).orElse(new LinkedHashMap<>()))
				.build())
			.orElse(null);
	}

	public static NoteDTO toNoteDto(final Note note) {
		return Optional.ofNullable(note).map(dto -> NoteDTO.builder()
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
				.withMunicipalityId(note.getMunicipalityId())
				.withExtraParameters(Optional.ofNullable(note.getExtraParameters()).orElse(new LinkedHashMap<>()))
				.build())
			.orElse(null);
	}

	public static Note toNote(final NoteDTO dto, final String municipalityId) {
		return Optional.of(dto).map(note -> Note.builder()
				.withId(dto.getId())
				.withVersion(dto.getVersion())
				.withCreated(dto.getCreated())
				.withUpdated(dto.getUpdated())
				.withTitle(dto.getTitle())
				.withText(dto.getText())
				.withNoteType(dto.getNoteType())
				.withCreatedBy(dto.getCreatedBy())
				.withUpdatedBy(dto.getUpdatedBy())
				.withNoteType(dto.getNoteType())
				.withMunicipalityId(municipalityId)
				.withExtraParameters(Optional.ofNullable(dto.getExtraParameters()).orElse(new LinkedHashMap<>()))
				.build())
			.orElse(null);
	}

	public static Appeal toAppeal(final AppealDTO dto, final String municipalityId) {
		return Optional.ofNullable(dto).map(appeal -> Appeal.builder()
				.withId(dto.getId())
				.withVersion(dto.getVersion())
				.withCreated(dto.getCreated())
				.withUpdated(dto.getUpdated())
				.withDescription(dto.getDescription())
				.withRegisteredAt(dto.getRegisteredAt())
				.withMunicipalityId(municipalityId)
				.withStatus(AppealStatus.valueOf(dto.getStatus()))
				.withAppealConcernCommunicatedAt(dto.getAppealConcernCommunicatedAt())
				.withTimelinessReview(TimelinessReview.valueOf(dto.getTimelinessReview()))
				.build())
			.orElse(null);
	}

	public static AppealDTO toAppealDto(final Appeal appeal) {
		return Optional.ofNullable(appeal).map(dto -> AppealDTO.builder()
				.withId(appeal.getId())
				.withVersion(appeal.getVersion())
				.withCreated(appeal.getCreated())
				.withUpdated(appeal.getUpdated())
				.withDescription(appeal.getDescription())
				.withRegisteredAt(appeal.getRegisteredAt())
				.withStatus(appeal.getStatus().name())
				.withMunicipalityId(appeal.getMunicipalityId())
				.withAppealConcernCommunicatedAt(appeal.getAppealConcernCommunicatedAt())
				.withTimelinessReview(Optional.ofNullable(appeal.getTimelinessReview()).map(TimelinessReview::name).orElse(null))
				.withDecisionId(Optional.ofNullable(appeal.getDecision()).map(Decision::getId).orElse(null))
				.build())
			.orElse(null);
	}

	public static Status toStatus(final StatusDTO dto) {
		return Optional.ofNullable(dto).map(status -> Status.builder()
				.withStatusType(dto.getStatusType())
				.withDescription(dto.getDescription())
				.withDateTime(dto.getDateTime())
				.build())
			.orElse(null);
	}

	public static FacilityDTO toFacilityDto(final Facility facility) {
		return Optional.ofNullable(facility).map(dto -> FacilityDTO.builder()
				.withId(facility.getId())
				.withVersion(facility.getVersion())
				.withCreated(facility.getCreated())
				.withUpdated(facility.getUpdated())
				.withDescription(facility.getDescription())
				.withAddress(toAddressDto(facility.getAddress()))
				.withFacilityCollectionName(facility.getFacilityCollectionName())
				.withMainFacility(facility.isMainFacility())
				.withMunicipalityId(facility.getMunicipalityId())
				.withFacilityType(facility.getFacilityType())
				.withExtraParameters(Optional.ofNullable(facility.getExtraParameters()).orElse(new LinkedHashMap<>()))
				.build())
			.orElse(null);
	}

	public static Facility toFacility(final FacilityDTO dto, final String municipalityId) {
		return Optional.ofNullable(dto).map(facility -> Facility.builder()
				.withId(dto.getId())
				.withVersion(dto.getVersion())
				.withCreated(dto.getCreated())
				.withUpdated(dto.getUpdated())
				.withDescription(dto.getDescription())
				.withAddress(toAddress(dto.getAddress()))
				.withFacilityCollectionName(dto.getFacilityCollectionName())
				.withMainFacility(dto.isMainFacility())
				.withFacilityType(dto.getFacilityType())
				.withMunicipalityId(municipalityId)
				.withExtraParameters(Optional.ofNullable(dto.getExtraParameters()).orElse(new LinkedHashMap<>()))
				.build())
			.orElse(null);
	}

	public static StatusDTO toStatusDto(final Status status) {
		return Optional.ofNullable(status).map(dto -> StatusDTO.builder()
				.withStatusType(status.getStatusType())
				.withDescription(status.getDescription())
				.withDateTime(status.getDateTime())
				.build())
			.orElse(null);
	}

	public static Coordinates toCoordinates(final CoordinatesDTO dto) {
		return Optional.ofNullable(dto).map(coordinates -> Coordinates.builder()
				.withLatitude(dto.getLatitude())
				.withLongitude(dto.getLongitude())
				.build())
			.orElse(null);
	}

	public static CoordinatesDTO toCoordinatesDto(final Coordinates coordinates) {
		return Optional.ofNullable(coordinates).map(dto -> CoordinatesDTO.builder()
				.withLatitude(coordinates.getLatitude())
				.withLongitude(coordinates.getLongitude())
				.build())
			.orElse(null);
	}

	public static Address toAddress(final AddressDTO dto) {
		return Optional.ofNullable(dto).map(address -> Address.builder()
				.withAddressCategory(dto.getAddressCategory())
				.withStreet(dto.getStreet())
				.withHouseNumber(dto.getHouseNumber())
				.withPostalCode(dto.getPostalCode())
				.withCity(dto.getCity())
				.withCountry(dto.getCountry())
				.withCareOf(dto.getCareOf())
				.withAttention(dto.getAttention())
				.withPropertyDesignation(dto.getPropertyDesignation())
				.withApartmentNumber(dto.getApartmentNumber())
				.withIsZoningPlanArea(dto.getIsZoningPlanArea())
				.withInvoiceMarking(dto.getInvoiceMarking())
				.withLocation(toCoordinates(dto.getLocation()))
				.build())
			.orElse(null);
	}

	public static AddressDTO toAddressDto(final Address address) {
		return Optional.ofNullable(address).map(dto -> AddressDTO.builder()
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
				.withLocation(toCoordinatesDto(address.getLocation()))
				.build())
			.orElse(null);
	}

	public static ContactInformationDTO toContactInformationDto(final ContactInformation contactInformation) {
		return Optional.ofNullable(contactInformation).map(dto -> ContactInformationDTO.builder()
				.withContactType(dto.getContactType())
				.withValue(dto.getValue()).build())
			.orElse(null);
	}

	public static ContactInformation toContactInformation(final ContactInformationDTO dto) {
		return Optional.ofNullable(dto).map(contactInformation -> ContactInformation.builder()
				.withContactType(dto.getContactType())
				.withValue(dto.getValue())
				.build())
			.orElse(null);
	}

	public static Law toLaw(final LawDTO dto) {
		return Optional.ofNullable(dto).map(law -> Law.builder()
				.withArticle(dto.getArticle())
				.withChapter(dto.getChapter())
				.withHeading(dto.getHeading())
				.withSfs(dto.getSfs())
				.build())
			.orElse(null);
	}

	public static LawDTO toLawDto(final Law law) {
		return Optional.ofNullable(law).map(dto -> LawDTO.builder()
				.withArticle(law.getArticle())
				.withChapter(law.getChapter())
				.withHeading(law.getHeading())
				.withSfs(law.getSfs())
				.build())
			.orElse(null);
	}
}
