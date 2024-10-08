package se.sundsvall.casedata.integration.db.model;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Embeddable
@Builder(setterPrefix = "with")
@NoArgsConstructor
@AllArgsConstructor
public class CoordinatesEntity {

	@Column(name = "latitude")
	private Double latitude;

	@Column(name = "longitude")
	private Double longitude;

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof final CoordinatesEntity that)) {
			return false;
		}
		return Objects.equals(latitude, that.latitude) && Objects.equals(longitude, that.longitude);
	}

	@Override
	public int hashCode() {
		return Objects.hash(latitude, longitude);
	}

}
