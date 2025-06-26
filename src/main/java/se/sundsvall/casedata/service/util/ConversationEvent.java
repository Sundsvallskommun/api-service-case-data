package se.sundsvall.casedata.service.util;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import se.sundsvall.casedata.integration.db.model.ConversationEntity;

@Data
@Builder(setterPrefix = "with")
@NoArgsConstructor
@AllArgsConstructor
public class ConversationEvent {
	private String requestId;
	private ConversationEntity conversationEntity;
}
