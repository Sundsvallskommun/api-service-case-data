package se.sundsvall.casedata.service.scheduler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.groups.Tuple.tuple;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.MOCK;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mariadb.jdbc.MariaDbBlob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ActiveProfiles;
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;

import se.sundsvall.casedata.Application;
import se.sundsvall.casedata.api.model.MessageRequest.AttachmentRequest;
import se.sundsvall.casedata.api.model.MessageResponse;
import se.sundsvall.casedata.api.model.MessageResponse.AttachmentResponse;
import se.sundsvall.casedata.api.model.validation.enums.MessageType;
import se.sundsvall.casedata.integration.db.model.EmailHeader;
import se.sundsvall.casedata.integration.db.model.Message;
import se.sundsvall.casedata.integration.db.model.MessageAttachment;
import se.sundsvall.casedata.integration.db.model.MessageAttachmentData;
import se.sundsvall.casedata.integration.db.model.enums.Direction;
import se.sundsvall.casedata.integration.db.model.enums.Header;
import se.sundsvall.dept44.common.validators.annotation.impl.ValidUuidConstraintValidator;

import generated.se.sundsvall.webmessagecollector.MessageDTO;
import generated.se.sundsvall.webmessagecollector.MessageDTO.DirectionEnum;

@SpringBootTest(classes = {Application.class}, webEnvironment = MOCK)
@ActiveProfiles("junit")
class MessageMapperTest {

	private static final ValidUuidConstraintValidator UUID_VALIDATOR = new ValidUuidConstraintValidator();

	@Autowired
	private MessageMapper messageMapper;

	@Test
	void testAnnotation() {
		assertThat(MessageMapper.class).hasAnnotation(Component.class);
	}

	@Test
	void testToMessageResponseFromNull() {
		assertThat(messageMapper.toMessageResponse(null)).isNull();
	}

	@Test
	void testToMessageResponseFromEmptyBean() {
		assertThat(messageMapper.toMessageResponse(Message.builder().build()))
			.hasAllNullFieldsOrPropertiesExcept("viewed")
			.extracting(MessageResponse::isViewed).isEqualTo(false);
	}

	@Test
	void testToMessageResponses() {
		final var bean = createMessage();
		final var list = List.of(bean);

		final var dto = messageMapper.toMessageResponses(list);

		assertThat(dto).isNotNull()
			.hasSize(1)
			.extracting(
				MessageResponse::getDirection,
				MessageResponse::getEmail,
				MessageResponse::getErrandNumber,
				MessageResponse::getExternalCaseID,
				MessageResponse::getFamilyID,
				MessageResponse::getFirstName,
				MessageResponse::getLastName,
				MessageResponse::getMessage,
				MessageResponse::getMessageID,
				MessageResponse::getMessageType,
				MessageResponse::getMobileNumber,
				MessageResponse::getSent,
				MessageResponse::getSubject,
				MessageResponse::getUserID,
				MessageResponse::getUsername)
			.containsExactly(tuple(
				bean.getDirection(),
				bean.getEmail(),
				bean.getErrandNumber(),
				bean.getExternalCaseID(),
				bean.getFamilyID(),
				bean.getFirstName(),
				bean.getLastName(),
				bean.getTextmessage(),
				bean.getMessageID(),
				bean.getMessageType(),
				bean.getMobileNumber(),
				bean.getSent(),
				bean.getSubject(),
				bean.getUserID(),
				bean.getUsername()));
		assertThat(dto.getFirst().getEmailHeaders()).allSatisfy(s -> {
			assertThat(s.getHeader()).isNotNull().isInstanceOf(Header.class);
			assertThat(s.getValues()).isNotNull().isNotEmpty();
		});
	}

	@Test
	void testToMessageResponse() {
		final var bean = createMessage();

		final var dto = messageMapper.toMessageResponse(bean);

		assertThat(dto).isNotNull()
			.extracting(
				MessageResponse::getDirection,
				MessageResponse::getEmail,
				MessageResponse::getErrandNumber,
				MessageResponse::getExternalCaseID,
				MessageResponse::getFamilyID,
				MessageResponse::getFirstName,
				MessageResponse::getLastName,
				MessageResponse::getMessage,
				MessageResponse::getMessageID,
				MessageResponse::getMessageType,
				MessageResponse::getMobileNumber,
				MessageResponse::getSent,
				MessageResponse::getSubject,
				MessageResponse::getUserID,
				MessageResponse::getUsername)
			.containsExactly(
				bean.getDirection(),
				bean.getEmail(),
				bean.getErrandNumber(),
				bean.getExternalCaseID(),
				bean.getFamilyID(),
				bean.getFirstName(),
				bean.getLastName(),
				bean.getTextmessage(),
				bean.getMessageID(),
				bean.getMessageType(),
				bean.getMobileNumber(),
				bean.getSent(),
				bean.getSubject(),
				bean.getUserID(),
				bean.getUsername());

		assertThat(dto.getEmailHeaders()).allSatisfy(s -> {
			assertThat(s.getHeader()).isNotNull().isInstanceOf(Header.class);
			assertThat(s.getValues()).isNotNull().isNotEmpty();
		});

		assertThat(dto.getAttachments()).isNotEmpty()
			.extracting(
				AttachmentResponse::getAttachmentID,
				AttachmentResponse::getContentType,
				AttachmentResponse::getName)
			.containsExactly(tuple(
				bean.getAttachments().getFirst().getAttachmentID(),
				bean.getAttachments().getFirst().getContentType(),
				bean.getAttachments().getFirst().getName()));
	}

	@Test
	void testToAttachmentDto() {
		final var attachmentID = "attachmentID";
		final var contentType = "contentType";
		final var name = "name";
		final var content = "content";
		final var messageAttachment = MessageAttachment.builder()
			.withAttachmentData(MessageAttachmentData.builder()
				.withFile(new MariaDbBlob(content.getBytes()))
				.build())
			.withAttachmentID(attachmentID)
			.withContentType(contentType)
			.withName(name)
			.build();

		final var dto = messageMapper.toAttachmentDto(messageAttachment);

		assertThat(dto.getAttachmentID()).isEqualTo(attachmentID);
		assertThat(dto.getContent()).isEqualTo(Base64.getEncoder().encodeToString(content.getBytes()));
		assertThat(dto.getContentType()).isEqualTo(contentType);
		assertThat(dto.getName()).isEqualTo(name);
	}

	@Test
	void testToAttachmentDtoWithException() {
		final var e = assertThrows(ThrowableProblem.class, () -> messageMapper.toAttachmentDto(null));

		assertThat(e.getStatus()).isEqualTo(Status.INTERNAL_SERVER_ERROR);
		assertThat(e.getMessage()).isEqualTo("Internal Server Error: Failed to convert binary stream to base64 representation");
	}

	@Test
	void testToAttachmentEntity() throws Exception {

		final var messageID = "messageID";
		final var content = new String(Base64.getEncoder().encode("content".getBytes()), StandardCharsets.UTF_8);
		final var contentType = "contentType";
		final var name = "name";
		final var attachmentRequest = AttachmentRequest.builder()
			.withContent(content)
			.withContentType(contentType)
			.withName(name)
			.build();

		final var bean = messageMapper.toAttachmentEntity(attachmentRequest, messageID);

		assertThat(bean.getAttachmentData()).isNotNull();
		assertThat(bean.getAttachmentData().getId()).isZero();
		assertThat(bean.getAttachmentData().getFile().getBinaryStream().readAllBytes()).isEqualTo("content".getBytes());
		assertThat(bean.getAttachmentID()).isNotBlank().satisfies(s -> assertThat(isValidUUID(s)).isTrue());
		assertThat(bean.getContentType()).isEqualTo(contentType);
		assertThat(bean.getMessageID()).isEqualTo(messageID);
		assertThat(bean.getName()).isEqualTo(name);
	}

	@Test
	void toAttachmentEntity() {
		final var messageID = "messageID";
		final var contentType = "contentType";
		final var name = "name";
		final var attachmentID = "12";
		final var messageAttachment = new generated.se.sundsvall.webmessagecollector.MessageAttachment()
			.name(name)
			.extension(contentType)
			.mimeType(contentType)
			.attachmentId(Integer.valueOf(attachmentID));

		// Act
		final var bean = messageMapper.toAttachmentEntity(messageAttachment, messageID);

		// Assert
		assertThat(bean.getAttachmentData()).isNull();
		assertThat(bean.getAttachmentID()).isEqualTo(attachmentID);
		assertThat(bean.getContentType()).isEqualTo(contentType);
		assertThat(bean.getMessageID()).isEqualTo(messageID);
		assertThat(bean.getName()).isEqualTo(name);
	}

	@Test
	void testToMessageEntity() {
		final var errandNumber = "errandNumber";
		final var direction = DirectionEnum.OUTBOUND;
		final var email = "email";
		final var externalCaseId = "externalCaseId";
		final var familyId = "familyId";
		final var firstName = "firstName";
		final var id = 987;
		final var lastName = "lastName";
		final var message = "message";
		final var messageId = "messageId";
		final var sent = "sent";
		final var userId = "userId";
		final var username = "username";

		final var attachmentId = 12;
		final var name = "name";
		final var extension = "extension";
		final var mimeType = "mimeType";
		final var attachments = List.of(new generated.se.sundsvall.webmessagecollector.MessageAttachment()
			.name(name)
			.extension(extension)
			.mimeType(mimeType)
			.attachmentId(attachmentId));
		final var dto = new MessageDTO()
			.direction(direction)
			.email(email)
			.externalCaseId(externalCaseId)
			.familyId(familyId)
			.firstName(firstName)
			.id(id)
			.lastName(lastName)
			.message(message)
			.messageId(messageId)
			.sent(sent)
			.userId(userId)
			.username(username)
			.attachments(attachments);

		final var bean = messageMapper.toMessageEntity(errandNumber, dto);

		assertThat(bean.getDirection()).isEqualTo(Direction.OUTBOUND);
		assertThat(bean.getEmail()).isEqualTo(email);
		assertThat(bean.getErrandNumber()).isEqualTo(errandNumber);
		assertThat(bean.getExternalCaseID()).isEqualTo(externalCaseId);
		assertThat(bean.getFamilyID()).isEqualTo(familyId);
		assertThat(bean.getFirstName()).isEqualTo(firstName);
		assertThat(bean.getLastName()).isEqualTo(lastName);
		assertThat(bean.getMessageID()).satisfies(s -> assertThat(isValidUUID(s)).isTrue());
		assertThat(bean.getMessageType()).isEqualTo(MessageType.WEBMESSAGE.name());
		assertThat(bean.getMobileNumber()).isNull();
		assertThat(bean.getSent()).isEqualTo(sent);
		assertThat(bean.getSubject()).isNull();
		assertThat(bean.getTextmessage()).isEqualTo(message);
		assertThat(bean.getUserID()).isEqualTo(userId);
		assertThat(bean.getUsername()).isEqualTo(username);
		assertThat(bean.getAttachments()).isNull();

	}

	@Test
	void toAttachment() {
		// Arrange
		final var attachment = MessageAttachment.builder()
			.withAttachmentData(MessageAttachmentData.builder()
				.withFile(new MariaDbBlob("content".getBytes()))
				.build())
			.withAttachmentID("attachmentID")
			.withContentType("contentType")
			.withName("name")
			.build();

		// Act
		final var result = messageMapper.toAttachment(attachment);

		// Assert
		assertThat(result.getFile()).isEqualTo(Base64.getEncoder().encodeToString("content".getBytes()));
		assertThat(result.getName()).isEqualTo("name");

	}

	@Test
	void toAttachmentWhenAttachmentIsNull() {
		// Arrange
		final var attachment = MessageAttachment.builder()
			.withAttachmentData(null)
			.withAttachmentID("attachmentID")
			.withContentType("contentType")
			.withName("name")
			.build();

		// Act
		assertThatThrownBy(() -> messageMapper.toAttachment(attachment))
			.isInstanceOf(ThrowableProblem.class)
			.hasMessage("Internal Server Error: Failed to convert binary stream to base64 representation");

	}

	private boolean isValidUUID(final String uuid) {
		return UUID_VALIDATOR.isValid(uuid);
	}

	private Message createMessage() {
		final var attachmentID = "attachmentID";
		final var attachmentData = MessageAttachmentData.builder().build();
		final var contentType = "contentType";
		final var messageID = "messageID";
		final var name = "name";
		final var attachments = List.of(MessageAttachment.builder()
			.withAttachmentID(attachmentID)
			.withAttachmentData(attachmentData)
			.withContentType(contentType)
			.withMessageID(messageID)
			.withName(name)
			.build());
		final var headers = List.of(
			EmailHeader.builder()
				.withHeader(Header.MESSAGE_ID)
				.withValues(List.of("<Test@Test>"))
				.build(),
			EmailHeader.builder()
				.withHeader(Header.IN_REPLY_TO)
				.withValues(List.of("<Test@Test>"))
				.build()
		);
		final var direction = Direction.INBOUND;
		final var email = "email";
		final var errandNumber = "errandNumber";
		final var externalCaseID = "externalCaseID";
		final var familyID = "familyID";
		final var firstName = "firstName";
		final var lastName = "lastName";
		final var textmessage = "textmessage";
		final var messageType = MessageType.EMAIL;
		final var mobileNumber = "mobileNumber";
		final var sent = "sent";
		final var subject = "subject";
		final var userID = "userID";
		final var username = "username";
		final var viewed = true;

		return Message.builder()
			.withAttachments(attachments)
			.withDirection(direction)
			.withEmail(email)
			.withErrandNumber(errandNumber)
			.withExternalCaseID(externalCaseID)
			.withFamilyID(familyID)
			.withFirstName(firstName)
			.withLastName(lastName)
			.withTextmessage(textmessage)
			.withMessageID(messageID)
			.withMessageType(messageType.name())
			.withMobileNumber(mobileNumber)
			.withSent(sent)
			.withSubject(subject)
			.withUserID(userID)
			.withUsername(username)
			.withViewed(viewed)
			.withHeaders(headers)
			.build();
	}

}
