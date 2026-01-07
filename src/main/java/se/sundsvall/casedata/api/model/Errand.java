package se.sundsvall.casedata.api.model;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;
import static org.hibernate.annotations.TimeZoneStorageType.NORMALIZE;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.TimeZoneStorage;
import se.sundsvall.casedata.api.model.validation.UniqueDecisionType;
import se.sundsvall.casedata.api.model.validation.ValidCaseType;
import se.sundsvall.casedata.integration.db.model.enums.Channel;
import se.sundsvall.casedata.integration.db.model.enums.Priority;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(setterPrefix = "with")
public class Errand {

	@Schema(description = "The id of the errand", accessMode = READ_ONLY, examples = "1")
	private Long id;

	@Schema(description = "The version of the errand", accessMode = READ_ONLY, examples = "1")
	private int version;

	@Schema(description = "Errand number", examples = "PRH-2022-000001", accessMode = READ_ONLY)
	private String errandNumber;

	@Schema(description = "The municipality ID", examples = "2281", accessMode = READ_ONLY)
	private String municipalityId;

	@Schema(description = "Namespace", examples = "MY_NAMESPACE", accessMode = READ_ONLY)
	private String namespace;

	@Schema(description = "Case ID from the client", examples = "caa230c6-abb4-4592-ad9a-34e263c2787b", maxLength = 255)
	@Size(max = 255)
	private String externalCaseId;

	@Schema(description = "Type of the case", examples = "BUILDING_PERMIT")
	@ValidCaseType
	private String caseType;

	@Schema(description = "How the errand was created", examples = "EMAIL", nullable = true)
	private Channel channel;

	@Schema(description = "Priority of the errand", defaultValue = "MEDIUM", examples = "HIGH")
	@Builder.Default
	private Priority priority = Priority.MEDIUM;

	@Schema(description = "Description of the errand", examples = "Some description of the case.")
	private String description;

	@Schema(description = "Additions to the case title. Right now only applicable to cases of CaseType: NYBYGGNAD_ANSOKAN_OM_BYGGLOV.", examples = "Eldstad/rökkanal, Skylt", maxLength = 255)
	@Size(max = 255)
	private String caseTitleAddition;

	@Schema(description = "Diary number", examples = "DIA123456", maxLength = 255)
	@Size(max = 255)
	private String diaryNumber;

	@Schema(description = "Phase of the errand", examples = "Aktualisering", maxLength = 255)
	@Size(max = 255)
	private String phase;

	@Schema(description = "The current status of the errand", maxLength = 255, nullable = true)
	private Status status;

	@Schema(description = "The statuses connected to the errand", accessMode = READ_ONLY)
	private List<Status> statuses;

	@Schema(description = "Start date for the business", format = "date", examples = "2022-01-01")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private LocalDate startDate;

	@Schema(description = "End date of the business if it is time-limited", format = "date", examples = "2022-06-01")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private LocalDate endDate;

	@Schema(description = "The time the application was received", examples = "2023-10-01T12:00:00Z")
	@TimeZoneStorage(NORMALIZE)
	private OffsetDateTime applicationReceived;

	@Schema(description = "Process-ID from ProcessEngine", examples = "c3cb9123-4ed2-11ed-ac7c-0242ac110003", accessMode = READ_ONLY)
	private String processId;

	@Schema(description = "The applicant and other stakeholders connected to the errand")
	@Valid
	private List<Stakeholder> stakeholders;

	@Schema(description = "The facilities connected to the errand")
	@Valid
	private List<Facility> facilities;

	@Schema(description = "List of notifications connected to this errand", accessMode = READ_ONLY)
	private List<Notification> notifications;

	@Valid
	@UniqueDecisionType
	@Schema(description = "The decisions connected to the errand")
	private List<Decision> decisions;

	@Schema(description = "The notes connected to the errand")
	@Valid
	private List<Note> notes;

	@Schema(description = "Messages connected to this errand. Get message information from Message-API", accessMode = READ_ONLY)
	private List<String> messageIds;

	@Schema(description = "List of labels for the errand", examples = "[\"label1\",\"label2\"]")
	private List<String> labels;

	@Valid
	@Schema(description = "Other errands related to the errand")
	private List<RelatedErrand> relatesTo;

	@Schema(description = "The client who created the errand. WSO2-username", accessMode = READ_ONLY)
	private String createdByClient;

	@Schema(description = "The most recent client who updated the errand. WSO2-username", accessMode = READ_ONLY)
	private String updatedByClient;

	@Schema(description = "The user who created the errand", accessMode = READ_ONLY)
	private String createdBy;

	@Schema(description = "The most recent user who updated the errand", accessMode = READ_ONLY)
	private String updatedBy;

	@Valid
	@Schema(description = "Suspension information")
	private Suspension suspension;

	@Schema(description = "Extra parameters for the errand")
	private List<ExtraParameter> extraParameters;

	@Schema(description = "Date and time when the errand was created", accessMode = READ_ONLY, examples = "2023-10-01T12:00:00Z")
	@TimeZoneStorage(NORMALIZE)
	private OffsetDateTime created;

	@Schema(description = "Date and time when the errand was last updated", accessMode = READ_ONLY, examples = "2023-10-02T12:00:00Z")
	@TimeZoneStorage(NORMALIZE)
	private OffsetDateTime updated;
}
