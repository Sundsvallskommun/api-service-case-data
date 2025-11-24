package se.sundsvall.casedata.integration.messaging;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.casedata.api.model.validation.enums.StakeholderRole.APPLICANT;
import static se.sundsvall.casedata.integration.db.model.enums.ContactType.EMAIL;

import java.util.List;
import org.junit.jupiter.api.Test;
import se.sundsvall.casedata.integration.db.model.ContactInformationEntity;
import se.sundsvall.casedata.integration.db.model.ErrandEntity;
import se.sundsvall.casedata.integration.db.model.StakeholderEntity;
import se.sundsvall.casedata.service.model.MessagingSettings;

class MessagingMapperTest {

	@Test
	void toEmailRequest() {

		// Arrange
		final var firstName = "Test";
		final var namespace = "my-namespace";
		final var municipalityId = "2281";
		final var caseTitleAddition = "Case Title Addition";
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
			.withCaseTitleAddition(caseTitleAddition)
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
			.withSupportText(supportText)
			.withContactInformationUrl(url)
			.withContactInformationEmailName(emailName)
			.withContactInformationEmail(emailAddress)
			.withSmsSender(smsSender)
			.build();

		// Act
		final var bean = MessagingMapper.toEmailRequest(errandEntity, messagingSettings, errandEntity.getStakeholders().getFirst());

		// Assert
		assertThat(bean).isNotNull().hasNoNullFieldsOrPropertiesExcept("party", "htmlMessage");
		assertThat(bean.getSubject()).isEqualTo("Nytt meddelande kopplat till ärendet Case Title Addition 123456789");
		assertThat(bean.getMessage()).isEqualTo("""
			Hej Test,
			Du har fått ett nytt meddelande kopplat till ditt ärende gällande Case Title Addition, 123456789
			Gå in på Mina Sidor via länken för att visa meddelandet: https://example.com/contact/privat/arenden/123

			Sundsvalls kommun
			""");
		assertThat(bean.getSender().getName()).isEqualTo(emailName);
		assertThat(bean.getSender().getAddress()).isEqualTo(emailAddress);

	}

	@Test
	void toMessagingMessageRequest() {

		// Arrange
		final var namespace = "my-namespace";
		final var municipalityId = "2281";
		final var caseTitleAddition = "Case Title Addition";
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
			.withCaseTitleAddition(caseTitleAddition)
			.withErrandNumber(errandNumber)
			.withStakeholders(List.of(StakeholderEntity.builder()
				.withPersonId("123e4567-e89b-12d3-a456-426614174000")
				.withFirstName("Test")
				.withRoles(List.of(APPLICANT.name()))
				.build()))
			.build();

		final var messagingSettings = MessagingSettings.builder()
			.withSupportText(supportText)
			.withContactInformationUrl(url)
			.withContactInformationEmail(emailAddress)
			.withSmsSender(smsSender)
			.build();

		// Act
		final var bean = MessagingMapper.toMessagingMessageRequest(errandEntity, messagingSettings);

		// Assert
		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.getMessages()).hasSize(1);
		assertThat(bean.getMessages().getFirst().getMessage())
			.isEqualTo("""
				Hej Test,
				Du har fått ett nytt meddelande kopplat till ditt ärende gällande Case Title Addition, 123456789
				Gå in på Mina Sidor via länken för att visa meddelandet: https://example.com/contact/privat/arenden/123

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
	void toFilterSTring() {
		final var namespace = "my-namespace";
		final var departmentName = "my-department-name";

		final var result = MessagingMapper.toFilterString(namespace, departmentName);

		assertThat(result).isEqualTo("exists(values.key: 'namespace' and values.value: 'my-namespace') and exists(values.key: 'department_name' and values.value: 'my-department-name')");
	}
}
