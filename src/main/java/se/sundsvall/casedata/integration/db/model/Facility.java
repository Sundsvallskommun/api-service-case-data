package se.sundsvall.casedata.integration.db.model;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import se.sundsvall.casedata.integration.db.listeners.FacilityListener;

@Entity
@Table(name = "facility",
	indexes = {
		@Index(name = "idx_facility_municipality_id", columnList = "municipality_id")
	})
@EntityListeners(FacilityListener.class)
@Getter
@Setter
@SuperBuilder(setterPrefix = "with")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Facility extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "errand_id", foreignKey = @ForeignKey(name = "FK_facility_errand_id"))
	@JsonBackReference
	private Errand errand;

	@Column(name = "description")
	private String description;

	@Column(name = "municipality_id")
	private String municipalityId;

	@Embedded
	private Address address;

	@Column(name = "facility_collection_name")
	private String facilityCollectionName;

	@Column(name = "main_facility")
	private boolean mainFacility;

	@Column(name = "facility_type")
	private String facilityType;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "facility_extra_parameters",
		joinColumns = @JoinColumn(name = "facility_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "FK_facility_extra_parameters_facility_id")))
	@MapKeyColumn(name = "extra_parameter_key")
	@Column(name = "extra_parameter_value", length = 8192)
	@Builder.Default
	private Map<String, String> extraParameters = new HashMap<>();
}
