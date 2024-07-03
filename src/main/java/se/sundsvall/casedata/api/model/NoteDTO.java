package se.sundsvall.casedata.api.model;

import java.util.HashMap;
import java.util.Map;

import jakarta.validation.constraints.Size;

import se.sundsvall.casedata.api.model.validation.ValidMapValueSize;
import se.sundsvall.casedata.integration.db.model.enums.NoteType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(setterPrefix = "with")
public class NoteDTO extends BaseDTO {

	@Schema(example = "Motivering till bifall")
	@Size(max = 255)
	private String title;

	@Schema(example = "Den sökande har rätt till parkeringstillstånd eftersom alla kriterier uppfylls.")
	@Size(max = 10000)
	private String text;

	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	private String municipalityId;

	@Schema(description = "AD-account for the user who created the note.", example = "user",
		accessMode = Schema.AccessMode.READ_ONLY)
	@Size(max = 36)
	private String createdBy;

	@Schema(description = "AD-account for the user who last modified the note.", example = "user",
		accessMode = Schema.AccessMode.READ_ONLY)
	@Size(max = 36)
	private String updatedBy;

	@Schema(description = "The type of note", example = "INTERNAL")
	private NoteType noteType;

	@Builder.Default
	@ValidMapValueSize(max = 8192)
	private Map<String, String> extraParameters = new HashMap<>();

}
