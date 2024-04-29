package se.sundsvall.casedata.api.model;

import java.util.HashMap;
import java.util.Map;

import se.sundsvall.casedata.api.model.validation.ValidMapValueSize;

import lombok.Data;

@Data
public class ExtraParameterDTO {

	@ValidMapValueSize(max = 8192)
	private Map<String, String> extraParameters = new HashMap<>();

}
