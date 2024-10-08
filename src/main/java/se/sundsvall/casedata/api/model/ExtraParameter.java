package se.sundsvall.casedata.api.model;

import java.util.HashMap;
import java.util.Map;

import se.sundsvall.casedata.api.model.validation.ValidMapValueSize;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ExtraParameter {

	@ValidMapValueSize(max = 8192)
	@Schema(description = "Extra parameters", example = "{\"key1\":\"value1\",\"key2\":\"value2\"}")
	private Map<String, String> extraParameters = new HashMap<>();

}
