package se.sundsvall.casedata.integration.db.model.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(enumAsRef = true, description = "Message classification")
public enum Classification {
	INFORMATION,
	COMPLETION_REQUEST,
	OBTAIN_OPINION,
	INTERNAL_COMMUNICATION,
	OTHER
}
