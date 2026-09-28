package se.sundsvall.casedata.integration.db.model.enums;

/**
 * APPROVAL (bifall)
 * REJECTION (avslag)
 * DISMISSAL (avvisande)
 * CANCELLATION (avskrivande)
 * CONDITIONAL_APPROVAL (bifall med villkor)
 * REVOCATION (återkallelse av tidigare utfärdat tillstånd)
 */
public enum DecisionOutcome {
	APPROVAL, REJECTION, DISMISSAL, CANCELLATION, CONDITIONAL_APPROVAL, REVOCATION
}
