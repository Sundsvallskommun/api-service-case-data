package se.sundsvall.casedata.integration.messaging;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static se.sundsvall.casedata.api.model.validation.enums.StakeholderRole.APPLICANT;
import static se.sundsvall.casedata.integration.db.model.enums.ContactType.EMAIL;

import com.nimbusds.oauth2.sdk.util.StringUtils;
import generated.se.sundsvall.messaging.Email;
import generated.se.sundsvall.messaging.EmailRequest;
import generated.se.sundsvall.messaging.EmailSender;
import generated.se.sundsvall.messaging.MessageParty;
import generated.se.sundsvall.messaging.MessageRequest;
import generated.se.sundsvall.messaging.MessageSender;
import generated.se.sundsvall.messaging.Sms;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import se.sundsvall.casedata.integration.db.model.ContactInformationEntity;
import se.sundsvall.casedata.integration.db.model.ErrandEntity;
import se.sundsvall.casedata.integration.db.model.StakeholderEntity;
import se.sundsvall.casedata.service.model.MessagingSettings;

public final class MessagingMapper {

	private static final String FILTER_TEMPLATE = "exists(values.key: 'namespace' and values.value: '%s') and exists(values.key: 'department_name' and values.value: '%s')";
	private static final String SUBJECT_TEMPLATE = "Nytt meddelande kopplat till ärendet %s %s";

	private MessagingMapper() {
		// Private constructor to prevent instantiation
	}

	public static EmailRequest toEmailRequest(final ErrandEntity errandEntity, final MessagingSettings messagingSettings, final StakeholderEntity stakeholderEntity) {
		return new EmailRequest()
			.subject(SUBJECT_TEMPLATE.formatted(errandEntity.getCaseTitleAddition(), errandEntity.getErrandNumber()))
			.message(createBody(errandEntity, messagingSettings))
			.emailAddress(findStakeholderEmail(stakeholderEntity))
			.sender(new EmailSender()
				.name(messagingSettings.getContactInformationEmailName())
				.address(messagingSettings.getContactInformationEmail()));
	}

	public static MessageRequest toMessagingMessageRequest(final ErrandEntity errandEntity, final MessagingSettings messagingSettings) {
		return new MessageRequest()
			.messages(List.of(new generated.se.sundsvall.messaging.Message()
				.subject(SUBJECT_TEMPLATE.formatted(errandEntity.getCaseTitleAddition(), errandEntity.getErrandNumber()))
				.message(createBody(errandEntity, messagingSettings))
				.party(new MessageParty().partyId(findErrandOwnerPartyId(errandEntity)))
				.sender(new MessageSender()
					.sms(new Sms()
						.name(messagingSettings.getSmsSender()))
					.email(new Email()
						.name(messagingSettings.getContactInformationEmail())
						.address(messagingSettings.getContactInformationEmail())))));
	}

	static String createBody(final ErrandEntity errandEntity, final MessagingSettings messagingSettings) {
		return ofNullable(messagingSettings.getSupportText())
			.filter(StringUtils::isNotBlank)
			.map(supportText -> String.format(
				messagingSettings.getSupportText(),
				findErrandOwnerFirstName(errandEntity),
				errandEntity.getCaseTitleAddition(),
				errandEntity.getErrandNumber(),
				messagingSettings.getContactInformationUrl(),
				errandEntity.getId()))
			.orElse("");
	}

	static String findErrandOwnerFirstName(final ErrandEntity errandEntity) {
		return ofNullable(errandEntity.getStakeholders())
			.orElse(emptyList())
			.stream()
			.filter(stakeholder -> isNotEmpty(stakeholder.getRoles()))
			.filter(stakeholder -> stakeholder.getRoles().contains(APPLICANT.name()))
			.findFirst()
			.map(StakeholderEntity::getFirstName)
			.orElse(null);
	}

	static UUID findErrandOwnerPartyId(final ErrandEntity errandEntity) {
		return ofNullable(errandEntity.getStakeholders())
			.orElse(emptyList())
			.stream()
			.filter(stakeholder -> isNotEmpty(stakeholder.getRoles()))
			.filter(stakeholder -> stakeholder.getRoles().contains(APPLICANT.name()))
			.findFirst()
			.map(StakeholderEntity::getPersonId)
			.map(UUID::fromString)
			.orElse(null);
	}

	static String findStakeholderEmail(final StakeholderEntity stakeholderEntity) {
		return ofNullable(stakeholderEntity.getContactInformation())
			.orElse(emptyList())
			.stream()
			.filter(contactInformation -> Objects.equals(EMAIL, contactInformation.getContactType()))
			.findFirst()
			.map(ContactInformationEntity::getValue)
			.orElse(null);
	}

	public static String toFilterString(final String namespace, final String departmentName) {
		return FILTER_TEMPLATE.formatted(namespace, departmentName);
	}
}
