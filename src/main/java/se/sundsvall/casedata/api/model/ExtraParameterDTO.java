package se.sundsvall.casedata.api.model;

import java.util.HashMap;
import java.util.Map;

import lombok.Data;

@Data
public class ExtraParameterDTO {

	private Map<String, String> extraParameters = new HashMap<>();

}
