package se.sundsvall.casedata.service.util.mappers;

import java.util.List;
import org.junit.jupiter.api.Test;
import se.sundsvall.casedata.api.model.JsonParameter;
import se.sundsvall.casedata.integration.db.model.ErrandEntity;
import se.sundsvall.casedata.integration.db.model.JsonParameterEntity;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JsonParameterMapperTest {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	@Test
	void testToJsonParameterEntityList() {
		// Arrange
		final var errandEntity = new ErrandEntity();
		final var jsonNode = OBJECT_MAPPER.createObjectNode().put("firstName", "Joe");
		final var parameter = JsonParameter.builder()
			.withKey("formData1")
			.withSchemaId("2281_person_1.0")
			.withValue(jsonNode)
			.build();

		// Act
		final var result = JsonParameterMapper.toJsonParameterEntityList(List.of(parameter), errandEntity);

		// Assert
		assertThat(result).hasSize(1);
		assertThat(result.getFirst().getKey()).isEqualTo("formData1");
		assertThat(result.getFirst().getSchemaId()).isEqualTo("2281_person_1.0");
		assertThat(result.getFirst().getValue()).isEqualTo("{\"firstName\":\"Joe\"}");
		assertThat(result.getFirst().getErrand()).isEqualTo(errandEntity);
	}

	@Test
	void testToJsonParameterEntityListNullParameters() {
		// Arrange
		final var errandEntity = new ErrandEntity();

		// Act
		final var result = JsonParameterMapper.toJsonParameterEntityList(null, errandEntity);

		// Assert
		assertThat(result).isEmpty();
	}

	@Test
	void testToJsonParameterEntity() {
		// Arrange
		final var errandEntity = ErrandEntity.builder().build();
		final var jsonNode = OBJECT_MAPPER.createObjectNode().put("name", "test");
		final var parameter = JsonParameter.builder()
			.withKey("testKey")
			.withSchemaId("schemaId")
			.withValue(jsonNode)
			.build();

		// Act
		final var result = JsonParameterMapper.toJsonParameterEntity(parameter, errandEntity);

		// Assert
		assertThat(result).isNotNull();
		assertThat(result.getErrand()).isSameAs(errandEntity);
		assertThat(result.getKey()).isEqualTo("testKey");
		assertThat(result.getSchemaId()).isEqualTo("schemaId");
		assertThat(result.getValue()).isEqualTo("{\"name\":\"test\"}");
	}

	@Test
	void testToJsonParameter() {
		// Arrange
		final var entity = JsonParameterEntity.builder()
			.withKey("testKey")
			.withSchemaId("schemaId")
			.withValue("{\"name\":\"test\"}")
			.build();

		// Act
		final var result = JsonParameterMapper.toJsonParameter(entity);

		// Assert
		assertThat(result.getKey()).isEqualTo("testKey");
		assertThat(result.getSchemaId()).isEqualTo("schemaId");
		assertThat(result.getValue()).isInstanceOf(ObjectNode.class);
		assertThat(result.getValue().get("name").asString()).isEqualTo("test");
	}

	@Test
	void testToJsonParameterList() {
		// Arrange
		final var entity = JsonParameterEntity.builder()
			.withKey("testKey")
			.withSchemaId("schemaId")
			.withValue("{\"name\":\"test\"}")
			.build();

		// Act
		final var result = JsonParameterMapper.toJsonParameterList(List.of(entity));

		// Assert
		assertThat(result).hasSize(1);
		assertThat(result.getFirst().getKey()).isEqualTo("testKey");
		assertThat(result.getFirst().getSchemaId()).isEqualTo("schemaId");
	}

	@Test
	void testToJsonParameterListNullEntities() {
		// Act
		final var result = JsonParameterMapper.toJsonParameterList(null);

		// Assert
		assertThat(result).isEmpty();
	}

	@Test
	void testToJsonParameterEntityWithNullValue() {
		// Arrange
		final var errandEntity = new ErrandEntity();
		final var parameter = JsonParameter.builder()
			.withKey("testKey")
			.withValue(null)
			.build();

		// Act
		final var result = JsonParameterMapper.toJsonParameterEntity(parameter, errandEntity);

		// Assert
		assertThat(result.getValue()).isNull();
	}

	@Test
	void testToJsonParameterWithNullValue() {
		// Arrange
		final var entity = JsonParameterEntity.builder()
			.withKey("testKey")
			.withValue(null)
			.build();

		// Act
		final var result = JsonParameterMapper.toJsonParameter(entity);

		// Assert
		assertThat(result.getValue()).isNull();
	}

	@Test
	void testToJsonParameterWithInvalidJsonValue() {
		// Arrange
		final var entity = JsonParameterEntity.builder()
			.withKey("testKey")
			.withValue("not valid json{{{")
			.build();

		// Act & Assert
		assertThatThrownBy(() -> JsonParameterMapper.toJsonParameter(entity))
			.isInstanceOf(JacksonException.class);
	}
}
