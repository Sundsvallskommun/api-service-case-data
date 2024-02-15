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
public class StatusDTO {

	@Size(max = 255)
	@Schema(example = "Ärende inkommit")
	private String statusType;

	@Size(max = 255)
	@Schema(example = "Ärende har kommit in från e-tjänsten.")
	private String description;

	@TimeZoneStorage(TimeZoneStorageType.NORMALIZE)
	private OffsetDateTime dateTime;

}
