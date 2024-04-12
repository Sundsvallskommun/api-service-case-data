package se.sundsvall.casedata.integration.db.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.TimeZoneStorage;
import org.hibernate.annotations.TimeZoneStorageType;
import se.sundsvall.casedata.integration.db.model.enums.AppealStatus;
import se.sundsvall.casedata.integration.db.model.enums.TrailStatus;

import java.time.OffsetDateTime;
import java.util.Objects;

import static org.hibernate.Length.LONG;

@Entity(name = "appeal")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(setterPrefix = "with")
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
	@Column(name = "status")
	private AppealStatus status = AppealStatus.NEW;

	@Enumerated(EnumType.STRING)
	@Column(name = "trail_status")
	private TrailStatus trailStatus = TrailStatus.NOT_CONDUCTED;

	@ManyToOne
	@JoinColumn(name = "decision_id", foreignKey = @ForeignKey(name = "FK_appeal_decision_id"))
	private Decision decision;

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}
		if (object == null || getClass() != object.getClass()) {
			return false;
		}
		if (!super.equals(object)) {
			return false;
		}
		Appeal appeal = (Appeal) object;
		return Objects.equals(errand, appeal.errand) && Objects.equals(description, appeal.description) && Objects.equals(registeredAt, appeal.registeredAt) && Objects.equals(appealConcernCommunicatedAt, appeal.appealConcernCommunicatedAt) && status == appeal.status && trailStatus == appeal.trailStatus && Objects.equals(decision, appeal.decision);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), errand, description, registeredAt, appealConcernCommunicatedAt, status, trailStatus, decision);
	}

	@Override
	public String toString() {
		return "Appeal{" +
			"errand=" + errand +
			", description='" + description + '\'' +
			", registeredAt=" + registeredAt +
			", appealConcernCommunicatedAt=" + appealConcernCommunicatedAt +
			", status=" + status +
			", trailStatus=" + trailStatus +
			", decision=" + decision +
			"} " + super.toString();
	}
}
