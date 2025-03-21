package se.sundsvall.casedata.api.model;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;
import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_WRITE;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;
import static org.hibernate.annotations.TimeZoneStorageType.NORMALIZE;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.TimeZoneStorage;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(setterPrefix = "with")
@Schema(description = "The status model", accessMode = READ_WRITE)
public class Status {

	@NotBlank
	@Size(max = 255)
	@Schema(description = "The type of status", example = "Ärende inkommit", maxLength = 255, requiredMode = REQUIRED)
	private String statusType;

	@Size(max = 255)
	@Schema(description = "Description of the status", example = "Ärende har kommit in från e-tjänsten.", maxLength = 255)
	private String description;

	@TimeZoneStorage(NORMALIZE)
	@Schema(description = "The date and time when the status was created", example = "2023-01-01T12:00:00Z", accessMode = READ_ONLY)
	private OffsetDateTime created;
}
