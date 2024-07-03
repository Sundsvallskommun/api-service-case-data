package se.sundsvall.casedata.api.model;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

class CoordinatesDTOTest {

	@Test
	void testBean() {
		MatcherAssert.assertThat(CoordinatesDTO.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}
	
	@Test
	void builderTest() {
		final var latitude = 59.3293;
		final var longitude = 18.0686;

		final var bean = CoordinatesDTO.builder()
			.withLatitude(latitude)
			.withLongitude(longitude)
			.build();

		assertThat(bean.getLatitude()).isEqualTo(latitude);
		assertThat(bean.getLongitude()).isEqualTo(longitude);
	}

	@Test
	void testNoDirtOnEmptyBean() {
		assertThat(CoordinatesDTO.builder().build()).hasAllNullFieldsOrProperties();
		assertThat(new CoordinatesDTO()).hasAllNullFieldsOrProperties();
	}
}
