package se.sundsvall.casedata.integration.db.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.With;
import org.hibernate.Length;
import se.sundsvall.casedata.integration.db.model.enums.Classification;
import se.sundsvall.casedata.integration.db.model.enums.Direction;

@Entity
@Table(name = "message",
	indexes = {
		@Index(name = "idx_message_municipality_id", columnList = "municipality_id"),
		@Index(name = "idx_message_namespace", columnList = "namespace"),
		@Index(name = "idx_messsage_errand_id", columnList = "errand_id")
	})
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@Builder(setterPrefix = "with")
@EqualsAndHashCode
@ToString
public class MessageEntity {

	@Id
	@Column(name = "messageID")
	private String messageId;

	@With
	@Column(name = "errand_id")
	private Long errandId;

	@Column(name = "municipality_id")
	private String municipalityId;

	@Column(name = "namespace")
	private String namespace;

	@Enumerated(EnumType.STRING)
	@Column(name = "direction")
	private Direction direction;

	@Column(name = "familyID")
	private String familyId;

	@Column(name = "external_caseID")
	private String externalCaseId;

	@Column(name = "subject")
	private String subject;

	@Column(name = "message", length = Length.LONG32)
	private String textmessage;

	@Column(name = "html_message", length = Length.LONG32)
	private String htmlMessage;

	@Column(name = "sent")
	private String sent;

	@Column(name = "username")
	private String username;

	@Column(name = "first_name")
	private String firstName;

	@Column(name = "last_name")
	private String lastName;

	@Column(name = "message_type")
	private String messageType;

	@Column(name = "mobile_number")
	private String mobileNumber;

	@Column(name = "email")
	private String email;

	@ElementCollection
	@CollectionTable(name = "message_recipients",
		joinColumns = @JoinColumn(name = "message_id"),
		foreignKey = @ForeignKey(name = "fk_message_recipients_message_id"))
	@Column(name = "recipient_email")
	private List<String> recipients;

	@Column(name = "userID")
	private String userId;

	@Column(name = "viewed", nullable = false)
	private boolean viewed;

	@Column(name = "internal")
	private Boolean internal;

	@Enumerated(EnumType.STRING)
	@Column(name = "classification")
	private Classification classification;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "messageID")
	private List<MessageAttachmentEntity> attachments;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "message_id",
		referencedColumnName = "messageID",
		foreignKey = @ForeignKey(name = "fk_message_header_message_id"))
	private List<EmailHeaderEntity> headers;

}
