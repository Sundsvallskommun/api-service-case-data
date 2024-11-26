package se.sundsvall.casedata.integration.db.model.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(enumAsRef = true)
public enum NoteType {
	INTERNAL, PUBLIC
}
