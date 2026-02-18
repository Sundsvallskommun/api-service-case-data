package se.sundsvall.casedata.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import se.sundsvall.dept44.common.validators.annotation.ValidUuid;

import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(setterPrefix = "with")
public class PatchNotification {

	@ValidUuid
	@Schema(description = "Unique identifier for the notification", examples = "123e4567-e89b-12d3-a456-426614174000")
	private String id;

	@NotNull
	@Schema(description = "The Errand Id", examples = "123")
	private Long errandId;

	@Schema(description = "Owner id of the notification", examples = "AD01")
	private String ownerId;

	@Schema(description = "Type of the notification", examples = "CREATE")
	private String type;

	@Schema(description = "Description of the notification", examples = "Some description of the notification")
	private String description;

	@Schema(description = "Content of the notification", examples = "Some content of the notification")
	private String content;

	@DateTimeFormat(iso = DATE_TIME)
	@Schema(description = "Timestamp when the notification expires", examples = "2000-10-31T01:30:00.000+02:00")
	private OffsetDateTime expires;

	@Schema(description = "Acknowledged status of the notification", examples = "true")
	private Boolean acknowledged;

	@Schema(description = "Acknowledged status of the notification (global level). I.e. this notification is acknowledged by anyone.", examples = "true")
	private Boolean globalAcknowledged;
}
