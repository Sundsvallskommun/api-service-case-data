package se.sundsvall.casedata.integration.db.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity(name = "appeal")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(setterPrefix = "with")
public class Appeal extends BaseEntity {

	@OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "appealed_by_id", foreignKey = @ForeignKey(name = "FK_appeal_appealed_by_id"))
	private Stakeholder appealedBy;

	@OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "judicial_authorisation_id", foreignKey = @ForeignKey(name = "FK_appeal_judicial_authorisation_id"))
	private Stakeholder judicialAuthorisation;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "appeal_id", foreignKey = @ForeignKey(name = "FK_appeal_id"))
	@Builder.Default
	private List<Attachment> attachments = new ArrayList<>();

	@ElementCollection
	@CollectionTable(name = "appeal_extra_parameters",
		joinColumns = @JoinColumn(name = "appeal_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "FK_appeal_extra_parameters_appeal_id")))
	@MapKeyColumn(name = "extra_parameter_key")
	@Column(name = "extra_parameter_value")
	@Builder.Default
	private Map<String, String> extraParameters = new HashMap<>();

	@Override
	public String toString() {
		return "Appeal{" +
			"appealedBy=" + appealedBy +
			", judicialAuthorisation=" + judicialAuthorisation +
			", attachments=" + attachments +
			", extraParameters=" + extraParameters +
			"} " + super.toString();
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (!(o instanceof final Appeal appeal)) return false;
		if (!super.equals(o)) return false;
		return Objects.equals(appealedBy, appeal.appealedBy) && Objects.equals(judicialAuthorisation, appeal.judicialAuthorisation) && Objects.equals(attachments, appeal.attachments) && Objects.equals(extraParameters, appeal.extraParameters);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), appealedBy, judicialAuthorisation, attachments, extraParameters);
	}

}
