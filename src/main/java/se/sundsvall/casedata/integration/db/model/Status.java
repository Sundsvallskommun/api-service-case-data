package se.sundsvall.casedata.integration.db.model;

import java.time.OffsetDateTime;
import java.util.Objects;

import org.hibernate.annotations.TimeZoneStorage;
import org.hibernate.annotations.TimeZoneStorageType;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Embeddable
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder(setterPrefix = "with")
public class Status {

	@Column(name = "status_type")
	private String statusType;

	@Column(name = "description")
	private String description;

	@Column(name = "date_time")
	@TimeZoneStorage(TimeZoneStorageType.NORMALIZE)
	private OffsetDateTime dateTime;

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof final Status status)) {
			return false;
		}
		return Objects.equals(statusType, status.statusType) && Objects.equals(description, status.description) && Objects.equals(dateTime, status.dateTime);
	}

	@Override
	public int hashCode() {
		return Objects.hash(statusType, description, dateTime);
	}
}
