package se.sundsvall.casedata.integration.db.model;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEqualsExcluding;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToStringExcluding;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.allOf;

import java.util.List;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

class ExtraParameterEntityTest {

	@Test
	void testBean() {
		MatcherAssert.assertThat(ExtraParameterEntity.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEqualsExcluding(),
			hasValidBeanToStringExcluding("errand")));
	}

	@Test
	void builderTest() {
		// Arrange
		var key = "key";
		var displayName = "displayName";
		var values = List.of("value1", "value2");

		// Act
		var entity = ExtraParameterEntity.builder()
			.withKey(key)
			.withDisplayName(displayName)
			.withValues(values)
			.build();

		// Assert
		assertThat(entity.getKey()).isEqualTo(key);
		assertThat(entity.getDisplayName()).isEqualTo(displayName);
		assertThat(entity.getValues()).isEqualTo(values);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(ExtraParameterEntity.builder().build()).hasAllNullFieldsOrProperties();
		assertThat(new ExtraParameterEntity()).hasAllNullFieldsOrProperties();
	}

}
