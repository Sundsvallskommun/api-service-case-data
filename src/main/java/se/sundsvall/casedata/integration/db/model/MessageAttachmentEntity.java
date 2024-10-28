package se.sundsvall.casedata.integration.db.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "message_attachment",
	indexes = {
		@Index(name = "idx_message_attachment_municipality_id", columnList = "municipality_id"),
		@Index(name = "idx_message_attachment_namespace", columnList = "namespace")
	},
	uniqueConstraints = {
		@UniqueConstraint(name = "UK_message_attachment_data_id",
			columnNames = {
				"message_attachment_data_id"
			})
	})
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@Builder(setterPrefix = "with")
@EqualsAndHashCode
@ToString
public class MessageAttachmentEntity {

	@Id
	@Column(name = "attachmentID")
	private String attachmentId;

	@Column(name = "messageID")
	private String messageID;

	@Column(name = "municipality_id")
	private String municipalityId;

	@Column(name = "namespace")
	private String namespace;

	@OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "message_attachment_data_id", nullable = false, foreignKey = @ForeignKey(name = "fk_message_attachment_data_message_attachment"))
	private MessageAttachmentDataEntity attachmentData;

	@Column(name = "name")
	private String name;

	@Column(name = "content_type")
	private String contentType;

}
