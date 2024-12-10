package se.sundsvall.casedata.integration.db.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
import se.sundsvall.casedata.integration.db.listeners.NoteListener;
import se.sundsvall.casedata.integration.db.model.enums.NoteType;

@Entity
@Table(name = "note",
	indexes = {
		@Index(name = "idx_note_municipality_id", columnList = "municipality_id"),
		@Index(name = "idx_note_namespace", columnList = "namespace")

	})
@EntityListeners(NoteListener.class)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder(setterPrefix = "with")
@EqualsAndHashCode
public class NoteEntity {

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
	@JoinColumn(name = "errand_id", foreignKey = @ForeignKey(name = "FK_note_errand_id"))
	@JsonBackReference
	private ErrandEntity errand;

	@Column(name = "municipality_id")
	private String municipalityId;

	@Column(name = "namespace")
	private String namespace;

	@Column(name = "title")
	private String title;

	@Column(name = "text", length = 10000)
	private String text;

	@Column(name = "created_by", length = 36)
	@DiffIgnore
	private String createdBy;

	@Column(name = "updated_by", length = 36)
	@DiffIgnore
	private String updatedBy;

	@Enumerated(EnumType.STRING)
	@Column(name = "note_type")
	private NoteType noteType;

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

	@ElementCollection
	@CollectionTable(name = "note_extra_parameters",
		joinColumns = @JoinColumn(name = "note_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "FK_note_extra_parameters_note_id")))
	@MapKeyColumn(name = "extra_parameter_key")
	@Column(name = "extra_parameter_value", length = 8192)
	@Builder.Default
	private Map<String, String> extraParameters = new HashMap<>();

	@Override
	public String toString() {
		var errandId = errand != null ? errand.getId() : null;
		return "NoteEntity{" +
			"id=" + id +
			", version=" + version +
			", errand=" + errandId +
			", municipalityId='" + municipalityId + '\'' +
			", namespace='" + namespace + '\'' +
			", title='" + title + '\'' +
			", text='" + text + '\'' +
			", createdBy='" + createdBy + '\'' +
			", updatedBy='" + updatedBy + '\'' +
			", noteType=" + noteType +
			", created=" + created +
			", updated=" + updated +
			", extraParameters=" + extraParameters +
			'}';
	}

}
