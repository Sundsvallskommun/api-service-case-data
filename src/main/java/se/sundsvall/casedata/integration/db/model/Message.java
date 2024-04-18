package se.sundsvall.casedata.integration.db.model;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;

import org.hibernate.Length;

import se.sundsvall.casedata.integration.db.model.enums.Classification;
import se.sundsvall.casedata.integration.db.model.enums.Direction;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.With;

@Entity
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@Builder(setterPrefix = "with")
@EqualsAndHashCode
@ToString
public class Message {

	@Id
	private String messageID;

	@With
	private String errandNumber;

	@Enumerated(EnumType.STRING)
	private Direction direction;

	private String familyID;

	private String externalCaseID;

	private String subject;

	@Column(name = "message", length = Length.LONG32)
	private String textmessage;

	private String sent;

	private String username;

	private String firstName;

	private String lastName;

	private String messageType;

	private String mobileNumber;

	private String email;

	private String userID;

	private boolean viewed;

	@Enumerated(EnumType.STRING)
	private Classification classification;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "messageID")
	private List<MessageAttachment> attachments;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "message_id", referencedColumnName = "messageID",
		foreignKey = @ForeignKey(name = "fk_message_header_message_id"))
	private List<EmailHeader> headers;

}
