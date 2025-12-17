package se.sundsvall.casedata.service.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(setterPrefix = "with")
public class MessagingSettings {
	private String supportText;
	private String contactInformationUrl;
	private String smsSender;
	private String contactInformationEmail;
	private String contactInformationEmailName;
}
