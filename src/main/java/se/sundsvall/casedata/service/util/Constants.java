package se.sundsvall.casedata.service.util;

public final class Constants {

	public static final String ERRAND_WAS_NOT_FOUND = "Errand with id: %s was not found";

	public static final String NAMESPACE_REGEXP = "[\\w|\\.|\\-]+";
	public static final String NAMESPACE_VALIDATION_MESSAGE = "can only contain A-Z, a-z, 0-9, -, _ and .";

	public static final String PROCESS_ENGINE_PROBLEM_DETAIL = "Unexpected response from ProcessEngine API.";

	public static final String ORGNR_PATTERN_REGEX = "^((18|19|20|21)\\d{6}|\\d{6})-(\\d{4})$";
	public static final String ORGNR_PATTERN_MESSAGE = "organizationNumber must consist of 10 or 12 digits. 10 digit orgnr must follow this format: \"XXXXXX-XXXX\". 12 digit orgnr must follow this format: \"(18|19|20)XXXXXX-XXXX\".";

	public static final String UNKNOWN = "UNKNOWN";
	public static final String PERMIT_NUMBER_EXTRA_PARAMETER_KEY = "artefact.permit.number";
	public static final String PERMIT_STATUS_EXTRA_PARAMETER_KEY = "artefact.permit.status";
	public static final String AD_USER_HEADER_KEY = "sentbyuser";
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
