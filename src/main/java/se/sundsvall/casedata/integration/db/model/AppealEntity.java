package se.sundsvall.casedata.integration.db.model;

import static org.hibernate.Length.LONG;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.Column;
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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.OffsetDateTime;
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
import se.sundsvall.casedata.integration.db.listeners.AppealListener;
import se.sundsvall.casedata.integration.db.model.enums.AppealStatus;
import se.sundsvall.casedata.integration.db.model.enums.TimelinessReview;

@Entity
@Table(name = "appeal",
	indexes = {
		@Index(name = "idx_appeal_municipality_id", columnList = "municipality_id"),
		@Index(name = "idx_appeal_namespace", columnList = "namespace")
	})
@EntityListeners(AppealListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(setterPrefix = "with")
@EqualsAndHashCode
public class AppealEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	@DiffIgnore
	private Long id;

	@Version
	@Column(name = "version")
	@DiffIgnore
	private int version;

	@ManyToOne
	@JoinColumn(name = "errand_id", foreignKey = @ForeignKey(name = "FK_appeal_errand_id"))
	@JsonBackReference
	private ErrandEntity errand;

	@Column(name = "municipality_id")
	private String municipalityId;

	@Column(name = "namespace")
	private String namespace;

	@Column(name = "description", length = LONG)
	private String description;

	@TimeZoneStorage(TimeZoneStorageType.NORMALIZE)
	@Column(name = "registered_at")
	private OffsetDateTime registeredAt;

	@TimeZoneStorage(TimeZoneStorageType.NORMALIZE)
	@Column(name = "appeal_concern_communicated_at")
	private OffsetDateTime appealConcernCommunicatedAt;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", columnDefinition = "varchar(255)")
	@Builder.Default
	private AppealStatus status = AppealStatus.NEW;

	@Enumerated(EnumType.STRING)
	@Column(name = "timeliness_review", columnDefinition = "varchar(255)")
	@Builder.Default
	private TimelinessReview timelinessReview = TimelinessReview.NOT_CONDUCTED;

	@ManyToOne
	@JoinColumn(name = "decision_id", foreignKey = @ForeignKey(name = "FK_appeal_decision_id"))
	private DecisionEntity decision;

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

	@Override
	public String toString() {
		var errandId = errand != null ? errand.getId() : null;
		return "AppealEntity{" +
			"id=" + id +
			", version=" + version +
			", errand=" + errandId +
			", municipalityId='" + municipalityId + '\'' +
			", namespace='" + namespace + '\'' +
			", description='" + description + '\'' +
			", registeredAt=" + registeredAt +
			", appealConcernCommunicatedAt=" + appealConcernCommunicatedAt +
			", status=" + status +
			", timelinessReview=" + timelinessReview +
			", decision=" + decision +
			", created=" + created +
			", updated=" + updated +
			'}';
	}

}
