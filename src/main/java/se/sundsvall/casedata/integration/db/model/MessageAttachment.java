package se.sundsvall.casedata.integration.db.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
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
@Table(uniqueConstraints = {@UniqueConstraint(name = "UK_message_attachment_data_id", columnNames = {"message_attachment_data_id"})})
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@Builder(setterPrefix = "with")
@EqualsAndHashCode
@ToString
public class MessageAttachment {

	@Id
	private String attachmentID;

	private String messageID;

	@OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "message_attachment_data_id", nullable = false, foreignKey = @ForeignKey(name = "fk_message_attachment_data_message_attachment"))
	private MessageAttachmentData attachmentData;

	private String name;

	private String contentType;

}
