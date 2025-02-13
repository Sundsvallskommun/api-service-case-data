package se.sundsvall.casedata.integration.db.model;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static com.google.code.beanmatchers.BeanMatchers.registerValueGenerator;
import static java.time.OffsetDateTime.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;

import java.time.OffsetDateTime;
import java.util.Random;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class StatusEntityTest {

	@BeforeAll
	static void setup() {
		registerValueGenerator(() -> now().plusDays(new Random().nextInt()), OffsetDateTime.class);
	}

	@Test
	void testBean() {
		MatcherAssert.assertThat(StatusEntity.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void builder() {
		// Arrange
		final var statusType = "statusType";
		final var description = "description";
		final var dateTime = now().plusDays(new Random().nextInt());

		// Act
		final var bean = StatusEntity.builder()
			.withStatusType(statusType)
			.withDescription(description)
			.withCreated(dateTime)
			.build();

		// Assert
		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.getStatusType()).isEqualTo(statusType);
		assertThat(bean.getDescription()).isEqualTo(description);
		assertThat(bean.getCreated()).isEqualTo(dateTime);

	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(StatusEntity.builder().build()).hasAllNullFieldsOrProperties();
		assertThat(new StatusEntity()).hasAllNullFieldsOrProperties();
	}

}
