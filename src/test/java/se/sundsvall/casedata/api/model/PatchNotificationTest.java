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
		final var owner = "Test Testorsson";
		final var ownerId = "cb20c51f-fcf3-42c0-b613-de563634a8ec";
		final var type = "SomeType";
		final var description = "Some description of the notification";
		final var content = "Some content of the notification";
		final var expires = now();
		final var acknowledged = true;

		// Act
		final var bean = PatchNotification.builder()
			.withOwnerFullName(owner)
			.withOwnerId(ownerId)
			.withType(type)
			.withDescription(description)
			.withContent(content)
			.withExpires(expires)
			.withAcknowledged(acknowledged)
			.build();

		// Assert
		assertThat(bean.getOwnerFullName()).isEqualTo(owner);
		assertThat(bean.getOwnerId()).isEqualTo(ownerId);
		assertThat(bean.getType()).isEqualTo(type);
		assertThat(bean.getDescription()).isEqualTo(description);
		assertThat(bean.getContent()).isEqualTo(content);
		assertThat(bean.getExpires()).isEqualTo(expires);
		assertThat(bean.getAcknowledged()).isEqualTo(acknowledged);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(Notification.builder().build()).hasAllNullFieldsOrPropertiesExcept("acknowledged");
		assertThat(new Notification()).hasAllNullFieldsOrPropertiesExcept("acknowledged");
	}

}
