package se.sundsvall.casedata.integration.db.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

@Data
@Entity
@Table(name = "related_errand")
@NoArgsConstructor
@AllArgsConstructor
@Builder(setterPrefix = "with")
public class RelatedErrandEntity {

	@Id
	@UuidGenerator
	@Column(name = "id")
	private String id;

	@Column(name = "errand_id")
	private Long errandId;

	@Column(name = "related_errand_id")
	private Long relatedErrandId;

	@Column(name = "related_errand_number")
	private String relatedErrandNumber;

	@Column(name = "relation_reason")
	private String relationReason;
}
