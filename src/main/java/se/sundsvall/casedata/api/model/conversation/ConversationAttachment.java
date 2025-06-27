package se.sundsvall.casedata.api.model.conversation;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(setterPrefix = "with")
@Schema(description = "ConversationAttachment model")
public class ConversationAttachment {

	@Schema(description = "ConversationAttachment ID", example = "cb20c51f-fcf3-42c0-b613-de563634a8ec")
	private String id;

	@Schema(description = "Name of the file", example = "my-file.txt")
	private String fileName;

	@Schema(description = "Size of the file in bytes", example = "1024")
	private int fileSize;

	@Schema(description = "Mime type of the file")
	private String mimeType;

	@Schema(description = "The attachment created date", example = "2023-01-01T00:00:00+01:00")
	private OffsetDateTime created;

}
