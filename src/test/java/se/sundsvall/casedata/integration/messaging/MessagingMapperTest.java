package se.sundsvall.casedata.integration.messaging;

import generated.se.sundsvall.messaging.Party;
import java.util.Base64;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import se.sundsvall.casedata.api.model.BulkEmailRequest;
import se.sundsvall.casedata.api.model.CaseType;
import se.sundsvall.casedata.api.model.MessageRequest;
import se.sundsvall.casedata.integration.db.model.ContactInformationEntity;
import se.sundsvall.casedata.integration.db.model.ErrandEntity;
import se.sundsvall.casedata.integration.db.model.StakeholderEntity;
import se.sundsvall.casedata.service.model.MessagingSettings;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.casedata.api.model.validation.enums.StakeholderRole.APPLICANT;
import static se.sundsvall.casedata.api.model.validation.enums.StakeholderRole.REPORTER;
import static se.sundsvall.casedata.integration.db.model.enums.ContactType.EMAIL;
import static se.sundsvall.casedata.integration.messaging.MessagingMapper.TYPE_OWNER_SUPPORT_TEXT;
import static se.sundsvall.casedata.integration.messaging.MessagingMapper.TYPE_REPORTER_SUPPORT_TEXT;

class MessagingMapperTest {

	@Test
	void toEmailBatchRequestForOwner() {

		// Arrange
		final var firstName = "Test";
		final var namespace = "my-namespace";
		final var municipalityId = "2281";
		final var displayName = "Case type displayName";
		final var errandNumber = "123456789";
		final var emailAddress = "test™@example.com";
		final var emailName = "Test";
		final var supportText = """
			Hej %s,
			Du har fått ett nytt meddelande kopplat till ditt ärende gällande %s, %s
			Gå in på Mina Sidor via länken för att visa meddelandet: %s/privat/arenden/%s

			Sundsvalls kommun
			""";
		final var smsSender = "TestSender";
		final var url = "https://example.com/contact";
		final var errandEntity = ErrandEntity.builder()
			.withId(123L)
			.withNamespace(namespace)
			.withMunicipalityId(municipalityId)
			.withErrandNumber(errandNumber)
			.withStakeholders(List.of(StakeholderEntity.builder()
				.withFirstName(firstName)
				.withContactInformation(List.of(
					ContactInformationEntity.builder()
						.withContactType(EMAIL)
						.withValue(emailAddress)
						.build()))
				.withRoles(List.of(APPLICANT.name()))
				.build()))
			.build();
		final var messagingSettings = MessagingSettings.builder()
			.withOwnerSupportText(supportText)
			.withContactInformationUrl(url)
			.withContactInformationEmailName(emailName)
			.withContactInformationEmail(emailAddress)
			.withSmsSender(smsSender)
			.build();

		// Act
		final var bean = MessagingMapper.toEmailBatchRequest(errandEntity, messagingSettings, List.of(emailAddress), firstName, TYPE_OWNER_SUPPORT_TEXT,
			CaseType.builder().withDisplayName(displayName).build());

		// Assert
		assertThat(bean).isNotNull().hasNoNullFieldsOrPropertiesExcept("htmlMessage");
		assertThat(bean.getParties()).containsExactly(new Party(emailAddress));
		assertThat(bean.getSubject()).isEqualTo("Nytt meddelande kopplat till ärendet Case type displayName 123456789");
		assertThat(bean.getMessage()).isEqualTo("""
			Hej Test,
			Du har fått ett nytt meddelande kopplat till ditt ärende gällande Case type displayName, 123456789
			Gå in på Mina Sidor via länken för att visa meddelandet: https://example.com/contact/privat/arenden/123456789

			Sundsvalls kommun
			""");
		assertThat(bean.getSender().getName()).isEqualTo(emailName);
		assertThat(bean.getSender().getAddress()).isEqualTo(emailAddress);
	}

	@Test
	void toEmailBatchRequestForReporter() {

		// Arrange
		final var firstName = "Test";
		final var namespace = "my-namespace";
		final var municipalityId = "2281";
		final var displayName = "Case type displayName";
		final var errandNumber = "123456789";
		final var emailAddress = "test™@example.com";
		final var emailName = "Test";
		final var supportText = """
			Hej %s,
			Ett nytt meddelande har skapats kopplat till ärende gällande %s, %s där du är
			rapportör.
			Gå in på Katla via länken för att visa meddelandet: %s/subpath/arenden/%s

			Avsändare
			""";

		final var smsSender = "TestSender";
		final var url = "https://example.com/contact";
		final var errandEntity = ErrandEntity.builder()
			.withId(123L)
			.withNamespace(namespace)
			.withMunicipalityId(municipalityId)
			.withErrandNumber(errandNumber)
			.withStakeholders(List.of(StakeholderEntity.builder()
				.withFirstName(firstName)
				.withContactInformation(List.of(
					ContactInformationEntity.builder()
						.withContactType(EMAIL)
						.withValue(emailAddress)
						.build()))
				.withRoles(List.of(REPORTER.name()))
				.build()))
			.build();
		final var messagingSettings = MessagingSettings.builder()
			.withReporterSupportText(supportText)
			.withKatlaUrl(url)
			.withContactInformationEmailName(emailName)
			.withContactInformationEmail(emailAddress)
			.withSmsSender(smsSender)
			.build();

		// Act
		final var bean = MessagingMapper.toEmailBatchRequest(errandEntity, messagingSettings, List.of(emailAddress), firstName, TYPE_REPORTER_SUPPORT_TEXT,
			CaseType.builder().withDisplayName(displayName).build());

		// Assert
		assertThat(bean).isNotNull().hasNoNullFieldsOrPropertiesExcept("htmlMessage");
		assertThat(bean.getParties()).containsExactly(new Party(emailAddress));
		assertThat(bean.getSubject()).isEqualTo("Nytt meddelande kopplat till ärendet Case type displayName 123456789");
		assertThat(bean.getMessage()).isEqualTo("""
			Hej Test,
			Ett nytt meddelande har skapats kopplat till ärende gällande Case type displayName, 123456789 där du är
			rapportör.
			Gå in på Katla via länken för att visa meddelandet: https://example.com/contact/subpath/arenden/123456789

			Avsändare
			""");
		assertThat(bean.getSender().getName()).isEqualTo(emailName);
		assertThat(bean.getSender().getAddress()).isEqualTo(emailAddress);

	}

	@Test
	void toEmailBatchRequestWithMultipleRecipientsCreatesOnePartyPerRecipient() {
		// Arrange
		final var firstEmail = "first@example.com";
		final var secondEmail = "second@example.com";
		final var errandEntity = ErrandEntity.builder()
			.withErrandNumber("123456789")
			.build();
		final var messagingSettings = MessagingSettings.builder()
			.withReporterSupportText("Hej %s, %s %s %s%s")
			.build();

		// Act
		final var bean = MessagingMapper.toEmailBatchRequest(errandEntity, messagingSettings, List.of(firstEmail, secondEmail), "Test", TYPE_REPORTER_SUPPORT_TEXT,
			CaseType.builder().withDisplayName("displayName").build());

		// Assert - one combined batch request carries a party per recipient, instead of one request per recipient
		assertThat(bean.getParties()).containsExactly(new Party(firstEmail), new Party(secondEmail));
	}

	@Test
	void toEmailBatchRequestForBulkEmailRequestCreatesOnePartyPerRecipient() {
		// Arrange
		final var firstEmail = "first@example.com";
		final var secondEmail = "second@example.com";
		final var senderEmail = "sender@example.com";
		final var senderName = "Sender Name";
		final var request = BulkEmailRequest.builder()
			.withRecipients(List.of(firstEmail, secondEmail))
			.withSubject("Subject")
			.withMessage("Message in plain text")
			.withHtmlMessage("<p>Message in html</p>")
			.withDepartmentName("CONVERSATION")
			.withAttachments(List.of(MessageRequest.AttachmentRequest.builder()
				.withName("file.txt")
				.withContentType("text/plain")
				.withContent("aGVsbG8=")
				.build()))
			.build();
		final var messagingSettings = MessagingSettings.builder()
			.withContactInformationEmail(senderEmail)
			.withContactInformationEmailName(senderName)
			.build();

		// Act
		final var bean = MessagingMapper.toEmailBatchRequest(request, messagingSettings, MessagingMapper.toEmailAttachments(request.getAttachments()));

		// Assert
		assertThat(bean.getParties()).containsExactly(new Party(firstEmail), new Party(secondEmail));
		assertThat(bean.getSubject()).isEqualTo("Subject");
		assertThat(bean.getMessage()).isEqualTo("Message in plain text");
		assertThat(bean.getHtmlMessage()).isEqualTo(Base64.getEncoder().encodeToString("<p>Message in html</p>".getBytes(UTF_8)));
		assertThat(bean.getSender().getName()).isEqualTo(senderName);
		assertThat(bean.getSender().getAddress()).isEqualTo(senderEmail);
		assertThat(bean.getAttachments()).hasSize(1).first().satisfies(attachment -> {
			assertThat(attachment.getName()).isEqualTo("file.txt");
			assertThat(attachment.getContentType()).isEqualTo("text/plain");
			assertThat(attachment.getContent()).isEqualTo("aGVsbG8=");
		});
	}

	@Test
	void toEmailAttachmentsReturnsEmptyListForNullInput() {
		assertThat(MessagingMapper.toEmailAttachments(null)).isEmpty();
	}

	@Test
	void toMessagingMessageRequest() {

		// Arrange
		final var namespace = "my-namespace";
		final var municipalityId = "2281";
		final var displayName = "Case type displayName";
		final var errandNumber = "123456789";
		final var emailAddress = "test™@example.com";
		final var supportText = """
			Hej %s,
			Du har fått ett nytt meddelande kopplat till ditt ärende gällande %s, %s
			Gå in på Mina Sidor via länken för att visa meddelandet: %s/privat/arenden/%s

			Sundsvalls kommun
			""";
		final var smsSender = "TestSender";
		final var url = "https://example.com/contact";
		final var applicant = StakeholderEntity.builder()
			.withPersonId("123e4567-e89b-12d3-a456-426614174000")
			.withFirstName("Test")
			.withRoles(List.of(APPLICANT.name()))
			.build();
		final var errandEntity = ErrandEntity.builder()
			.withId(123L)
			.withNamespace(namespace)
			.withMunicipalityId(municipalityId)
			.withErrandNumber(errandNumber)
			.withStakeholders(List.of(applicant))
			.build();

		final var messagingSettings = MessagingSettings.builder()
			.withOwnerSupportText(supportText)
			.withContactInformationUrl(url)
			.withContactInformationEmail(emailAddress)
			.withSmsSender(smsSender)
			.build();

		// Act
		final var bean = MessagingMapper.toMessagingMessageRequest(errandEntity, messagingSettings, List.of(applicant), CaseType.builder().withDisplayName(displayName).build());

		// Assert
		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.getMessages()).hasSize(1);
		assertThat(bean.getMessages().getFirst().getMessage())
			.isEqualTo("""
				Hej Test,
				Du har fått ett nytt meddelande kopplat till ditt ärende gällande Case type displayName, 123456789
				Gå in på Mina Sidor via länken för att visa meddelandet: https://example.com/contact/privat/arenden/123456789

				Sundsvalls kommun
				""");
		assertThat(bean.getMessages().getFirst().getSender()).isNotNull();
		assertThat(bean.getMessages().getFirst().getSender().getEmail()).isNotNull();
		assertThat(bean.getMessages().getFirst().getSender().getEmail().getName()).isEqualTo(emailAddress);
		assertThat(bean.getMessages().getFirst().getSender().getEmail().getAddress()).isEqualTo(emailAddress);
		assertThat(bean.getMessages().getFirst().getSender().getSms()).isNotNull();
		assertThat(bean.getMessages().getFirst().getSender().getSms().getName()).isEqualTo(smsSender);
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"556002-1361",  // organisationsnummer
		"199001011234", // personnummer
		"not-a-uuid",
		""
	})
	void toMessagingMessageRequestDoesNotThrowForNonUuidPersonId(final String personId) {
		final var applicant = StakeholderEntity.builder()
			.withRoles(List.of(APPLICANT.name()))
			.withFirstName("Test")
			.withPersonId(personId)
			.build();
		final var errandEntity = ErrandEntity.builder()
			.withId(123L)
			.withNamespace("my-namespace")
			.withMunicipalityId("2281")
			.withErrandNumber("KS-26060031")
			.withStakeholders(List.of(applicant))
			.build();
		final var messagingSettings = MessagingSettings.builder()
			.withContactInformationEmail("noreply@example.com")
			.withSmsSender("TestSender")
			.build();

		// A non-UUID personId must not abort the notification flow - it resolves to a null partyId instead of throwing
		final var request = MessagingMapper.toMessagingMessageRequest(errandEntity, messagingSettings, List.of(applicant), CaseType.builder().withDisplayName("displayName").build());

		assertThat(request.getMessages()).hasSize(1);
		assertThat(request.getMessages().getFirst().getParty().getPartyId()).isNull();
	}

	@Test
	void toMessagingMessageRequestBuildsOnePersonalizedMessagePerRecipient() {
		// Arrange - each recipient must get their own message, addressed with their own first name and party id,
		// instead of a single message derived from an arbitrary stakeholder on the errand
		final var firstApplicantPartyId = randomUUID();
		final var secondApplicantPartyId = randomUUID();
		final var firstApplicant = StakeholderEntity.builder()
			.withRoles(List.of(APPLICANT.name()))
			.withFirstName("Anna")
			.withPersonId(firstApplicantPartyId.toString())
			.build();
		final var secondApplicant = StakeholderEntity.builder()
			.withRoles(List.of(APPLICANT.name()))
			.withFirstName("Bertil")
			.withPersonId(secondApplicantPartyId.toString())
			.build();
		final var errandEntity = ErrandEntity.builder()
			.withId(123L)
			.withNamespace("my-namespace")
			.withMunicipalityId("2281")
			.withErrandNumber("123456789")
			.withStakeholders(List.of(firstApplicant, secondApplicant))
			.build();
		final var messagingSettings = MessagingSettings.builder()
			.withOwnerSupportText("Hej %s, %s %s %s%s")
			.withContactInformationEmail("noreply@example.com")
			.withSmsSender("TestSender")
			.build();

		// Act
		final var request = MessagingMapper.toMessagingMessageRequest(errandEntity, messagingSettings, List.of(firstApplicant, secondApplicant),
			CaseType.builder().withDisplayName("displayName").build());

		// Assert
		assertThat(request.getMessages()).hasSize(2);
		assertThat(request.getMessages().get(0).getMessage()).startsWith("Hej Anna,");
		assertThat(request.getMessages().get(0).getParty().getPartyId()).isEqualTo(firstApplicantPartyId);
		assertThat(request.getMessages().get(1).getMessage()).startsWith("Hej Bertil,");
		assertThat(request.getMessages().get(1).getParty().getPartyId()).isEqualTo(secondApplicantPartyId);
	}

	@Test
	void findFirstRecipientFirstNameReturnsFirstNameOfFirstStakeholderWithEmail() {
		final var withoutEmail = StakeholderEntity.builder().withFirstName("NoEmail").build();
		final var withEmail = StakeholderEntity.builder()
			.withFirstName("Anna")
			.withContactInformation(List.of(ContactInformationEntity.builder().withContactType(EMAIL).withValue("anna@example.com").build()))
			.build();

		assertThat(MessagingMapper.findFirstRecipientFirstName(List.of(withoutEmail, withEmail))).isEqualTo("Anna");
	}

	@Test
	void findFirstRecipientFirstNameReturnsNullForNullInput() {
		assertThat(MessagingMapper.findFirstRecipientFirstName(null)).isNull();
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"556002-1361",
		"not-a-uuid",
		"   ",
		""
	})
	@NullSource
	void toUuidOrNullReturnsNullForInvalidValues(final String value) {
		assertThat(MessagingMapper.toUuidOrNull(value)).isNull();
	}

	@Test
	void toUuidOrNullReturnsUuidForValidValue() {
		final var uuid = randomUUID();
		assertThat(MessagingMapper.toUuidOrNull(uuid.toString())).isEqualTo(uuid);
	}

	@Test
	void findStakeholderEmailsReturnsDistinctNonBlankEmails() {
		final var firstEmail = "first@example.com";
		final var secondEmail = "second@example.com";
		final var stakeholders = List.of(
			StakeholderEntity.builder().withContactInformation(List.of(ContactInformationEntity.builder().withContactType(EMAIL).withValue(firstEmail).build())).build(),
			StakeholderEntity.builder().withContactInformation(List.of(ContactInformationEntity.builder().withContactType(EMAIL).withValue(secondEmail).build())).build(),
			StakeholderEntity.builder().withContactInformation(List.of(ContactInformationEntity.builder().withContactType(EMAIL).withValue(firstEmail).build())).build(),
			StakeholderEntity.builder().build());

		assertThat(MessagingMapper.findStakeholderEmails(stakeholders)).containsExactlyInAnyOrder(firstEmail, secondEmail);
	}

	@Test
	void findStakeholderEmailsReturnsEmptyListForNullInput() {
		assertThat(MessagingMapper.findStakeholderEmails(null)).isEmpty();
	}

	@Test
	void toFilterSTring() {
		final var namespace = "my-namespace";
		final var departmentName = "my-department-name";

		final var result = MessagingMapper.toFilterString(namespace, departmentName);

		assertThat(result).isEqualTo("exists(values.key: 'namespace' and values.value: 'my-namespace') and exists(values.key: 'department_name' and values.value: 'my-department-name')");
	}
}
