package se.sundsvall.casedata.integration.db.model;

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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.With;
import lombok.experimental.SuperBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.hibernate.Length.LONG32;

@Entity
@Table(name = "attachment", indexes = {@Index(name = "attachment_errand_number_idx", columnList = "errand_number")})
@Getter
@Setter
@SuperBuilder(setterPrefix = "with")
@NoArgsConstructor
@AllArgsConstructor
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

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "attachment_extra_parameters",
		joinColumns = @JoinColumn(name = "attachment_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "FK_attachment_extra_parameters_attachment_id")))
	@MapKeyColumn(name = "extra_parameter_key")
	@Column(name = "extra_parameter_value")
	@Builder.Default
	private Map<String, String> extraParameters = new HashMap<>();

	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;
		final Attachment that = (Attachment) o;
		return Objects.equals(category,that.category) && Objects.equals(name, that.name) && Objects.equals(note, that.note) && Objects.equals(extension, that.extension) && Objects.equals(mimeType, that.mimeType) && Objects.equals(file, that.file) && Objects.equals(errandNumber, that.errandNumber) && Objects.equals(extraParameters, that.extraParameters);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), category, name, note, extension, mimeType, file, errandNumber, extraParameters);
	}

	@Override
	public String toString() {
		return "Attachment{" +

			"category=" + category +
			", name='" + name + '\'' +
			", note='" + note + '\'' +
			", extension='" + extension + '\'' +
			", mimeType='" + mimeType + '\'' +
			", file='" + file + '\'' +
			", errandNumber='" + errandNumber + '\'' +
			", extraParameters=" + extraParameters +
			'}' + super.toString();
	}

}
