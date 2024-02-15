package se.sundsvall.casedata.api.model;

import java.util.HashMap;
import java.util.Map;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Size;

import se.sundsvall.casedata.integration.db.model.enums.AttachmentCategory;

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
public class AttachmentDTO extends BaseDTO {

	@Enumerated(EnumType.STRING)
	private AttachmentCategory category;

	@Size(max = 255)
	private String name;

	@Size(max = 1000)
	private String note;

	@Size(max = 255)
	private String extension;

	@Size(max = 255)
	private String mimeType;

	private String file;

	private String errandNumber;

	@Builder.Default
	private Map<String, String> extraParameters = new HashMap<>();

}
