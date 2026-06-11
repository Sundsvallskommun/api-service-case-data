package se.sundsvall.casedata.integration.messaging;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import se.sundsvall.casedata.api.model.CaseType;
import se.sundsvall.casedata.integration.db.model.ContactInformationEntity;
import se.sundsvall.casedata.integration.db.model.ErrandEntity;
import se.sundsvall.casedata.integration.db.model.StakeholderEntity;
import se.sundsvall.casedata.service.model.MessagingSettings;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.casedata.api.model.validation.enums.StakeholderRole.APPLICANT;
import static se.sundsvall.casedata.api.model.validation.enums.StakeholderRole.REPORTER;
import static se.sundsvall.casedata.integration.db.model.enums.ContactType.EMAIL;
import static se.sundsvall.casedata.integration.messaging.MessagingMapper.TYPE_OWNER_SUPPORT_TEXT;
import static se.sundsvall.casedata.integration.messaging.MessagingMapper.TYPE_REPORTER_SUPPORT_TEXT;

class MessagingMapperTest {

	@Test
	void toEmailRequestForOwner() {

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
		final var bean = MessagingMapper.toEmailRequest(errandEntity, messagingSettings, errandEntity.getStakeholders().getFirst(), TYPE_OWNER_SUPPORT_TEXT,
			CaseType.builder().withDisplayName(displayName).build());

		// Assert
		assertThat(bean).isNotNull().hasNoNullFieldsOrPropertiesExcept("party", "htmlMessage");
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
	void toEmailRequestForReporter() {

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
		final var bean = MessagingMapper.toEmailRequest(errandEntity, messagingSettings, errandEntity.getStakeholders().getFirst(), TYPE_REPORTER_SUPPORT_TEXT,
			CaseType.builder().withDisplayName(displayName).build());

		// Assert
		assertThat(bean).isNotNull().hasNoNullFieldsOrPropertiesExcept("party", "htmlMessage");
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
		final var errandEntity = ErrandEntity.builder()
			.withId(123L)
			.withNamespace(namespace)
			.withMunicipalityId(municipalityId)
			.withErrandNumber(errandNumber)
			.withStakeholders(List.of(StakeholderEntity.builder()
				.withPersonId("123e4567-e89b-12d3-a456-426614174000")
				.withFirstName("Test")
				.withRoles(List.of(APPLICANT.name()))
				.build()))
			.build();

		final var messagingSettings = MessagingSettings.builder()
			.withOwnerSupportText(supportText)
			.withContactInformationUrl(url)
			.withContactInformationEmail(emailAddress)
			.withSmsSender(smsSender)
			.build();

		// Act
		final var bean = MessagingMapper.toMessagingMessageRequest(errandEntity, messagingSettings, CaseType.builder().withDisplayName(displayName).build());

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

	@Test
	void findErrandOwnerPartyIdReturnsUuidForValidPersonId() {
		final var partyId = randomUUID();
		final var errandEntity = ErrandEntity.builder()
			.withStakeholders(List.of(StakeholderEntity.builder()
				.withRoles(List.of(APPLICANT.name()))
				.withPersonId(partyId.toString())
				.build()))
			.build();

		assertThat(MessagingMapper.findErrandOwnerPartyId(errandEntity)).isEqualTo(partyId);
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"556002-1361",  // organisationsnummer
		"199001011234", // personnummer
		"not-a-uuid",
		""
	})
	void findErrandOwnerPartyIdReturnsNullForNonUuidPersonId(final String personId) {
		final var errandEntity = ErrandEntity.builder()
			.withStakeholders(List.of(StakeholderEntity.builder()
				.withRoles(List.of(APPLICANT.name()))
				.withPersonId(personId)
				.build()))
			.build();

		// A non-UUID personId must not abort the notification flow - it resolves to a null partyId instead of throwing
		assertThat(MessagingMapper.findErrandOwnerPartyId(errandEntity)).isNull();
	}

	@Test
	void findErrandOwnerPartyIdReturnsNullWhenNoStakeholders() {
		assertThat(MessagingMapper.findErrandOwnerPartyId(ErrandEntity.builder().build())).isNull();
	}

	@Test
	void toMessagingMessageRequestDoesNotThrowForNonUuidPersonId() {
		final var errandEntity = ErrandEntity.builder()
			.withId(123L)
			.withNamespace("my-namespace")
			.withMunicipalityId("2281")
			.withErrandNumber("KS-26060031")
			.withStakeholders(List.of(StakeholderEntity.builder()
				.withRoles(List.of(APPLICANT.name()))
				.withFirstName("Test")
				.withPersonId("556002-1361")
				.build()))
			.build();
		final var messagingSettings = MessagingSettings.builder()
			.withContactInformationEmail("noreply@example.com")
			.withSmsSender("TestSender")
			.build();

		final var request = MessagingMapper.toMessagingMessageRequest(errandEntity, messagingSettings, CaseType.builder().withDisplayName("displayName").build());

		assertThat(request.getMessages()).hasSize(1);
		assertThat(request.getMessages().getFirst().getParty().getPartyId()).isNull();
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
	void toFilterSTring() {
		final var namespace = "my-namespace";
		final var departmentName = "my-department-name";

		final var result = MessagingMapper.toFilterString(namespace, departmentName);

		assertThat(result).isEqualTo("exists(values.key: 'namespace' and values.value: 'my-namespace') and exists(values.key: 'department_name' and values.value: 'my-department-name')");
	}
}
