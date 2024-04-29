package se.sundsvall.casedata.integration.db.model;

import static org.hibernate.Length.LONG;

import java.time.OffsetDateTime;
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
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderColumn;

import com.fasterxml.jackson.annotation.JsonBackReference;
import org.hibernate.annotations.TimeZoneStorage;
import org.hibernate.annotations.TimeZoneStorageType;

import se.sundsvall.casedata.integration.db.listeners.DecisionListener;
import se.sundsvall.casedata.integration.db.model.enums.DecisionOutcome;
import se.sundsvall.casedata.integration.db.model.enums.DecisionType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity(name = "decision")
@EntityListeners(DecisionListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(setterPrefix = "with")
public class Decision extends BaseEntity {

	@ManyToOne
	@JoinColumn(name = "errand_id", foreignKey = @ForeignKey(name = "FK_decision_errand_id"))
	@JsonBackReference
	private Errand errand;

	@Enumerated(EnumType.STRING)
	@Column(name = "decision_type")
	private DecisionType decisionType;

	@Enumerated(EnumType.STRING)
	@Column(name = "decision_outcome")
	private DecisionOutcome decisionOutcome;

	@Column(name = "description", length = LONG)
	private String description;

	@ElementCollection
	@CollectionTable(name = "decision_laws",
		joinColumns = @JoinColumn(name = "decision_id", foreignKey = @ForeignKey(name = "FK_decision_laws_decision_id")))
	@OrderColumn(name = "law_order")
	@Builder.Default
	private List<Law> law = new ArrayList<>();

	@OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "decided_by_id", foreignKey = @ForeignKey(name = "FK_decision_decided_by_id"))
	private Stakeholder decidedBy;

	@TimeZoneStorage(TimeZoneStorageType.NORMALIZE)
	@Column(name = "decided_at")
	private OffsetDateTime decidedAt;

	@TimeZoneStorage(TimeZoneStorageType.NORMALIZE)
	@Column(name = "valid_from")
	private OffsetDateTime validFrom;

	@TimeZoneStorage(TimeZoneStorageType.NORMALIZE)
	@Column(name = "valid_to")
	private OffsetDateTime validTo;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "decision_id", foreignKey = @ForeignKey(name = "FK_decision_id"))
	@Builder.Default
	private List<Attachment> attachments = new ArrayList<>();

	@ElementCollection
	@CollectionTable(name = "decision_extra_parameters",
		joinColumns = @JoinColumn(name = "decision_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "FK_decision_extra_parameters_decision_id")))
	@MapKeyColumn(name = "extra_parameter_key")
	@Column(name = "extra_parameter_value", length = 8192)
	@Builder.Default
	private Map<String, String> extraParameters = new HashMap<>();

	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (!(o instanceof final Decision decision)) return false;
		if (!super.equals(o)) return false;
		return decisionType == decision.decisionType && decisionOutcome == decision.decisionOutcome && Objects.equals(description, decision.description) && Objects.equals(law, decision.law) && Objects.equals(decidedBy, decision.decidedBy) && Objects.equals(decidedAt, decision.decidedAt) && Objects.equals(validFrom, decision.validFrom) && Objects.equals(validTo, decision.validTo) && Objects.equals(attachments, decision.attachments) && Objects.equals(extraParameters, decision.extraParameters);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), decisionType, decisionOutcome, description, law, decidedBy, decidedAt, validFrom, validTo, attachments, extraParameters);
	}

	@Override
	public String toString() {
		final long errandId = errand == null ? 0 : errand.getId();

		return "Decision{" +
			"errand.id=" + errandId +
			", decisionType=" + decisionType +
			", decisionOutcome=" + decisionOutcome +
			", description='" + description + '\'' +
			", law=" + law +
			", decidedBy=" + decidedBy +
			", decidedAt=" + decidedAt +
			", validFrom=" + validFrom +
			", validTo=" + validTo +
			", attachments=" + attachments +
			", extraParameters=" + extraParameters +
			"} " + super.toString();
	}

}
