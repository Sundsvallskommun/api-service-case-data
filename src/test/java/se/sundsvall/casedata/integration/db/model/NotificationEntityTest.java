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

class NotificationEntityTest {

	@BeforeAll
	static void setup() {
		registerValueGenerator(() -> now().plusDays(new Random().nextInt()), OffsetDateTime.class);
	}

	@Test
	void testBean() {
		MatcherAssert.assertThat(NotificationEntity.class, allOf(
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
		final var errandEntity = ErrandEntity.builder()
			.withId(12345L)
			.withErrandNumber("ERRAND-NUMBER").build();
		final var municipalityId = "municipalityId";
		final var namespace = "namespace";

		// Act
		final var notification = NotificationEntity.builder()
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
			.withErrand(errandEntity)
			.withMunicipalityId(municipalityId)
			.withNamespace(namespace)
			.build();

		// Assert
		assertThat(notification.getId()).isEqualTo(id);
		assertThat(notification.getCreated()).isEqualTo(created);
		assertThat(notification.getModified()).isEqualTo(modified);
		assertThat(notification.getOwnerFullName()).isEqualTo(owner);
		assertThat(notification.getOwnerId()).isEqualTo(ownerId);
		assertThat(notification.getCreatedBy()).isEqualTo(createdBy);
		assertThat(notification.getCreatedByFullName()).isEqualTo(createdByFullName);
		assertThat(notification.getType()).isEqualTo(type);
		assertThat(notification.getDescription()).isEqualTo(description);
		assertThat(notification.getContent()).isEqualTo(content);
		assertThat(notification.getExpires()).isEqualTo(expires);
		assertThat(notification.isAcknowledged()).isEqualTo(acknowledged);
		assertThat(notification.getErrand()).isEqualTo(errandEntity);
		assertThat(notification.getMunicipalityId()).isEqualTo(municipalityId);
		assertThat(notification.getNamespace()).isEqualTo(namespace);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(NotificationEntity.builder().build()).hasAllNullFieldsOrPropertiesExcept("acknowledged");
		assertThat(new NotificationEntity()).hasAllNullFieldsOrPropertiesExcept("acknowledged");
	}
}
