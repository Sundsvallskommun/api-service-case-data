package se.sundsvall.casedata.integration.db.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapKeyColumn;

import com.fasterxml.jackson.annotation.JsonBackReference;
import org.javers.core.metamodel.annotation.DiffIgnore;

import se.sundsvall.casedata.integration.db.listeners.NoteListener;
import se.sundsvall.casedata.integration.db.model.enums.NoteType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity(name = "note")
@EntityListeners(NoteListener.class)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder(setterPrefix = "with")
public class Note extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "errand_id", foreignKey = @ForeignKey(name = "FK_note_errand_id"))
	@JsonBackReference
	private Errand errand;

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

	@Override
	public String toString() {
		final long errandId = errand == null ? 0 : errand.getId();
		return "Note{" +
			"errand.id=" + errandId +
			", title='" + title + '\'' +
			", text='" + text + '\'' +
			", createdBy='" + createdBy + '\'' +
			", updatedBy='" + updatedBy + '\'' +
			", noteType=" + noteType + '\'' +
			", extraParameters=" + extraParameters + '\'' +
			"} " + super.toString();
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (!(o instanceof final Note note)) return false;
		if (!super.equals(o)) return false;
		return Objects.equals(title, note.title) && Objects.equals(text, note.text) && Objects.equals(createdBy, note.createdBy) && Objects.equals(updatedBy, note.updatedBy) && Objects.equals(noteType, note.noteType) && Objects.equals(extraParameters, note.extraParameters);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), title, text, createdBy, updatedBy, noteType, extraParameters);
	}

}
