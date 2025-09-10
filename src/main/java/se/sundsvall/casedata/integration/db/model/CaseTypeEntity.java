package se.sundsvall.casedata.integration.db.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "case_type")
@Builder(setterPrefix = "with")
@NoArgsConstructor
@AllArgsConstructor
public class CaseTypeEntity {

	@Id
	@Column(name = "type", length = 100)
	private String type;

	@Column(name = "display_name")
	private String displayName;

	@Column(name = "namespace", length = 100)
	private String namespace;

	@Column(name = "municipality_id", length = 10)
	private String municipalityId;
}
