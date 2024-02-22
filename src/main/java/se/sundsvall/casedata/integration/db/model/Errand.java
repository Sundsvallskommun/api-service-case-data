package se.sundsvall.casedata.integration.db.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
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
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.TimeZoneStorage;
import org.hibernate.annotations.TimeZoneStorageType;
import org.javers.core.metamodel.annotation.DiffIgnore;
import se.sundsvall.casedata.integration.db.listeners.ErrandListener;
import se.sundsvall.casedata.integration.db.model.enums.Channel;
import se.sundsvall.casedata.integration.db.model.enums.Priority;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity(name = "errand")
@Table(uniqueConstraints = {@UniqueConstraint(name = "UK_errand_errand_number", columnNames = {"errand_number"})})
@EntityListeners(ErrandListener.class)
@Getter
@Setter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(setterPrefix = "with")
public class Errand extends BaseEntity {

	@Column(name = "errand_number", nullable = false)
	private String errandNumber;

	@Column(name = "external_case_id")
	private String externalCaseId;

	@Column(name = "case_type")
	private String caseType;

	@Enumerated(EnumType.STRING)
	@Column(name = "channel")
	private Channel channel;

	@Enumerated(EnumType.STRING)
	@Column(name = "priority")
	private Priority priority;

	@Column(name = "description")
	private String description;

	@Column(name = "case_title_addition")
	private String caseTitleAddition;

	@Column(name = "diary_number")
	private String diaryNumber;

	@Column(name = "phase")
	private String phase;

	@ElementCollection
	@CollectionTable(name = "errand_statuses",
		joinColumns = @JoinColumn(name = "errand_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "FK_errand_statuses_errand_id")))
	@OrderColumn(name = "status_order")
	@Builder.Default
	private List<Status> statuses = new ArrayList<>();

	@Column(name = "municipality_id")
	private String municipalityId;

	@Column(name = "start_date")
	private LocalDate startDate;

	@Column(name = "end_date")
	private LocalDate endDate;

	@TimeZoneStorage(TimeZoneStorageType.NORMALIZE)
	@Column(name = "application_received")
	private OffsetDateTime applicationReceived;

	@DiffIgnore
	@Column(name = "process_id")
	private String processId;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "errand")
	@JsonManagedReference
	@Builder.Default
	private List<Stakeholder> stakeholders = new ArrayList<>();

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "errand")
	@JsonManagedReference
	@Builder.Default
	private List<Facility> facilities = new ArrayList<>();

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "errand")
	@JsonManagedReference
	@Builder.Default
	private List<Decision> decisions = new ArrayList<>();

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "errand")
	@JsonManagedReference
	@Builder.Default
	private List<Note> notes = new ArrayList<>();

	// WSO2-client
	@Column(name = "created_by_client")
	@DiffIgnore
	private String createdByClient;

	// WSO2-client
	@Column(name = "updated_by_client")
	@DiffIgnore
	private String updatedByClient;

	// AD-user
	@Column(name = "created_by", length = 36)
	@DiffIgnore
	private String createdBy;

	// AD-user
	@Column(name = "updated_by", length = 36)
	@DiffIgnore
	private String updatedBy;

	@ElementCollection
	@CollectionTable(name = "errand_extra_parameters",
		joinColumns = @JoinColumn(name = "errand_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "FK_errand_extra_parameters_errand_id")))
	@MapKeyColumn(name = "extra_parameter_key")
	@Column(name = "extra_parameter_value")
	@Builder.Default
	private Map<String, String> extraParameters = new HashMap<>();

}
