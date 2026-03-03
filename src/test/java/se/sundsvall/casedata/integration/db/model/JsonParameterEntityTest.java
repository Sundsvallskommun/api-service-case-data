package se.sundsvall.casedata.integration.db.model;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEqualsExcluding;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCodeExcluding;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToStringExcluding;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.allOf;

class JsonParameterEntityTest {

	@Test
	void testBean() {
		MatcherAssert.assertThat(JsonParameterEntity.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCodeExcluding("errand"),
			hasValidBeanEqualsExcluding("errand"),
			hasValidBeanToStringExcluding("errand")));
	}

	@Test
	void builderTest() {
		// Arrange
		var key = "key";
		var schemaId = "schemaId";
		var value = "{\"test\": true}";

		// Act
		var entity = JsonParameterEntity.builder()
			.withKey(key)
			.withSchemaId(schemaId)
			.withValue(value)
			.build();

		// Assert
		assertThat(entity.getKey()).isEqualTo(key);
		assertThat(entity.getSchemaId()).isEqualTo(schemaId);
		assertThat(entity.getValue()).isEqualTo(value);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(JsonParameterEntity.builder().build()).hasAllNullFieldsOrProperties();
		assertThat(new JsonParameterEntity()).hasAllNullFieldsOrProperties();
	}

}
