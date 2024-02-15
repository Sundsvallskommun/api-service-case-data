package se.sundsvall.casedata.api.model;

import java.time.OffsetDateTime;

import org.hibernate.annotations.TimeZoneStorage;
import org.hibernate.annotations.TimeZoneStorageType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(setterPrefix = "with")
public abstract class BaseDTO {

	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	private Long id;

	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	private int version;

	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	@TimeZoneStorage(TimeZoneStorageType.NORMALIZE)
	private OffsetDateTime created;

	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	@TimeZoneStorage(TimeZoneStorageType.NORMALIZE)
	private OffsetDateTime updated;

}
