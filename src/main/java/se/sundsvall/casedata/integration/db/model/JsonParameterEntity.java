package se.sundsvall.casedata.integration.db.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;

import static org.hibernate.type.SqlTypes.LONG32VARCHAR;

@Entity
@Table(name = "json_parameter",
	indexes = {
		@Index(name = "idx_json_parameter_errand_id", columnList = "errand_id"),
		@Index(name = "idx_json_parameter_key", columnList = "parameter_key")
	})
@Data
@EqualsAndHashCode(exclude = "errand")
@Builder(setterPrefix = "with")
@NoArgsConstructor
@AllArgsConstructor
public class JsonParameterEntity {

	@Id
	@UuidGenerator
	private String id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "errand_id", nullable = false, foreignKey = @ForeignKey(name = "fk_json_parameter_errand_id"))
	private ErrandEntity errand;

	@Column(name = "parameter_key", nullable = false)
	private String key;

	@Column(name = "schema_id", nullable = false)
	private String schemaId;

	@JdbcTypeCode(LONG32VARCHAR)
	@Column(name = "value", nullable = false)
	private String value;

	@Override
	public String toString() {
		return "JsonParameterEntity{" +
			"id='" + id + '\'' +
			", errand=" + (errand != null ? errand.getId() : null) +
			", key='" + key + '\'' +
			", schemaId='" + schemaId + '\'' +
			", value='" + value + '\'' +
			'}';
	}
}
