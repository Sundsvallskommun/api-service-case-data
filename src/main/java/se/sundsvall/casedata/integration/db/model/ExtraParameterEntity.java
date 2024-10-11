package se.sundsvall.casedata.integration.db.model;

import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import org.hibernate.annotations.UuidGenerator;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.With;

@Entity
@Table(name = "errand_extra_parameters")
@Getter
@Setter
@EqualsAndHashCode
@Builder(setterPrefix = "with")
@NoArgsConstructor
@AllArgsConstructor
public class ExtraParameterEntity {

	@Id
	@UuidGenerator
	private String id;

	@With
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "errand_id", nullable = false, foreignKey = @ForeignKey(name = "fk_extra_parameter_errand_id"))
	private ErrandEntity errandEntity;

	@Column(name = "display_name")
	private String displayName;

	@Column(name = "parameters_key")
	private String key;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(
		name = "errand_extra_parameter_values",
		joinColumns = @JoinColumn(name = "extra_parameter_id",
			foreignKey = @ForeignKey(name = "fk_errand_extra_parameter_values_parameter_id")))
	@Column(name = "value")
	private List<String> values;


	@Override
	public String toString() {
		final long errandId = errandEntity == null ? 0 : errandEntity.getId();
		return "ExtraParameterEntity{" +
			"id='" + id + '\'' +
			", errandEntity=" + errandId +
			", displayName='" + displayName + '\'' +
			", key='" + key + '\'' +
			", values=" + values +
			'}';
	}

}
