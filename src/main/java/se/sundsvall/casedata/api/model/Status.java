package se.sundsvall.casedata.api.model;

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
public class Status {

	@NotBlank
	@Size(max = 255)
	@Schema(description = "The type of status", examples = "Ärende inkommit", maxLength = 255, requiredMode = Schema.RequiredMode.REQUIRED)
	private String statusType;

	@Size(max = 255)
	@Schema(description = "Description of the status", examples = "Ärende har kommit in från e-tjänsten.", maxLength = 255)
	private String description;

	@TimeZoneStorage(NORMALIZE)
	@Schema(description = "The date and time when the status was created", examples = "2023-01-01T12:00:00Z", accessMode = Schema.AccessMode.READ_ONLY)
	private OffsetDateTime created;

}
