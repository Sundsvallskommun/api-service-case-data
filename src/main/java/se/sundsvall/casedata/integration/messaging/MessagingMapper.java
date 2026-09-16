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
import java.util.HashSet;
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
	 * Builds one batch email request per recipient for a templated notification, each carrying a single party and a
	 * message body personalized with that recipient's own first name. Messaging's {@code /email/batch} endpoint sends
	 * the exact same subject/message to every party in a single call - it has no per-party template variables - so a
	 * personalized greeting can only be achieved by sending one call per recipient, the same reasoning that gives
	 * {@link #toMessagingMessageRequest} one {@code Message} per recipient. Recipients sharing an e-mail address are
	 * deduplicated, keeping the first occurrence; recipients without a resolvable e-mail address are skipped.
	 *
	 * @param  errandEntity      the errand the notification concerns
	 * @param  messagingSettings the messaging settings to resolve support text, sender address, etc. from
	 * @param  recipients        the stakeholders to send the notification to
	 * @param  supportTextType   which support text (owner or reporter) to build the message from
	 * @param  caseType          the case type of the errand
	 * @return                   one batch email request per distinct recipient e-mail address
	 */
	public static List<EmailBatchRequest> toEmailBatchRequests(final ErrandEntity errandEntity, final MessagingSettings messagingSettings, final List<StakeholderEntity> recipients, int supportTextType, CaseType caseType) {
		final var seenEmails = new HashSet<String>();
		return recipients.stream()
			.filter(recipient -> StringUtils.isNotBlank(findStakeholderEmail(recipient)))
			.filter(recipient -> seenEmails.add(findStakeholderEmail(recipient)))
			.map(recipient -> new EmailBatchRequest()
				.parties(List.of(new Party(findStakeholderEmail(recipient))))
				.subject(SUBJECT_TEMPLATE.formatted(caseType.getDisplayName(), errandEntity.getErrandNumber()))
				.message(createBody(errandEntity, messagingSettings, supportTextType, caseType, recipient.getFirstName()))
				.sender(new EmailSender()
					.name(messagingSettings.getContactInformationEmailName())
					.address(messagingSettings.getContactInformationEmail())))
			.toList();
	}

	/**
	 * Builds a batch email request for a manually composed message, sent to every recipient in the request as an
	 * individual email via Messaging's batch endpoint. Unlike {@link #toEmailBatchRequests}, the subject and message
	 * are taken verbatim from the caller instead of being built from a templated support text, so there is no
	 * per-recipient personalization to lose by sending all recipients in a single call. Recipients are deduplicated,
	 * so a caller-supplied duplicate address does not result in the same email being sent twice.
	 *
	 * @param  request           the bulk email request containing recipients, subject, message and attachments
	 * @param  messagingSettings the messaging settings to resolve the sender address from
	 * @param  attachments       the attachments to include on the email
	 * @return                   the resulting batch email request
	 */
	public static EmailBatchRequest toEmailBatchRequest(final BulkEmailRequest request, final MessagingSettings messagingSettings, final List<EmailAttachment> attachments) {
		return new EmailBatchRequest()
			.parties(request.getRecipients().stream()
				.distinct()
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
	 * meant to be notified (as determined by the caller) receive a message. {@code MessageParty.partyId} is required
	 * by the Messaging contract, so recipients whose {@code personId} cannot be parsed as a UUID (e.g. an
	 * organisationsnummer or personnummer) are skipped entirely rather than sent with a null partyId, which would fail
	 * the whole request.
	 *
	 * @param  errandEntity      the errand the notification concerns
	 * @param  messagingSettings the messaging settings to resolve support text, sender address, etc. from
	 * @param  recipients        the stakeholders to send a message to
	 * @param  caseType          the case type of the errand
	 * @return                   the resulting message request, containing one message per recipient with a valid partyId
	 */
	public static MessageRequest toMessagingMessageRequest(final ErrandEntity errandEntity, final MessagingSettings messagingSettings, final List<StakeholderEntity> recipients, CaseType caseType) {
		return new MessageRequest()
			.messages(recipients.stream()
				.filter(recipient -> toUuidOrNull(recipient.getPersonId()) != null)
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
		String nullableSupportText;
		String url;
		if (TYPE_REPORTER_SUPPORT_TEXT == supportTextType) {
			nullableSupportText = messagingSettings.getReporterSupportText();
			url = messagingSettings.getKatlaUrl();
		} else {
			nullableSupportText = messagingSettings.getOwnerSupportText();
			url = messagingSettings.getContactInformationUrl();
		}

		return ofNullable(nullableSupportText)
			.filter(StringUtils::isNotBlank)
			.map(supportText -> String.format(
				supportText,
				greetingFirstName,
				caseType.getDisplayName(),
				errandEntity.getErrandNumber(),
				url,
				errandEntity.getErrandNumber()))
			.orElse("");
	}

	/**
	 * Parses the provided value into a UUID. Returns null instead of throwing if the value is not a valid UUID (e.g. an
	 * organisationsnummer, personnummer or any other arbitrary string), so that a caller can filter out recipients with
	 * a non-UUID personId instead of having the parse failure abort the notification flow.
	 *
	 * @param  value the value to parse
	 * @return       the parsed UUID, or null if the value is not a valid UUID
	 */
	public static UUID toUuidOrNull(final String value) {
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

	public static String toFilterString(final String namespace, final String departmentName) {
		return FILTER_TEMPLATE.formatted(namespace, departmentName);
	}
}
