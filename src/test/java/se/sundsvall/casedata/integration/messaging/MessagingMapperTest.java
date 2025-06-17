package se.sundsvall.casedata.integration.messaging;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.casedata.api.model.validation.enums.StakeholderRole.APPLICANT;

import generated.se.sundsvall.messagingsettings.SenderInfoResponse;
import java.util.List;
import org.junit.jupiter.api.Test;
import se.sundsvall.casedata.integration.db.model.ErrandEntity;
import se.sundsvall.casedata.integration.db.model.StakeholderEntity;

class MessagingMapperTest {

	@Test
	void toMessagingMessageRequest() {

		// Arrange
		final var namespace = "my-namespace";
		final var municipalityId = "2281";
		final var caseTitleAddition = "Case Title Addition";
		final var errandNumber = "123456789";
		final var emailAddress = "test™@example.com";
		final var phoneNumber = "123456789";
		final var supportText = "Support text for %s with errand number %s. Contact us at %s";
		final var smsSender = "TestSender";
		final var url = "https://example.com/contact";
		final var errandEntity = ErrandEntity.builder()
			.withNamespace(namespace)
			.withMunicipalityId(municipalityId)
			.withCaseTitleAddition(caseTitleAddition)
			.withErrandNumber(errandNumber)
			.withStakeholders(List.of(StakeholderEntity.builder()
				.withPersonId("123e4567-e89b-12d3-a456-426614174000")
				.withRoles(List.of(APPLICANT.name()))
				.build()))
			.build();

		final var senderInfo = new SenderInfoResponse()
			.supportText(supportText)
			.contactInformationUrl(url)
			.contactInformationPhoneNumber(phoneNumber)
			.contactInformationEmail(emailAddress)
			.smsSender(smsSender);

		// Act
		final var bean = MessagingMapper.toMessagingMessageRequest(errandEntity, senderInfo);

		// Assert
		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.getMessages()).hasSize(1);
		assertThat(bean.getMessages().getFirst().getMessage())
			.isEqualTo("Support text for " + caseTitleAddition + " with errand number " + errandNumber + ". Contact us at " + url);
		assertThat(bean.getMessages().getFirst().getSender()).isNotNull();
		assertThat(bean.getMessages().getFirst().getSender().getEmail()).isNotNull();
		assertThat(bean.getMessages().getFirst().getSender().getEmail().getAddress()).isEqualTo(emailAddress);
		assertThat(bean.getMessages().getFirst().getSender().getSms()).isNotNull();
		assertThat(bean.getMessages().getFirst().getSender().getSms().getName()).isEqualTo(smsSender);

	}
}
