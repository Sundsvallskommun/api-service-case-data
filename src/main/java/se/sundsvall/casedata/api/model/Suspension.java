package se.sundsvall.casedata.api.model;

import java.time.OffsetDateTime;
import java.util.Objects;

import jakarta.validation.constraints.FutureOrPresent;

import org.springframework.format.annotation.DateTimeFormat;

import se.sundsvall.casedata.api.model.validation.ValidSuspension;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(setterPrefix = "with")
@ValidSuspension
public class Suspension {

	@Schema(description = "Timestamp when the suspension wears off", example = "2000-10-31T01:30:00.000+02:00")
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
	@FutureOrPresent
	private OffsetDateTime suspendedTo;

	@Schema(description = "Timestamp when the suspension started", example = "2000-10-31T01:30:00.000+02:00")
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
	private OffsetDateTime suspendedFrom;

	@Override
	public String toString() {
		return "Suspension{" +
			"suspendedTo=" + suspendedTo +
			", suspendedFrom=" + suspendedFrom +
			'}';
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		final Suspension that = (Suspension) o;
		return Objects.equals(suspendedTo, that.suspendedTo) && Objects.equals(suspendedFrom, that.suspendedFrom);
	}

	@Override
	public int hashCode() {
		return Objects.hash(suspendedTo, suspendedFrom);
	}

}
