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

class NotificationTest {

	@BeforeAll
	static void setup() {
		registerValueGenerator(() -> now().plusDays(new Random().nextInt()), OffsetDateTime.class);
	}

	@Test
	void testBean() {
		MatcherAssert.assertThat(Notification.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testBuilderMethods() {

		// Arrange
		final var id = "123e4567-e89b-12d3-a456-426614174000";
		final var created = now();
		final var modified = now();
		final var owner = "Test Testorsson";
		final var ownerId = "cb20c51f-fcf3-42c0-b613-de563634a8ec";
		final var createdBy = "TestUser";
		final var createdByFullName = "Test Testorsson";
		final var type = "SomeType";
		final var description = "Some description of the notification";
		final var content = "Some content of the notification";
		final var expires = now();
		final var acknowledged = true;
		final var errandId = 12345L;
		final var errandNumber = "PRH-2022-000001";

		// Act
		final var bean = Notification.builder()
			.withId(id)
			.withCreated(created)
			.withModified(modified)
			.withOwnerFullName(owner)
			.withOwnerId(ownerId)
			.withCreatedBy(createdBy)
			.withCreatedByFullName(createdByFullName)
			.withType(type)
			.withDescription(description)
			.withContent(content)
			.withExpires(expires)
			.withAcknowledged(acknowledged)
			.withErrandId(errandId)
			.withErrandNumber(errandNumber)
			.build();

		// Assert
		assertThat(bean.getId()).isEqualTo(id);
		assertThat(bean.getCreated()).isEqualTo(created);
		assertThat(bean.getModified()).isEqualTo(modified);
		assertThat(bean.getOwnerFullName()).isEqualTo(owner);
		assertThat(bean.getOwnerId()).isEqualTo(ownerId);
		assertThat(bean.getCreatedBy()).isEqualTo(createdBy);
		assertThat(bean.getCreatedByFullName()).isEqualTo(createdByFullName);
		assertThat(bean.getType()).isEqualTo(type);
		assertThat(bean.getDescription()).isEqualTo(description);
		assertThat(bean.getContent()).isEqualTo(content);
		assertThat(bean.getExpires()).isEqualTo(expires);
		assertThat(bean.isAcknowledged()).isEqualTo(acknowledged);
		assertThat(bean.getErrandId()).isEqualTo(errandId);
		assertThat(bean.getErrandNumber()).isEqualTo(errandNumber);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(Notification.builder().build()).hasAllNullFieldsOrPropertiesExcept("acknowledged");
		assertThat(new Notification()).hasAllNullFieldsOrPropertiesExcept("acknowledged");
	}
}
