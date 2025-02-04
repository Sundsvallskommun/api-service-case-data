package se.sundsvall.casedata.api.model;

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
import se.sundsvall.casedata.api.model.validation.enums.CaseType;
import se.sundsvall.casedata.integration.db.model.enums.Priority;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(setterPrefix = "with")
public class PatchErrand {

	@Schema(description = "Case ID from the client.", example = "caa230c6-abb4-4592-ad9a-34e263c2787b", maxLength = 255)
	@Size(max = 255)
	private String externalCaseId;

	@Schema(description = "The type of case", example = "NYBYGGNAD_ANSOKAN_OM_BYGGLOV")
	private CaseType caseType;

	@Schema(description = "The priority of the case", example = "MEDIUM")
	private Priority priority;

	@Schema(description = "Description of the case", example = "Some description of the case.")
	private String description;

	@Schema(description = "Additions to the case title. Right now only applicable to cases of CaseType: NYBYGGNAD_ANSOKAN_OM_BYGGLOV.", example = "Eldstad/rökkanal, Skylt", maxLength = 255)
	@Size(max = 255)
	private String caseTitleAddition;

	@Schema(description = "Diary number of the case", example = "D123456", maxLength = 255)
	@Size(max = 255)
	private String diaryNumber;

	@Schema(description = "Phase of the case", example = "Aktualisering", maxLength = 255)
	@Size(max = 255)
	private String phase;

	@Schema(description = "The facilities in the case")
	private List<Facility> facilities;

	@Schema(description = "Start date for the business.", format = "date", example = "2022-01-01")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private LocalDate startDate;

	@Schema(description = "End date of the business if it is time-limited.", format = "date", example = "2022-06-01")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private LocalDate endDate;

	@Valid
	@Schema(description = "Suspension information")
	private Suspension suspension;

	@Schema(description = "The time the application was received.", example = "2022-01-01T12:00:00Z")
	@TimeZoneStorage(NORMALIZE)
	private OffsetDateTime applicationReceived;

	@Schema(description = "Extra parameters for the errand")
	private List<ExtraParameter> extraParameters;

	@Valid
	@Schema(description = "Other errands related to the errand")
	private List<RelatedErrand> relatesTo;

	@Schema(description = "List of labels for the errand", example = "[\"label1\",\"label2\"]")
	private List<String> labels;
}
