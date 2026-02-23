package se.sundsvall.casedata.api.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static com.google.code.beanmatchers.BeanMatchers.registerValueGenerator;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;

class JsonParameterTest {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	@BeforeAll
	static void setup() {
		registerValueGenerator(() -> OBJECT_MAPPER.createObjectNode().put("random", UUID.randomUUID().toString()), JsonNode.class);
	}

	@Test
	void testBean() {
		MatcherAssert.assertThat(JsonParameter.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void builder() {
		// Arrange
		final var key = "formData1";
		final var value = new ObjectMapper().createObjectNode().put("name", "test");
		final var schemaId = "2281_person_1.0";

		// Act
		final var result = JsonParameter.builder()
			.withKey(key)
			.withValue(value)
			.withSchemaId(schemaId)
			.build();

		// Assert
		assertThat(result).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(result.getKey()).isEqualTo(key);
		assertThat(result.getValue()).isEqualTo(value);
		assertThat(result.getSchemaId()).isEqualTo(schemaId);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(JsonParameter.builder().build()).hasAllNullFieldsOrProperties();
		assertThat(new JsonParameter()).hasAllNullFieldsOrProperties();
	}
}
