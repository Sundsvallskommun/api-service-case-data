package se.sundsvall.casedata.integration.db.model;

import static org.hibernate.Length.LONG32;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
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
import lombok.ToString;
import lombok.With;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.TimeZoneStorage;
import org.hibernate.annotations.TimeZoneStorageType;
import org.hibernate.annotations.UpdateTimestamp;
import org.javers.core.metamodel.annotation.DiffIgnore;

@Entity
@Table(name = "attachment",
	indexes = {
		@Index(name = "attachment_errand_number_idx", columnList = "errand_number"),
		@Index(name = "idx_attachment_errand_id", columnList = "errand_id"),
		@Index(name = "idx_attachment_municipality_id", columnList = "municipality_id"),
		@Index(name = "idx_attachment_namespace", columnList = "namespace")
	})
@Getter
@Setter
@Builder(setterPrefix = "with")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class AttachmentEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	@DiffIgnore
	private Long id;

	@Version
	@Column(name = "version")
	@DiffIgnore
	private int version;

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
	@Column(name = "errand_id")
	private Long errandId;

	@With
	@Column(name = "municipality_id")
	private String municipalityId;

	@With
	@Column(name = "namespace")
	private String namespace;

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
	@CollectionTable(name = "attachment_extra_parameters",
		joinColumns = @JoinColumn(name = "attachment_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "FK_attachment_extra_parameters_attachment_id")))
	@MapKeyColumn(name = "extra_parameter_key")
	@Column(name = "extra_parameter_value", length = 8192)
	@Builder.Default
	private Map<String, String> extraParameters = new HashMap<>();

}
