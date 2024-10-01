package se.sundsvall.casedata.integration.db.model;

import static org.hibernate.Length.LONG;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

import com.fasterxml.jackson.annotation.JsonBackReference;
import org.hibernate.annotations.TimeZoneStorage;
import org.hibernate.annotations.TimeZoneStorageType;

import se.sundsvall.casedata.integration.db.listeners.DecisionListener;
import se.sundsvall.casedata.integration.db.model.enums.DecisionOutcome;
import se.sundsvall.casedata.integration.db.model.enums.DecisionType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "decision",
	indexes = {
		@Index(name = "idx_decision_municipality_id", columnList = "municipality_id"),
		@Index(name = "idx_decision_namespace", columnList = "namespace")
	})
@EntityListeners(DecisionListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(setterPrefix = "with")
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Decision extends BaseEntity {

	@ManyToOne
	@JoinColumn(name = "errand_id", foreignKey = @ForeignKey(name = "FK_decision_errand_id"))
	@JsonBackReference
	private Errand errand;

	@Column(name = "municipality_id")
	private String municipalityId;

	@Column(name = "namespace")
	private String namespace;

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

}
