package se.sundsvall.casedata.integration.db.model;

import java.util.HashMap;
import java.util.Map;

import org.javers.core.metamodel.annotation.DiffIgnore;

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
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import se.sundsvall.casedata.integration.db.listeners.NoteListener;
import se.sundsvall.casedata.integration.db.model.enums.NoteType;

@Entity
@Table(name = "note",
	indexes = {
		@Index(name = "idx_note_municipality_id", columnList = "municipality_id")
	})
@EntityListeners(NoteListener.class)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder(setterPrefix = "with")
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class Note extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "errand_id", foreignKey = @ForeignKey(name = "FK_note_errand_id"))
	@JsonBackReference
	private Errand errand;

	@Column(name = "municipality_id")
	private String municipalityId;

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

	@ElementCollection
	@CollectionTable(name = "note_extra_parameters",
		joinColumns = @JoinColumn(name = "note_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "FK_note_extra_parameters_note_id")))
	@MapKeyColumn(name = "extra_parameter_key")
	@Column(name = "extra_parameter_value", length = 8192)
	@Builder.Default
	private Map<String, String> extraParameters = new HashMap<>();
}
