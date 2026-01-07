package se.sundsvall.casedata.service.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(setterPrefix = "with")
public class MessagingSettings {
	private String ownerSupportText;
	private String reporterSupportText;
	private String contactInformationUrl;
	private String katlaUrl;
	private String smsSender;
	private String contactInformationEmail;
	private String contactInformationEmailName;
}
