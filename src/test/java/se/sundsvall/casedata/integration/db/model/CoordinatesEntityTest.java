package se.sundsvall.casedata.integration.db.model;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

class CoordinatesEntityTest {

	@Test
	void testBean() {
		MatcherAssert.assertThat(CoordinatesEntity.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void builder() {
		// Arrange
		var latitude = 1.0;
		var longitude = 2.0;

		// Act
		var bean = CoordinatesEntity.builder()
			.withLatitude(latitude)
			.withLongitude(longitude)
			.build();

		// Assert
		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.getLatitude()).isEqualTo(latitude);
		assertThat(bean.getLongitude()).isEqualTo(longitude);
	}


	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(CoordinatesEntity.builder().build()).hasAllNullFieldsOrProperties();
		assertThat(new CoordinatesEntity()).hasAllNullFieldsOrProperties();
	}

}
