package se.sundsvall.casedata.integration.db.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

@Data
@Entity
@Table(name = "case_type",
	indexes = {
		@Index(name = "idx_case_type_municipality_namespace", columnList = "municipality_id, namespace")
	},
	uniqueConstraints = {
		@UniqueConstraint(name = "ux_case_type_municipality_namespace_type", columnNames = {
			"municipality_id", "namespace", "type"
		})
	})
@Builder(setterPrefix = "with")
@NoArgsConstructor
@AllArgsConstructor
public class CaseTypeEntity {

	@Id
	@UuidGenerator
	@Column(name = "id")
	private String id;

	@Column(name = "type", length = 100)
	private String type;

	@Column(name = "display_name")
	private String displayName;

	@Column(name = "namespace", length = 100, nullable = false)
	private String namespace;

	@Column(name = "municipality_id", length = 10, nullable = false)
	private String municipalityId;
}
