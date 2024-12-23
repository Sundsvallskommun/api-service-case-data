package se.sundsvall.casedata.api.model;

import java.time.OffsetDateTime;

import jakarta.validation.constraints.Size;

import org.hibernate.annotations.TimeZoneStorage;
import org.hibernate.annotations.TimeZoneStorageType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Builder(setterPrefix = "with")
public class Status {

	@Size(max = 255)
	@Schema(description = "The type of status", example = "Ärende inkommit")
	private String statusType;

	@Size(max = 255)
	@Schema(description = "Description of the status", example = "Ärende har kommit in från e-tjänsten.")
	private String description;

	@TimeZoneStorage(TimeZoneStorageType.NORMALIZE)
	@Schema(description = "The date and time when the status was recorded", example = "2023-01-01T12:00:00Z")
	private OffsetDateTime dateTime;

}
