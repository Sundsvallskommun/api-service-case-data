package se.sundsvall.casedata.api.model;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static com.google.code.beanmatchers.BeanMatchers.registerValueGenerator;
import static java.time.OffsetDateTime.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.allOf;
import static se.sundsvall.casedata.integration.db.model.enums.AppealStatus.NEW;
import static se.sundsvall.casedata.integration.db.model.enums.TimelinessReview.NOT_CONDUCTED;

import java.time.OffsetDateTime;
import java.util.Random;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class AppealTest {

	@BeforeAll
	static void setup() {
		registerValueGenerator(() -> now().plusDays(new Random().nextInt()), OffsetDateTime.class);
	}

	@Test
	void testBean() {
		MatcherAssert.assertThat(Appeal.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void builder() {

		// Arrange
		final var id = 1L;
		final var description = "description";
		final var registeredAt = now();
		final var appealConcernCommunicatedAt = now();
		final var status = NEW.name();
		final var decisionId = 1L;
		final var timelinessReview = NOT_CONDUCTED.name();
		final var created = now();
		final var updated = now();

		// Act
		var result = Appeal.builder()
			.withId(id)
			.withDescription(description)
			.withRegisteredAt(registeredAt)
			.withAppealConcernCommunicatedAt(appealConcernCommunicatedAt)
			.withStatus(status)
			.withTimelinessReview(timelinessReview)
			.withDecisionId(decisionId)
			.withCreated(created)
			.withUpdated(updated)
			.build();

		// Assert
		assertThat(result).hasNoNullFieldsOrProperties();
		assertThat(result.getId()).isEqualTo(id);
		assertThat(result.getDescription()).isEqualTo(description);
		assertThat(result.getRegisteredAt()).isEqualTo(registeredAt);
		assertThat(result.getAppealConcernCommunicatedAt()).isEqualTo(appealConcernCommunicatedAt);
		assertThat(result.getStatus()).isEqualTo(status);
		assertThat(result.getDecisionId()).isEqualTo(decisionId);
		assertThat(result.getTimelinessReview()).isEqualTo(timelinessReview);
		assertThat(result.getCreated()).isEqualTo(created);
		assertThat(result.getUpdated()).isEqualTo(updated);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(Appeal.builder().build()).hasAllNullFieldsOrPropertiesExcept("id", "version", "status", "timelinessReview")
			.satisfies(bean -> {
				assertThat(bean.getId()).isZero();
				assertThat(bean.getVersion()).isZero();
				assertThat(bean.getStatus()).isEqualTo(NEW.name());
				assertThat(bean.getTimelinessReview()).isEqualTo(NOT_CONDUCTED.name());
			});
		assertThat(new Appeal()).hasAllNullFieldsOrPropertiesExcept("id", "version", "status", "timelinessReview")
			.satisfies(bean -> {
				assertThat(bean.getId()).isZero();
				assertThat(bean.getVersion()).isZero();
				assertThat(bean.getStatus()).isEqualTo(NEW.name());
				assertThat(bean.getTimelinessReview()).isEqualTo(NOT_CONDUCTED.name());
			});
	}

}
