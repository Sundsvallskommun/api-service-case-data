package se.sundsvall.casedata.integration.messaging;

import com.nimbusds.oauth2.sdk.util.StringUtils;
import generated.se.sundsvall.messaging.Email;
import generated.se.sundsvall.messaging.EmailAttachment;
import generated.se.sundsvall.messaging.EmailBatchRequest;
import generated.se.sundsvall.messaging.EmailSender;
import generated.se.sundsvall.messaging.MessageParty;
import generated.se.sundsvall.messaging.MessageRequest;
import generated.se.sundsvall.messaging.MessageSender;
import generated.se.sundsvall.messaging.Party;
import generated.se.sundsvall.messaging.Sms;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import se.sundsvall.casedata.api.model.BulkEmailRequest;
import se.sundsvall.casedata.api.model.CaseType;
import se.sundsvall.casedata.integration.db.model.ContactInformationEntity;
import se.sundsvall.casedata.integration.db.model.ErrandEntity;
import se.sundsvall.casedata.integration.db.model.StakeholderEntity;
import se.sundsvall.casedata.service.model.MessagingSettings;

import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;
import static se.sundsvall.casedata.integration.db.model.enums.ContactType.EMAIL;

public final class MessagingMapper {

	public static final int TYPE_OWNER_SUPPORT_TEXT = 1;
	public static final int TYPE_REPORTER_SUPPORT_TEXT = 2;
	private static final String FILTER_TEMPLATE = "exists(values.key: 'namespace' and values.value: '%s') and exists(values.key: 'department_name' and values.value: '%s')";
	private static final String SUBJECT_TEMPLATE = "Nytt meddelande kopplat till ärendet %s %s";

	private MessagingMapper() {
		// Private constructor to prevent instantiation
	}

	/**
	 * Builds a batch email request for a templated notification, sent to every recipient in {@code recipientEmails} as
	 * an individual email via Messaging's batch endpoint. Since the batch endpoint sends the same message to every
	 * party in one call, the greeting can only be personalized with a single name - {@code greetingFirstName} should
	 * therefore be the first name of one of the actual recipients (typically the first), not an unrelated stakeholder.
	 *
	 * @param  errandEntity      the errand the notification concerns
	 * @param  messagingSettings the messaging settings to resolve support text, sender address, etc. from
	 * @param  recipientEmails   the e-mail addresses to send the notification to
	 * @param  greetingFirstName the first name to use in the support text greeting
	 * @param  supportTextType   which support text (owner or reporter) to build the message from
	 * @param  caseType          the case type of the errand
	 * @return                   the resulting batch email request
	 */
	public static EmailBatchRequest toEmailBatchRequest(final ErrandEntity errandEntity, final MessagingSettings messagingSettings, final List<String> recipientEmails, final String greetingFirstName, int supportTextType, CaseType caseType) {
		return new EmailBatchRequest()
			.parties(recipientEmails.stream()
				.map(Party::new)
				.toList())
			.subject(SUBJECT_TEMPLATE.formatted(caseType.getDisplayName(), errandEntity.getErrandNumber()))
			.message(createBody(errandEntity, messagingSettings, supportTextType, caseType, greetingFirstName))
			.sender(new EmailSender()
				.name(messagingSettings.getContactInformationEmailName())
				.address(messagingSettings.getContactInformationEmail()));
	}

	/**
	 * Builds a batch email request for a manually composed message, sent to every recipient in the request as an
	 * individual email via Messaging's batch endpoint. Unlike {@link #toEmailBatchRequest(ErrandEntity, MessagingSettings,
	 * List, int, CaseType)}, the subject and message are taken verbatim from the caller instead of being built from a
	 * templated support text.
	 *
	 * @param  request           the bulk email request containing recipients, subject, message and attachments
	 * @param  messagingSettings the messaging settings to resolve the sender address from
	 * @param  attachments       the attachments to include on the email
	 * @return                   the resulting batch email request
	 */
	public static EmailBatchRequest toEmailBatchRequest(final BulkEmailRequest request, final MessagingSettings messagingSettings, final List<EmailAttachment> attachments) {
		return new EmailBatchRequest()
			.parties(request.getRecipients().stream()
				.map(Party::new)
				.toList())
			.subject(request.getSubject())
			.message(request.getMessage())
			.htmlMessage(ofNullable(request.getHtmlMessage())
				.map(htmlMessage -> Base64.getEncoder().encodeToString(htmlMessage.getBytes(StandardCharsets.UTF_8)))
				.orElse(null))
			.attachments(attachments)
			.sender(new EmailSender()
				.name(messagingSettings.getContactInformationEmailName())
				.address(messagingSettings.getContactInformationEmail()));
	}

	public static List<EmailAttachment> toEmailAttachments(final List<se.sundsvall.casedata.api.model.MessageRequest.AttachmentRequest> attachmentRequests) {
		return ofNullable(attachmentRequests)
			.orElse(emptyList())
			.stream()
			.map(attachment -> new EmailAttachment()
				.name(attachment.getName())
				.contentType(attachment.getContentType())
				.content(attachment.getContent()))
			.toList();
	}

	/**
	 * Builds a message request sending one individually personalized message per recipient via Messaging's /messages
	 * endpoint, so that each recipient's own first name is used in their greeting and only the recipients actually
	 * meant to be notified (as determined by the caller) receive a message.
	 *
	 * @param  errandEntity      the errand the notification concerns
	 * @param  messagingSettings the messaging settings to resolve support text, sender address, etc. from
	 * @param  recipients        the stakeholders to send a message to
	 * @param  caseType          the case type of the errand
	 * @return                   the resulting message request, containing one message per recipient
	 */
	public static MessageRequest toMessagingMessageRequest(final ErrandEntity errandEntity, final MessagingSettings messagingSettings, final List<StakeholderEntity> recipients, CaseType caseType) {
		return new MessageRequest()
			.messages(recipients.stream()
				.map(recipient -> new generated.se.sundsvall.messaging.Message()
					.subject(SUBJECT_TEMPLATE.formatted(caseType.getDisplayName(), errandEntity.getErrandNumber()))
					.message(createBody(errandEntity, messagingSettings, TYPE_OWNER_SUPPORT_TEXT, caseType, recipient.getFirstName()))
					.party(new MessageParty().partyId(toUuidOrNull(recipient.getPersonId())))
					.sender(new MessageSender()
						.sms(new Sms()
							.name(messagingSettings.getSmsSender()))
						.email(new Email()
							.name(messagingSettings.getContactInformationEmail())
							.address(messagingSettings.getContactInformationEmail()))))
				.toList());
	}

	static String createBody(final ErrandEntity errandEntity, final MessagingSettings messagingSettings, int supportTextType, CaseType caseType, final String greetingFirstName) {
		final var nullableSupportText = TYPE_REPORTER_SUPPORT_TEXT == supportTextType ? messagingSettings.getReporterSupportText() : messagingSettings.getOwnerSupportText();

		return ofNullable(nullableSupportText)
			.filter(StringUtils::isNotBlank)
			.map(supportText -> String.format(
				supportText,
				greetingFirstName,
				caseType.getDisplayName(),
				errandEntity.getErrandNumber(),
				TYPE_REPORTER_SUPPORT_TEXT == supportTextType ? messagingSettings.getKatlaUrl() : messagingSettings.getContactInformationUrl(),
				errandEntity.getErrandNumber()))
			.orElse("");
	}

	/**
	 * Parses the provided value into a UUID. Returns null instead of throwing if the value is not a valid UUID (e.g. an
	 * organisationsnummer, personnummer or any other arbitrary string), so that a non-UUID personId does not abort the
	 * notification flow.
	 *
	 * @param  value the value to parse
	 * @return       the parsed UUID, or null if the value is not a valid UUID
	 */
	static UUID toUuidOrNull(final String value) {
		try {
			return UUID.fromString(value);
		} catch (final Exception e) {
			return null;
		}
	}

	public static String findStakeholderEmail(final StakeholderEntity stakeholderEntity) {
		if (isNull(stakeholderEntity)) {
			return null;
		}

		return ofNullable(stakeholderEntity.getContactInformation())
			.orElse(emptyList())
			.stream()
			.filter(contactInformation -> Objects.equals(EMAIL, contactInformation.getContactType()))
			.findFirst()
			.map(ContactInformationEntity::getValue)
			.orElse(null);
	}

	/**
	 * Resolves the distinct, non-blank e-mail addresses for a list of stakeholders, so that a single combined message
	 * can be sent to all of them via Messaging's recipients array instead of one message per stakeholder.
	 *
	 * @param  stakeholderEntities the stakeholders to resolve e-mail addresses for
	 * @return                     the distinct, non-blank e-mail addresses found among the stakeholders
	 */
	public static List<String> findStakeholderEmails(final List<StakeholderEntity> stakeholderEntities) {
		return ofNullable(stakeholderEntities)
			.orElse(emptyList())
			.stream()
			.map(MessagingMapper::findStakeholderEmail)
			.filter(StringUtils::isNotBlank)
			.distinct()
			.toList();
	}

	/**
	 * Resolves the first name of the first stakeholder in the list that has a resolvable e-mail address, so that a
	 * greeting used in a combined batch email correlates with one of the stakeholders it is actually being sent to.
	 *
	 * @param  stakeholderEntities the stakeholders to resolve a first name from
	 * @return                     the first name of the first stakeholder with a resolvable e-mail address, or null
	 */
	public static String findFirstRecipientFirstName(final List<StakeholderEntity> stakeholderEntities) {
		return ofNullable(stakeholderEntities)
			.orElse(emptyList())
			.stream()
			.filter(stakeholder -> StringUtils.isNotBlank(findStakeholderEmail(stakeholder)))
			.findFirst()
			.map(StakeholderEntity::getFirstName)
			.orElse(null);
	}

	public static String toFilterString(final String namespace, final String departmentName) {
		return FILTER_TEMPLATE.formatted(namespace, departmentName);
	}
}
