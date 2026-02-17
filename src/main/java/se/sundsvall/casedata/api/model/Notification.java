package se.sundsvall.casedata.api.model;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(setterPrefix = "with")
public class Notification {

	@Schema(description = "Unique identifier for the notification", examples = "123e4567-e89b-12d3-a456-426614174000", accessMode = READ_ONLY)
	private String id;

	@Schema(description = "The municipality ID", examples = "2281", accessMode = READ_ONLY)
	private String municipalityId;

	@Schema(description = "Namespace", examples = "MY_NAMESPACE", accessMode = READ_ONLY)
	private String namespace;

	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
	@Schema(description = "Timestamp when the notification was created", examples = "2000-10-31T01:30:00.000+02:00", accessMode = READ_ONLY)
	private OffsetDateTime created;

	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
	@Schema(description = "Timestamp when the notification was last modified", examples = "2000-10-31T01:30:00.000+02:00", accessMode = READ_ONLY)
	private OffsetDateTime modified;

	@Schema(description = "Name of the owner of the notification", examples = "Test Testorsson", accessMode = READ_ONLY)
	private String ownerFullName;

	@NotBlank
	@Schema(description = "Owner id of the notification", examples = "AD01")
	private String ownerId;

	@Schema(description = "User who created the notification", examples = "TestUser", accessMode = READ_ONLY)
	private String createdBy;

	@Schema(description = "Full name of the user who created the notification", examples = "Test Testorsson", accessMode = READ_ONLY)
	private String createdByFullName;

	@NotBlank
	@Schema(description = "Type of the notification", examples = "CREATE")
	private String type;

	@Schema(description = "Sub type of the notification", examples = "PHASE_CHANGE", accessMode = READ_ONLY)
	private String subType;

	@NotBlank
	@Schema(description = "Description of the notification", examples = "Some description of the notification")
	private String description;

	@Schema(description = "Content of the notification", examples = "Some content of the notification")
	private String content;

	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
	@Schema(description = "Timestamp when the notification expires", examples = "2000-10-31T01:30:00.000+02:00")
	private OffsetDateTime expires;

	@Schema(description = "Acknowledged status of the notification", examples = "true")
	private boolean acknowledged;

	@Schema(description = "Acknowledged status of the notification (global level). I.e. this notification is acknowledged by anyone.", examples = "true")
	private boolean globalAcknowledged;

	@Schema(description = "Errand id of the notification", examples = "1234", accessMode = READ_ONLY)
	private Long errandId;

	@Schema(description = "Errand number of the notification", examples = "SGP-2022-000001", accessMode = READ_ONLY)
	private String errandNumber;

}
