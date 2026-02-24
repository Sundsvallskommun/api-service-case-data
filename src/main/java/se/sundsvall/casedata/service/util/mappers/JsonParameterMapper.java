package se.sundsvall.casedata.service.util.mappers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sundsvall.casedata.api.model.JsonParameter;
import se.sundsvall.casedata.integration.db.model.ErrandEntity;
import se.sundsvall.casedata.integration.db.model.JsonParameterEntity;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toCollection;

public final class JsonParameterMapper {

	private static final Logger LOG = LoggerFactory.getLogger(JsonParameterMapper.class);
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	private JsonParameterMapper() {
		// Intentionally empty
	}

	public static List<JsonParameterEntity> toJsonParameterEntityList(final List<JsonParameter> parameters, final ErrandEntity entity) {
		return ofNullable(parameters).orElse(emptyList()).stream()
			.map(parameter -> toJsonParameterEntity(parameter, entity))
			.collect(toCollection(ArrayList::new));
	}

	public static JsonParameterEntity toJsonParameterEntity(final JsonParameter parameter, final ErrandEntity errandEntity) {
		return JsonParameterEntity.builder()
			.withErrand(errandEntity)
			.withKey(parameter.getKey())
			.withSchemaId(parameter.getSchemaId())
			.withValue(toJsonString(parameter.getValue()))
			.build();
	}

	public static JsonParameter toJsonParameter(final JsonParameterEntity entity) {
		return JsonParameter.builder()
			.withKey(entity.getKey())
			.withSchemaId(entity.getSchemaId())
			.withValue(toJsonNode(entity.getValue()))
			.build();
	}

	public static List<JsonParameter> toJsonParameterList(final List<JsonParameterEntity> entities) {
		return ofNullable(entities).orElse(emptyList()).stream()
			.map(JsonParameterMapper::toJsonParameter)
			.toList();
	}

	private static String toJsonString(final JsonNode jsonNode) {
		if (jsonNode == null) {
			return null;
		}
		try {
			return OBJECT_MAPPER.writeValueAsString(jsonNode);
		} catch (final JsonProcessingException e) {
			LOG.warn("Failed to convert JsonNode to String", e);
			return null;
		}
	}

	private static JsonNode toJsonNode(final String value) {
		if (value == null) {
			return null;
		}
		try {
			return OBJECT_MAPPER.readTree(value);
		} catch (final JsonProcessingException e) {
			LOG.warn("Failed to convert String to JsonNode", e);
			return null;
		}
	}
}
