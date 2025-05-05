package se.sundsvall.casedata.service.util;

public final class Constants {
	public static final String ERRAND_ENTITY_NOT_FOUND = "Errand with id:'%s' not found in namespace:'%s' for municipality with id:'%s'";

	public static final String ATTACHMENT_ENTITY_NOT_FOUND = "Attachment with id:'%s' not found on errand with id:'%s' in namespace:'%s' for municipality with id:'%s'";

	public static final String MESSAGE_ENTITY_NOT_FOUND = "Message with id:'%s' not found in namespace:'%s' for municipality with id:'%s'";
	public static final String MESSAGE_ATTACHMENT_ENTITY_NOT_FOUND = "MessageAttachment with id:'%s' not found in namespace:'%s' for municipality with id:'%s'";

	public static final String NOTIFICATION_ENTITY_NOT_FOUND = "Notification with id:'%s' not found in namespace:'%s' for municipality with id:'%s' and errand with id:'%s'";
	public static final String NOTE_WITH_ID_X_WAS_NOT_FOUND_ON_ERRAND_WITH_ID_X = "Note with id:'%s' was not found on errand with id:'%s'";
	public static final String STAKEHOLDER_WITH_ID_X_WAS_NOT_FOUND_ON_ERRAND_WITH_ID_X = "Stakeholder with id:'%s' was not found on errand with id:'%s'";

	public static final String NAMESPACE_REGEXP = "[\\w|\\-]+";
	public static final String NAMESPACE_VALIDATION_MESSAGE = "can only contain A-Z, a-z, 0-9, - and _";

	public static final String PROCESS_ENGINE_PROBLEM_DETAIL = "Unexpected response from ProcessEngine API.";

	public static final String ORGNR_PATTERN_REGEX = "^((18|19|20|21)\\d{6}|\\d{6})-(\\d{4})$";
	public static final String ORGNR_PATTERN_MESSAGE = "organizationNumber must consist of 10 or 12 digits. 10 digit orgnr must follow this format: \"XXXXXX-XXXX\". 12 digit orgnr must follow this format: \"(18|19|20)XXXXXX-XXXX\".";

	public static final String UNKNOWN = "UNKNOWN";
	public static final String PERMIT_NUMBER_EXTRA_PARAMETER_KEY = "artefact.permit.number";
	public static final String PERMIT_STATUS_EXTRA_PARAMETER_KEY = "artefact.permit.status";
	public static final String X_JWT_ASSERTION_HEADER_KEY = "x-jwt-assertion";
	public static final String CAMUNDA_USER = "WSO2_Camunda";

	public static final String NOTIFICATION_ERRAND_CREATED = "Ärende skapat";
	public static final String NOTIFICATION_ERRAND_UPDATED = "Ärende uppdaterat";
	public static final String NOTIFICATION_NOTE_CREATED = "Notering skapad";
	public static final String NOTIFICATION_NOTE_UPDATED = "Notering uppdaterad";
	public static final String NOTIFICATION_DECISION_CREATED = "Beslut skapat";
	public static final String NOTIFICATION_DECISION_UPDATED = "Beslut uppdaterat";

	private Constants() {
		// Prevent instantiation
	}

}
