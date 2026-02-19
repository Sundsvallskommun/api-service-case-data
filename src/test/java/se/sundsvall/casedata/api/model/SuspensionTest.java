package se.sundsvall.casedata.api.model;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Random;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static com.google.code.beanmatchers.BeanMatchers.registerValueGenerator;
import static java.time.OffsetDateTime.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.allOf;

class SuspensionTest {

	@BeforeAll
	static void setup() {
		registerValueGenerator(() -> now().plusDays(new Random().nextInt()), OffsetDateTime.class);
	}

	@Test
	void testBean() {
		MatcherAssert.assertThat(Suspension.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void builder() {

		// Arrange
		var suspensionStart = OffsetDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.ofHours(1));
		var suspensionEnd = OffsetDateTime.of(2024, 1, 1, 14, 0, 0, 0, ZoneOffset.ofHours(1));

		// Act
		var result = Suspension.builder()
			.withSuspendedFrom(suspensionStart)
			.withSuspendedTo(suspensionEnd)
			.build();

		// Assert
		assertThat(result).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(result.getSuspendedTo()).isEqualTo(suspensionEnd);
		assertThat(result.getSuspendedFrom()).isEqualTo(suspensionStart);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(Suspension.builder().build()).hasAllNullFieldsOrProperties();
		assertThat(new Suspension()).hasAllNullFieldsOrProperties();
	}

}
