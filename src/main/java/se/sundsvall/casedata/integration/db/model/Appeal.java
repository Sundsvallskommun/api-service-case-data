package se.sundsvall.casedata.integration.db.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.TimeZoneStorage;
import org.hibernate.annotations.TimeZoneStorageType;
import se.sundsvall.casedata.integration.db.listeners.AppealListener;
import se.sundsvall.casedata.integration.db.model.enums.AppealStatus;
import se.sundsvall.casedata.integration.db.model.enums.TimelinessReview;

import java.time.OffsetDateTime;

import static org.hibernate.Length.LONG;

@Entity(name = "appeal")
@EntityListeners(AppealListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(setterPrefix = "with")
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Appeal extends BaseEntity {

	@ManyToOne
	@JoinColumn(name = "errand_id", foreignKey = @ForeignKey(name = "FK_appeal_errand_id"))
	@JsonBackReference
	private Errand errand;

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
	private AppealStatus status = AppealStatus.NEW;

	@Enumerated(EnumType.STRING)
	@Column(name = "timeliness_review", columnDefinition = "varchar(255)")
	private TimelinessReview timelinessReview = TimelinessReview.NOT_CONDUCTED;

	@ManyToOne
	@JoinColumn(name = "decision_id", foreignKey = @ForeignKey(name = "FK_appeal_decision_id"))
	private Decision decision;

}
