package se.sundsvall.casedata.integration.db.model;

import static org.hibernate.annotations.TimeZoneStorageType.NORMALIZE;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.TimeZoneStorage;
import org.hibernate.annotations.TimeZoneStorageType;
import org.hibernate.annotations.UpdateTimestamp;
import org.javers.core.metamodel.annotation.DiffIgnore;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.TimeZoneStorage;
import org.hibernate.annotations.TimeZoneStorageType;
import org.hibernate.annotations.UpdateTimestamp;
import org.javers.core.metamodel.annotation.DiffIgnore;

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
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import se.sundsvall.casedata.integration.db.listeners.ErrandListener;
import se.sundsvall.casedata.integration.db.model.enums.Channel;
import se.sundsvall.casedata.integration.db.model.enums.Priority;

@Entity
@Table(name = "errand",
	indexes = {
		@Index(name = "idx_errand_municipality_id", columnList = "municipality_id"),
		@Index(name = "idx_errand_namespace", columnList = "namespace")
	},
	uniqueConstraints = {
		@UniqueConstraint(name = "UK_errand_errand_number", columnNames = {
			"errand_number"
		})
	})
@EntityListeners(ErrandListener.class)
@Getter
@Setter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
@EqualsAndHashCode
@Builder(setterPrefix = "with")
public class ErrandEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	@DiffIgnore
	private Long id;

	@Version
	@Column(name = "version")
	@DiffIgnore
	private int version;

	@Column(name = "errand_number", nullable = false)
	private String errandNumber;

	@Column(name = "municipality_id")
	private String municipalityId;

	@Column(name = "namespace")
	private String namespace;

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

	@Column(name = "description", length = 8192)
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
	private List<StatusEntity> statuses;

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
	private List<StakeholderEntity> stakeholders;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "errand")
	@JsonManagedReference
	private List<FacilityEntity> facilities;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "errand")
	@JsonManagedReference
	private List<DecisionEntity> decisions;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "errand")
	@JsonManagedReference
	private List<AppealEntity> appeals;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "errand")
	@JsonManagedReference
	private List<NoteEntity> notes;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "errand")
	private List<NotificationEntity> notifications;

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

	@Column(name = "suspended_to")
	@TimeZoneStorage(NORMALIZE)
	private OffsetDateTime suspendedTo;

	@Column(name = "suspended_from")
	@TimeZoneStorage(NORMALIZE)
	private OffsetDateTime suspendedFrom;

	@OneToMany(mappedBy = "errandEntity", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<ExtraParameterEntity> extraParameters;

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("ErrandEntity [id=").append(id).append(", version=").append(version).append(", errandNumber=").append(errandNumber).append(", municipalityId=").append(municipalityId).append(", namespace=").append(namespace).append(
			", externalCaseId=").append(externalCaseId).append(", caseType=").append(caseType).append(", channel=").append(channel).append(", priority=").append(priority).append(", description=").append(description).append(", caseTitleAddition=").append(
				caseTitleAddition).append(", diaryNumber=").append(diaryNumber).append(", phase=").append(phase).append(", statuses=").append(statuses).append(", startDate=").append(startDate).append(", endDate=").append(endDate).append(
					", applicationReceived=").append(applicationReceived).append(", processId=").append(processId).append(", stakeholders=").append(stakeholders).append(", facilities=").append(facilities).append(", decisions=").append(decisions).append(
						", appeals=").append(appeals).append(", notes=").append(notes).append(", notifications=").append(notifications).append(", createdByClient=").append(createdByClient).append(", updatedByClient=").append(updatedByClient).append(
							", createdBy=").append(createdBy).append(", updatedBy=").append(updatedBy).append(", created=").append(created).append(", updated=").append(updated).append(", suspendedTo=").append(suspendedTo).append(", suspendedFrom=").append(
								suspendedFrom).append(", extraParameters=").append(extraParameters).append("]");
		return builder.toString();
	}
}
