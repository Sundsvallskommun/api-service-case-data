package se.sundsvall.casedata.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.TimeZoneStorage;
import org.hibernate.annotations.TimeZoneStorageType;
import se.sundsvall.casedata.api.model.validation.ValidMapValueSize;
import se.sundsvall.casedata.api.model.validation.ValidStakeholderRole;
import se.sundsvall.casedata.integration.db.model.enums.StakeholderType;
import se.sundsvall.casedata.service.util.Constants;
import se.sundsvall.dept44.common.validators.annotation.ValidUuid;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder(setterPrefix = "with")
public class Stakeholder {

	@Schema(accessMode = Schema.AccessMode.READ_ONLY, description = "The unique identifier of the stakeholder", example = "1")
	private long id;

	@Schema(accessMode = Schema.AccessMode.READ_ONLY, description = "The version of the stakeholder", example = "1")
	private int version;

	@Schema(description = "The municipality ID", example = "2281", accessMode = Schema.AccessMode.READ_ONLY)
	@Size(max = 255)
	private String municipalityId;

	@Schema(description = "Namespace", example = "my.namespace", accessMode = Schema.AccessMode.READ_ONLY)
	@Size(max = 255)
	private String namespace;

	@NotNull
	@Schema(description = "The type of stakeholder", example = "PERSON")
	private StakeholderType type;

	@Size(max = 255)
	@Schema(description = "The first name of the stakeholder", example = "Test")
	private String firstName;

	@Size(max = 255)
	@Schema(description = "The last name of the stakeholder", example = "Testorsson")
	private String lastName;

	@ValidUuid(nullable = true)
	@Schema(description = "The person ID of the stakeholder", example = "3ed5bc30-6308-4fd5-a5a7-78d7f96f4438")
	private String personId;

	@Size(max = 255)
	@Schema(description = "The organization name of the stakeholder", example = "Sundsvalls testfabrik")
	private String organizationName;

	@Size(max = 13)
	@Pattern(regexp = Constants.ORGNR_PATTERN_REGEX, message = Constants.ORGNR_PATTERN_MESSAGE)
	@Schema(description = "Organization number with 10 or 12 digits.", example = "19901010-1234")
	private String organizationNumber;

	@Size(max = 255)
	@Schema(description = "The authorized signatory of the stakeholder", example = "Test Testorsson")
	private String authorizedSignatory;

	@Schema(description = "The AD-account of the stakeholder", example = "user")
	@Size(max = 36)
	private String adAccount;

	@NotNull
	@ValidStakeholderRole
	@Schema(description = "A stakeholder can have one or more roles.")
	private List<String> roles;

	@Valid
	@Schema(description = "A stakeholder may have one or more addresses. For example, one POSTAL_ADDRESS and another INVOICE_ADDRESS.")
	private List<Address> addresses;

	@Schema(description = "The contact information of the stakeholder")
	private List<ContactInformation> contactInformation;

	@ValidMapValueSize(max = 8192)
	@Schema(description = "Additional parameters for the stakeholder", example = "{\"key1\":\"value1\",\"key2\":\"value2\"}")
	@Builder.Default
	private Map<String, String> extraParameters = new HashMap<>();

	@Schema(accessMode = Schema.AccessMode.READ_ONLY, description = "The timestamp when the stakeholder was created", example = "2023-01-01T12:00:00Z")
	@TimeZoneStorage(TimeZoneStorageType.NORMALIZE)
	private OffsetDateTime created;

	@Schema(accessMode = Schema.AccessMode.READ_ONLY, description = "The timestamp when the stakeholder was last updated", example = "2023-01-02T12:00:00Z")
	@TimeZoneStorage(TimeZoneStorageType.NORMALIZE)
	private OffsetDateTime updated;

}
