package se.sundsvall.casedata.integration.db.model;

import static org.hibernate.annotations.TimeZoneStorageType.NORMALIZE;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.TimeZoneStorage;
import org.hibernate.annotations.UuidGenerator;
import se.sundsvall.casedata.integration.db.listeners.NotificationListener;

@Entity
@Table(name = "notification",
	indexes = {
		@Index(name = "idx_notification_municipality_id", columnList = "municipality_id"),
		@Index(name = "idx_notification_namespace", columnList = "namespace"),
		@Index(name = "idx_notification_owner_id", columnList = "owner_id")

	})
@EntityListeners(NotificationListener.class)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(setterPrefix = "with")
@EqualsAndHashCode
public class NotificationEntity {

	@Id
	@UuidGenerator
	@Column(name = "id")
	private String id;

	@Column(name = "created")
	@TimeZoneStorage(NORMALIZE)
	private OffsetDateTime created;

	@Column(name = "modified")
	@TimeZoneStorage(NORMALIZE)
	private OffsetDateTime modified;

	@Column(name = "owner_full_name")
	private String ownerFullName;

	@Column(name = "owner_id")
	private String ownerId;

	@Column(name = "created_by")
	private String createdBy;

	@Column(name = "created_by_full_name")
	private String createdByFullName;

	@Column(name = "type")
	private String type;

	@Column(name = "description")
	private String description;

	@Column(name = "content")
	private String content;

	@Column(name = "expires")
	private OffsetDateTime expires;

	@Column(name = "acknowledged")
	private boolean acknowledged;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "errand_id", nullable = true, foreignKey = @ForeignKey(name = "fk_notification_errand_id"))
	private ErrandEntity errand;

	@Column(name = "municipality_id", nullable = false)
	private String municipalityId;

	@Column(name = "namespace", nullable = false)
	private String namespace;
}
