package se.sundsvall.casedata.integration.db.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.TimeZoneStorage;
import org.hibernate.annotations.TimeZoneStorageType;
import org.hibernate.annotations.UpdateTimestamp;
import org.javers.core.metamodel.annotation.DiffIgnore;
import se.sundsvall.casedata.integration.db.listeners.FacilityListener;

@Entity
@Table(name = "facility",
	indexes = {
		@Index(name = "idx_facility_municipality_id", columnList = "municipality_id"),
		@Index(name = "idx_facility_namespace", columnList = "namespace")
	})
@EntityListeners(FacilityListener.class)
@Getter
@Setter
@Builder(setterPrefix = "with")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class FacilityEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	@DiffIgnore
	private Long id;

	@Version
	@Column(name = "version")
	@DiffIgnore
	private int version;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "errand_id", foreignKey = @ForeignKey(name = "FK_facility_errand_id"))
	@JsonBackReference
	private ErrandEntity errand;

	@Column(name = "description")
	private String description;

	@Column(name = "municipality_id")
	private String municipalityId;

	@Column(name = "namespace")
	private String namespace;

	@Embedded
	private AddressEntity address;

	@Column(name = "facility_collection_name")
	private String facilityCollectionName;

	@Column(name = "main_facility")
	private boolean mainFacility;

	@Column(name = "facility_type")
	private String facilityType;

	@CreationTimestamp
	@Column(name = "created")
	@TimeZoneStorage(TimeZoneStorageType.NORMALIZE)
	@DiffIgnore
	private OffsetDateTime created;

	@UpdateTimestamp
	@Column(name = "updated")
	@TimeZoneStorage(TimeZoneStorageType.NORMALIZE)
	@DiffIgnore
	private OffsetDateTime updated;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "facility_extra_parameters",
		joinColumns = @JoinColumn(name = "facility_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "FK_facility_extra_parameters_facility_id")))
	@MapKeyColumn(name = "extra_parameter_key")
	@Column(name = "extra_parameter_value", length = 8192)
	@Builder.Default
	private Map<String, String> extraParameters = new HashMap<>();

	@Override
	public String toString() {
		var errandId = errand == null ? 0 : errand.getId();
		return "FacilityEntity{" +
			"id=" + id +
			", version=" + version +
			", errand=" + errandId +
			", description='" + description + '\'' +
			", municipalityId='" + municipalityId + '\'' +
			", namespace='" + namespace + '\'' +
			", address=" + address +
			", facilityCollectionName='" + facilityCollectionName + '\'' +
			", mainFacility=" + mainFacility +
			", facilityType='" + facilityType + '\'' +
			", created=" + created +
			", updated=" + updated +
			", extraParameters=" + extraParameters +
			'}';
	}

}
