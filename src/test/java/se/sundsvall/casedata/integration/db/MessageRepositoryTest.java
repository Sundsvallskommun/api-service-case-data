package se.sundsvall.casedata.integration.db;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;
import static se.sundsvall.casedata.TestUtil.MUNICIPALITY_ID;
import static se.sundsvall.casedata.integration.db.model.enums.Direction.INBOUND;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import se.sundsvall.casedata.api.filter.IncomingRequestFilter;
import se.sundsvall.casedata.api.model.validation.enums.MessageType;
import se.sundsvall.casedata.integration.db.config.JaversConfiguration;
import se.sundsvall.casedata.integration.db.listeners.ErrandListener;
import se.sundsvall.casedata.integration.db.model.Message;


/**
 * MessageAttachmentRepository tests.
 *
 * @see /src/test/resources/db/testdata-junit.sql for data setup.
 */
@DataJpaTest
@Import(value = {JaversConfiguration.class, ErrandListener.class, IncomingRequestFilter.class})
@Transactional
@AutoConfigureTestDatabase(replace = NONE)
@ActiveProfiles("junit")
@Sql(scripts = {
	"/db/scripts/truncate.sql",
	"/db/scripts/testdata-junit.sql"
})
class MessageRepositoryTest {

	@Autowired
	private MessageRepository messageRepository;

	@Test
	void findById() {

		// Arrange
		final var id = "02485d15-fa8b-488a-a907-fa4de5d6e5c9";

		// Act
		final var result = messageRepository.findById(id).orElseThrow();

		// Assert
		assertThat(result.getDirection()).isEqualTo(INBOUND);
		assertThat(result.getEmail()).isEqualTo("test.testorsson@noreply.com");
		assertThat(result.getErrandNumber()).isEqualTo("PRE-1970-000123");
		assertThat(result.getExternalCaseID()).isEqualTo("123456");
		assertThat(result.getFamilyID()).isEqualTo("123");
		assertThat(result.getFirstName()).isEqualTo("Test");
		assertThat(result.getLastName()).isEqualTo("Testorsson");
		assertThat(result.getTextmessage()).isEqualTo("Some message");
		assertThat(result.getSent()).isEqualTo("2023-10-02 15:13:45.363");
		assertThat(result.getSubject()).isEqualTo("Some subject");
		assertThat(result.getUserID()).isEqualTo("aba01cal");
		assertThat(result.getUsername()).isEqualTo("Abacus Calculator");
	}

	@Test
	void findByIdNothingFound() {

		// Arrange
		final var id = "NON-EXISTING";

		// Act
		final var result = messageRepository.findById(id);

		// Assert
		assertThat(result).isNotNull().isEmpty();
	}

	@Test
	void findAllByErrandNumber() {

		// Arrange
		final var errandNumber = "PRE-1970-000123";

		// Act
		final var result = messageRepository.findAllByErrandNumberAndMunicipalityId(errandNumber, MUNICIPALITY_ID);

		// Assert
		assertThat(result)
			.hasSize(1)
			.allSatisfy(obj -> {
				assertThat(obj.getDirection()).isEqualTo(INBOUND);
				assertThat(obj.getEmail()).isEqualTo("test.testorsson@noreply.com");
				assertThat(obj.getErrandNumber()).isEqualTo("PRE-1970-000123");
				assertThat(obj.getExternalCaseID()).isEqualTo("123456");
				assertThat(obj.getFamilyID()).isEqualTo("123");
				assertThat(obj.getFirstName()).isEqualTo("Test");
				assertThat(obj.getLastName()).isEqualTo("Testorsson");
				assertThat(obj.getTextmessage()).isEqualTo("Some message");
				assertThat(obj.getSent()).isEqualTo("2023-10-02 15:13:45.363");
				assertThat(obj.getSubject()).isEqualTo("Some subject");
				assertThat(obj.getUserID()).isEqualTo("aba01cal");
				assertThat(obj.getUsername()).isEqualTo("Abacus Calculator");
			});
	}

	@Test
	void findAllByErrandNumberNothingFound() {

		// Arrange
		final var errandNumber = "NON-EXISTING";

		// Act
		final var result = messageRepository.findAllByErrandNumberAndMunicipalityId(errandNumber, MUNICIPALITY_ID);

		// Assert
		assertThat(result).isNotNull().isEmpty();
	}

	@Test
	void create() {

		// Arrange
		final var direction = INBOUND;
		final var email = "email";
		final var errandNumber = "errandNumber";
		final var externalCaseId = "externalCaseId";
		final var familyId = "familyId";
		final var firstName = "firstName";
		final var lastName = "lastName";
		final var messageId = "messageId";
		final var messageType = MessageType.EMAIL;
		final var mobileNumber = "mobileNumber";
		final var subject = "subject";
		final var textMessage = "textMessage";
		final var userId = "userId";
		final var username = "username";
		final var viewed = true;

		final var entity = Message.builder()
			.withDirection(direction)
			.withEmail(email)
			.withErrandNumber(errandNumber)
			.withExternalCaseID(externalCaseId)
			.withFamilyID(familyId)
			.withFirstName(firstName)
			.withLastName(lastName)
			.withMessageID(messageId)
			.withMessageType(messageType.name())
			.withMobileNumber(mobileNumber)
			.withSubject(subject)
			.withTextmessage(textMessage)
			.withUserID(userId)
			.withUsername(username)
			.withViewed(viewed)
			.build();

		// Act
		final var result = messageRepository.saveAndFlush(entity);

		// Assert
		assertThat(result).isNotNull();
		assertThat(result.getMessageID()).isNotEmpty();
		assertThat(result.getDirection()).isEqualTo(direction);
		assertThat(result.getEmail()).isEqualTo(email);
		assertThat(result.getErrandNumber()).isEqualTo(errandNumber);
		assertThat(result.getExternalCaseID()).isEqualTo(externalCaseId);
		assertThat(result.getFamilyID()).isEqualTo(familyId);
		assertThat(result.getFirstName()).isEqualTo(firstName);
		assertThat(result.getLastName()).isEqualTo(lastName);
		assertThat(result.getMessageID()).isEqualTo(messageId);
		assertThat(result.getMessageType()).isEqualTo(messageType.name());
		assertThat(result.getMobileNumber()).isEqualTo(mobileNumber);
		assertThat(result.getSubject()).isEqualTo(subject);
		assertThat(result.getTextmessage()).isEqualTo(textMessage);
		assertThat(result.getUserID()).isEqualTo(userId);
		assertThat(result.getUsername()).isEqualTo(username);
		assertThat(result.isViewed()).isTrue();
	}

}
