package se.sundsvall.casedata.integration.db.model;

import static org.hibernate.Length.LONG32;

import java.util.HashMap;
import java.util.Map;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.With;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "attachment",
	indexes = {
		@Index(name = "attachment_errand_number_idx", columnList = "errand_number"),
		@Index(name = "idx_attachment_municipality_id", columnList = "municipality_id")
	})
@Getter
@Setter
@SuperBuilder(setterPrefix = "with")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Attachment extends BaseEntity {

	@Column(name = "category")
	private String category;

	@Column(name = "name")
	private String name;

	@Column(name = "note", length = 1000)
	private String note;

	@Column(name = "extension")
	private String extension;

	@Column(name = "mime_type")
	private String mimeType;

	@Column(name = "file", length = LONG32)
	private String file;

	@With
	@Column(name = "errand_number")
	private String errandNumber;

	@Column(name = "municipality_id")
	private String municipalityId;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "attachment_extra_parameters",
		joinColumns = @JoinColumn(name = "attachment_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "FK_attachment_extra_parameters_attachment_id")))
	@MapKeyColumn(name = "extra_parameter_key")
	@Column(name = "extra_parameter_value", length = 8192)
	@Builder.Default
	private Map<String, String> extraParameters = new HashMap<>();

}
