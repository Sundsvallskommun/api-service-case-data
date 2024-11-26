package se.sundsvall.casedata.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.TimeZoneStorage;
import org.hibernate.annotations.TimeZoneStorageType;
import se.sundsvall.casedata.api.model.validation.ValidMapValueSize;
import se.sundsvall.casedata.integration.db.model.enums.NoteType;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

@Data
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder(setterPrefix = "with")
public class Note {

	@Schema(accessMode = Schema.AccessMode.READ_ONLY, description = "The unique identifier of the note", example = "1")
	private long id;

	@Schema(accessMode = Schema.AccessMode.READ_ONLY, description = "The version of the note", example = "1")
	private int version;

	@Schema(description = "The municipality ID", example = "2281", accessMode = Schema.AccessMode.READ_ONLY)
	private String municipalityId;

	@Schema(description = "Namespace", example = "my.namespace", accessMode = Schema.AccessMode.READ_ONLY)
	private String namespace;

	@Schema(description = "The title of the note", example = "Motivering till bifall")
	@Size(max = 255)
	private String title;

	@Schema(description = "The content of the note", example = "Den sökande har rätt till parkeringstillstånd eftersom alla kriterier uppfylls.")
	@Size(max = 10000)
	private String text;

	@Schema(description = "AD-account for the user who created the note", example = "user", accessMode = Schema.AccessMode.READ_ONLY)
	@Size(max = 36)
	private String createdBy;

	@Schema(description = "AD-account for the user who last modified the note", example = "user", accessMode = Schema.AccessMode.READ_ONLY)
	@Size(max = 36)
	private String updatedBy;

	@Schema(description = "The type of note", example = "INTERNAL")
	private NoteType noteType;

	@ValidMapValueSize(max = 8192)
	@Schema(description = "Additional parameters for the note")
	@Builder.Default
	private Map<String, String> extraParameters = new HashMap<>();

	@Schema(accessMode = Schema.AccessMode.READ_ONLY, description = "The timestamp when the note was created", example = "2023-01-01T12:00:00Z")
	@TimeZoneStorage(TimeZoneStorageType.NORMALIZE)
	private OffsetDateTime created;

	@Schema(accessMode = Schema.AccessMode.READ_ONLY, description = "The timestamp when the note was last updated", example = "2023-01-02T12:00:00Z")
	@TimeZoneStorage(TimeZoneStorageType.NORMALIZE)
	private OffsetDateTime updated;

}
