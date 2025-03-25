package se.sundsvall.casedata.api.model;

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

class PatchNotificationTest {

	@BeforeAll
	static void setup() {
		registerValueGenerator(() -> now().plusDays(new Random().nextInt()), OffsetDateTime.class);
	}

	@Test
	void testBean() {
		MatcherAssert.assertThat(PatchNotification.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testBuilderMethods() {

		// Arrange
		final var ownerId = "cb20c51f-fcf3-42c0-b613-de563634a8ec";
		final var type = "SomeType";
		final var subType = "SomeSubType";
		final var description = "Some description of the notification";
		final var content = "Some content of the notification";
		final var expires = now();
		final var acknowledged = true;
		final var globalAcknowledged = true;

		// Act
		final var bean = PatchNotification.builder()
			.withOwnerId(ownerId)
			.withType(type)
			.withSubType(subType)
			.withDescription(description)
			.withContent(content)
			.withExpires(expires)
			.withAcknowledged(acknowledged)
			.withGlobalAcknowledged(globalAcknowledged)
			.build();

		// Assert
		assertThat(bean.getOwnerId()).isEqualTo(ownerId);
		assertThat(bean.getType()).isEqualTo(type);
		assertThat(bean.getSubType()).isEqualTo(subType);
		assertThat(bean.getDescription()).isEqualTo(description);
		assertThat(bean.getContent()).isEqualTo(content);
		assertThat(bean.getExpires()).isEqualTo(expires);
		assertThat(bean.getAcknowledged()).isEqualTo(acknowledged);
		assertThat(bean.getGlobalAcknowledged()).isEqualTo(globalAcknowledged);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(Notification.builder().build()).hasAllNullFieldsOrPropertiesExcept("acknowledged", "globalAcknowledged");
		assertThat(new Notification()).hasAllNullFieldsOrPropertiesExcept("acknowledged", "globalAcknowledged");
	}
}
