package se.sundsvall.casedata.api.model;

import java.util.HashMap;
import java.util.Map;

import se.sundsvall.casedata.api.model.validation.ValidAttachmentCategory;
import se.sundsvall.casedata.api.model.validation.ValidMapValueSize;

import io.swagger.v3.oas.annotations.media.Schema;
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

	@ValidAttachmentCategory
	private String category;

	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	private String municipalityId;

	private String name;

	private String note;

	private String extension;

	private String mimeType;

	private String file;

	private String errandNumber;

	@Builder.Default
	@ValidMapValueSize(max = 8192)
	private Map<String, String> extraParameters = new HashMap<>();

}
