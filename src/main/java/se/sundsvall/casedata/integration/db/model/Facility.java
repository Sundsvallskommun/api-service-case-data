package se.sundsvall.casedata.integration.db.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapKeyColumn;

import com.fasterxml.jackson.annotation.JsonBackReference;

import se.sundsvall.casedata.integration.db.listeners.FacilityListener;
import se.sundsvall.casedata.integration.db.model.enums.FacilityType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity(name = "facility")
@EntityListeners(FacilityListener.class)
@Getter
@Setter
@SuperBuilder(setterPrefix = "with")
@AllArgsConstructor
@NoArgsConstructor
public class Facility extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "errand_id", foreignKey = @ForeignKey(name = "FK_facility_errand_id"))
	@JsonBackReference
	private Errand errand;

	@Column(name = "description")
	private String description;

	@Embedded
	private Address address;

	@Column(name = "facility_collection_name")
	private String facilityCollectionName;

	@Column(name = "main_facility")
	private boolean mainFacility;

	@Enumerated(EnumType.STRING)
	@Column(name = "facility_type")
	private FacilityType facilityType;

	@ElementCollection
	@CollectionTable(name = "facility_extra_parameters",
		joinColumns = @JoinColumn(name = "facility_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "FK_facility_extra_parameters_facility_id")))
	@MapKeyColumn(name = "extra_parameter_key")
	@Column(name = "extra_parameter_value")
	@Builder.Default
	private Map<String, String> extraParameters = new HashMap<>();

	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (!(o instanceof final Facility facility)) return false;
		if (!super.equals(o)) return false;
		return mainFacility == facility.mainFacility && Objects.equals(description, facility.description) && Objects.equals(address, facility.address) && Objects.equals(facilityCollectionName, facility.facilityCollectionName) && facilityType == facility.facilityType && Objects.equals(extraParameters, facility.extraParameters);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), description, address, facilityCollectionName, mainFacility, facilityType, extraParameters);
	}

	@Override
	public String toString() {
		final long errandId = errand == null ? 0 : errand.getId();

		return "Facility{" +
			"errand.id=" + errandId +
			", description='" + description + '\'' +
			", address=" + address +
			", facilityCollectionName='" + facilityCollectionName + '\'' +
			", mainFacility=" + mainFacility +
			", facilityType=" + facilityType +
			", extraParameters=" + extraParameters +
			"} " + super.toString();
	}

}
