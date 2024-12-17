package se.sundsvall.casedata.api.model;

import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import se.sundsvall.casedata.api.model.validation.ValidSuspension;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(setterPrefix = "with")
@ValidSuspension
public class Suspension {

	@Schema(description = "Timestamp when the suspension wears off", example = "2000-10-31T01:30:00.000+02:00")
	@DateTimeFormat(iso = DATE_TIME)
	@FutureOrPresent
	private OffsetDateTime suspendedTo;

	@Schema(description = "Timestamp when the suspension started", example = "2000-10-31T01:30:00.000+02:00")
	@DateTimeFormat(iso = DATE_TIME)
	private OffsetDateTime suspendedFrom;
}
