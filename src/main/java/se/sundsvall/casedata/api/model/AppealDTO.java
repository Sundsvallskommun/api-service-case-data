package se.sundsvall.casedata.api.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(setterPrefix = "with")
public class AppealDTO extends BaseDTO {

	private StakeholderDTO appealedBy;

	private StakeholderDTO judicialAuthorisation;

	@Builder.Default
	private List<AttachmentDTO> attachments = new ArrayList<>();

	@Builder.Default
	private Map<String, String> extraParameters = new HashMap<>();

}
