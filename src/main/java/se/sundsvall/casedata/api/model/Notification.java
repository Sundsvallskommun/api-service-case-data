package se.sundsvall.casedata.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.OffsetDateTime;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Builder(setterPrefix = "with")
public class Notification {

	@Schema(description = "Unique identifier for the notification", example = "123e4567-e89b-12d3-a456-426614174000", accessMode = READ_ONLY)
	private String id;

	@Schema(description = "The municipality ID", example = "2281", accessMode = Schema.AccessMode.READ_ONLY)
	@Size(max = 255)
	private String municipalityId;

	@Schema(description = "Namespace", example = "my.namespace", accessMode = Schema.AccessMode.READ_ONLY)
	@Size(max = 255)
	private String namespace;

	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
	@Schema(description = "Timestamp when the notification was created", example = "2000-10-31T01:30:00.000+02:00", accessMode = READ_ONLY)
	private OffsetDateTime created;

	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
	@Schema(description = "Timestamp when the notification was last modified", example = "2000-10-31T01:30:00.000+02:00", accessMode = READ_ONLY)
	private OffsetDateTime modified;

	@Schema(description = "Name of the owner of the notification", example = "Test Testorsson", accessMode = READ_ONLY)
	private String ownerFullName;

	@NotBlank
	@Schema(description = "Owner id of the notification", example = "AD01")
	private String ownerId;

	@Schema(description = "User who created the notification", example = "TestUser")
	private String createdBy;

	@Schema(description = "Full name of the user who created the notification", example = "Test Testorsson", accessMode = READ_ONLY)
	private String createdByFullName;

	@NotBlank
	@Schema(description = "Type of the notification", example = "CREATE")
	private String type;

	@NotBlank
	@Schema(description = "Description of the notification", example = "Some description of the notification")
	private String description;

	@Schema(description = "Content of the notification", example = "Some content of the notification")
	private String content;

	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
	@Schema(description = "Timestamp when the notification expires", example = "2000-10-31T01:30:00.000+02:00")
	private OffsetDateTime expires;

	@Schema(description = "Acknowledged status of the notification", example = "true")
	private boolean acknowledged;

	@NotNull
	@Schema(description = "Errand id of the notification", example = "1234")
	private Long errandId;

	@Schema(description = "Errand number of the notification", example = "PRH-2022-000001", accessMode = READ_ONLY)
	private String errandNumber;

}
