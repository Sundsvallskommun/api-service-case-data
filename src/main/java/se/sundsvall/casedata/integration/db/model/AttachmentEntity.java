package se.sundsvall.casedata.integration.db.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.sql.Blob;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.With;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.TimeZoneStorage;
import org.hibernate.annotations.TimeZoneStorageType;
import org.hibernate.annotations.UpdateTimestamp;
import org.javers.core.metamodel.annotation.DiffIgnore;
import se.sundsvall.casedata.integration.db.model.enums.Channel;

@Entity
// Write only changed columns, so a metadata update does not rewrite the large 'content' blob.
@DynamicUpdate
@Table(name = "attachment",
	indexes = {
		@Index(name = "idx_attachment_errand_id", columnList = "errand_id"),
		@Index(name = "idx_attachment_municipality_id", columnList = "municipality_id"),
		@Index(name = "idx_attachment_namespace", columnList = "namespace"),
		@Index(name = "idx_attachment_hash", columnList = "hash")
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

	@With
	@Enumerated(EnumType.STRING)
	@Column(name = "channel", columnDefinition = "varchar(255)")
	private Channel channel;

	@Column(name = "mime_type")
	private String mimeType;

	@Lob
	@Column(name = "content", columnDefinition = "longblob")
	@DiffIgnore
	private Blob content;

	@Column(name = "hash", length = 64)
	private String hash;

	@With
	@Column(name = "errand_id")
	private Long errandId;

	/**
	 * Read-only view of the foreign key owned by {@link DecisionEntity#getAttachments()}, so attachments can be queried by
	 * their owning decision and errand attachments can be told apart from decision attachments. Writes go through the
	 * decision's collection - assigning this field has no effect.
	 *
	 * <p>
	 * Kept out of the JaVers diff: the foreign key is written when the decision's collection is flushed, so on a newly
	 * created attachment this field is still null when the change is recorded and would only ever be audited as such.
	 */
	@Column(name = "decision_id", insertable = false, updatable = false)
	@Setter(AccessLevel.NONE)
	@DiffIgnore
	private Long decisionId;

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
