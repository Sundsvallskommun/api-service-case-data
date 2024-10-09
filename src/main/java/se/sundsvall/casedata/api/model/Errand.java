package se.sundsvall.casedata.api.model;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.hibernate.annotations.TimeZoneStorage;
import org.hibernate.annotations.TimeZoneStorageType;

import se.sundsvall.casedata.api.model.validation.ValidCaseType;
import se.sundsvall.casedata.api.model.validation.ValidMapValueSize;
import se.sundsvall.casedata.integration.db.model.enums.Channel;
import se.sundsvall.casedata.integration.db.model.enums.Priority;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder(setterPrefix = "with")
public class Errand {

	@Schema(description = "The id of the errand", accessMode = Schema.AccessMode.READ_ONLY, example = "1")
	private long id;

	@Schema(description = "The version of the errand", accessMode = Schema.AccessMode.READ_ONLY, example = "1")
	private int version;

	@Schema(description = "Errand number", example = "PRH-2022-000001", accessMode = Schema.AccessMode.READ_ONLY)
	private String errandNumber;

	@Schema(description = "Case ID from the client", example = "caa230c6-abb4-4592-ad9a-34e263c2787b")
	@Size(max = 255)
	private String externalCaseId;

	@Schema(description = "Type of the case", example = "BUILDING_PERMIT")
	@ValidCaseType
	private String caseType;

	@Schema(description = "How the errand was created", example = "EMAIL", nullable = true)
	private Channel channel;

	@Schema(description = "Priority of the errand", defaultValue = "MEDIUM", example = "HIGH")
	@Builder.Default
	private Priority priority = Priority.MEDIUM;

	@Schema(description = "Description of the errand", example = "Some description of the case.")
	@Size(max = 8192)
	private String description;

	@Schema(description = "Additions to the case title. Right now only applicable to cases of CaseType: NYBYGGNAD_ANSOKAN_OM_BYGGLOV.", example = "Eldstad/rökkanal, Skylt")
	@Size(max = 255)
	private String caseTitleAddition;

	@Schema(description = "Diary number", example = "DIA123456")
	@Size(max = 255)
	private String diaryNumber;

	@Schema(description = "Phase of the errand", example = "Aktualisering")
	@Size(max = 255)
	private String phase;

	@Schema(description = "The statuses connected to the errand")
	private List<Status> statuses;

	@Schema(description = "Start date for the business", format = "date", example = "2022-01-01")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private LocalDate startDate;

	@Schema(description = "End date of the business if it is time-limited", format = "date", example = "2022-06-01")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private LocalDate endDate;

	@Schema(description = "The time the application was received", example = "2023-10-01T12:00:00Z")
	@TimeZoneStorage(TimeZoneStorageType.NORMALIZE)
	private OffsetDateTime applicationReceived;

	@Schema(description = "Process-ID from ProcessEngine", example = "c3cb9123-4ed2-11ed-ac7c-0242ac110003", accessMode = Schema.AccessMode.READ_ONLY)
	private String processId;

	@Schema(description = "The applicant and other stakeholders connected to the errand")
	@Valid
	private List<Stakeholder> stakeholders;

	@Schema(description = "The facilities connected to the errand")
	@Valid
	private List<Facility> facilities;

	@Schema(description = "The decisions connected to the errand")
	@Valid
	private List<Decision> decisions;

	@Schema(description = "The appeals connected to the errand")
	@Valid
	private List<Appeal> appeals;

	@Schema(description = "The notes connected to the errand")
	@Valid
	private List<Note> notes;

	@Schema(description = "Messages connected to this errand. Get message information from Message-API", accessMode = Schema.AccessMode.READ_ONLY)
	private List<String> messageIds;

	@Schema(description = "The client who created the errand. WSO2-username", accessMode = Schema.AccessMode.READ_ONLY)
	private String createdByClient;

	@Schema(description = "The most recent client who updated the errand. WSO2-username", accessMode = Schema.AccessMode.READ_ONLY)
	private String updatedByClient;

	@Schema(description = "The user who created the errand", accessMode = Schema.AccessMode.READ_ONLY)
	private String createdBy;

	@Schema(description = "The most recent user who updated the errand", accessMode = Schema.AccessMode.READ_ONLY)
	private String updatedBy;

	@Valid
	@Schema(description = "Suspension information")
	private Suspension suspension;

	@Schema(description = "Extra parameters for the errand", example = "{\"key1\":\"value1\",\"key2\":\"value2\"}")
	@ValidMapValueSize(max = 8192)
	@Builder.Default
	private Map<String, String> extraParameters = new HashMap<>();

	@Schema(description = "Date and time when the errand was created", accessMode = Schema.AccessMode.READ_ONLY, example = "2023-10-01T12:00:00Z")
	@TimeZoneStorage(TimeZoneStorageType.NORMALIZE)
	private OffsetDateTime created;

	@Schema(description = "Date and time when the errand was last updated", accessMode = Schema.AccessMode.READ_ONLY, example = "2023-10-02T12:00:00Z")
	@TimeZoneStorage(TimeZoneStorageType.NORMALIZE)
	private OffsetDateTime updated;

}
