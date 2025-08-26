package se.sundsvall.casedata.integration.messaging;

import static java.util.Collections.emptyList;
import static se.sundsvall.casedata.api.model.validation.enums.StakeholderRole.APPLICANT;
import static se.sundsvall.casedata.integration.db.model.enums.ContactType.EMAIL;

import generated.se.sundsvall.messaging.Email;
import generated.se.sundsvall.messaging.EmailRequest;
import generated.se.sundsvall.messaging.EmailSender;
import generated.se.sundsvall.messaging.MessageParty;
import generated.se.sundsvall.messaging.MessageRequest;
import generated.se.sundsvall.messaging.MessageSender;
import generated.se.sundsvall.messaging.Sms;
import generated.se.sundsvall.messagingsettings.SenderInfoResponse;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import se.sundsvall.casedata.integration.db.model.ContactInformationEntity;
import se.sundsvall.casedata.integration.db.model.ErrandEntity;
import se.sundsvall.casedata.integration.db.model.StakeholderEntity;

public final class MessagingMapper {

	private MessagingMapper() {
		// Private constructor to prevent instantiation
	}

	public static EmailRequest toEmailRequest(final ErrandEntity errandEntity, final SenderInfoResponse senderInfo, final StakeholderEntity stakeholderEntity) {
		return new EmailRequest()
			.subject("Nytt meddelande kopplat till ärendet " + errandEntity.getCaseTitleAddition() + " " + errandEntity.getErrandNumber())
			.message(createBody(errandEntity, senderInfo))
			.emailAddress(findStakeholderEmail(stakeholderEntity))
			.sender(new EmailSender()
				.name(senderInfo.getContactInformationEmailName())
				.address(senderInfo.getContactInformationEmail()));
	}

	public static MessageRequest toMessagingMessageRequest(final ErrandEntity errandEntity, final SenderInfoResponse senderInfo) {

		return new MessageRequest()
			.messages(List.of(new generated.se.sundsvall.messaging.Message()
				.subject("Nytt meddelande kopplat till ärendet " + errandEntity.getCaseTitleAddition() + " " + errandEntity.getErrandNumber())
				.message(createBody(errandEntity, senderInfo))
				.party(new MessageParty().partyId(findErrandOwnerPartyId(errandEntity)))
				.sender(new MessageSender()
					.sms(new Sms()
						.name(senderInfo.getSmsSender()))
					.email(new Email()
						.name(senderInfo.getContactInformationEmail())
						.address(senderInfo.getContactInformationEmail())))));

	}

	static String createBody(final ErrandEntity errandEntity, final SenderInfoResponse senderInfo) {

		if (senderInfo.getSupportText() == null || senderInfo.getSupportText().isBlank()) {
			return "";
		}

		return String.format(
			senderInfo.getSupportText(),
			findErrandOwnerFirstName(errandEntity),
			errandEntity.getCaseTitleAddition(),
			errandEntity.getErrandNumber(),
			senderInfo.getContactInformationUrl(),
			errandEntity.getId());
	}

	static String findErrandOwnerFirstName(final ErrandEntity errandEntity) {

		return Optional.ofNullable(errandEntity.getStakeholders())
			.orElse(emptyList())
			.stream()
			.filter(stakeholder -> stakeholder.getRoles().contains(APPLICANT.name()))
			.findFirst()
			.map(StakeholderEntity::getFirstName)
			.orElse(null);
	}

	static UUID findErrandOwnerPartyId(final ErrandEntity errandEntity) {

		final var partyIdString = Optional.ofNullable(errandEntity.getStakeholders())
			.orElse(emptyList())
			.stream()
			.filter(stakeholder -> stakeholder.getRoles().contains(APPLICANT.name()))
			.findFirst()
			.map(StakeholderEntity::getPersonId)
			.orElse(null);

		if (partyIdString == null || partyIdString.isBlank()) {
			return null;
		}
		return UUID.fromString(partyIdString);
	}

	static String findStakeholderEmail(final StakeholderEntity stakeholderEntity) {

		return Optional.ofNullable(stakeholderEntity.getContactInformation())
			.orElse(emptyList())
			.stream()
			.filter(contactInformation -> contactInformation.getContactType() != null && contactInformation.getContactType().equals(EMAIL))
			.findFirst()
			.map(ContactInformationEntity::getValue)
			.orElse(null);
	}
}
