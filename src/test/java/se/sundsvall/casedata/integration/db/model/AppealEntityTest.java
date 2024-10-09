package se.sundsvall.casedata.integration.db.model;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToStringExcluding;
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

import se.sundsvall.casedata.integration.db.model.enums.AppealStatus;
import se.sundsvall.casedata.integration.db.model.enums.TimelinessReview;

class AppealEntityTest {

	@BeforeAll
	static void setup() {
		registerValueGenerator(() -> now().plusDays(new Random().nextInt()), OffsetDateTime.class);
	}

	@Test
	void testBean() {
		MatcherAssert.assertThat(AppealEntity.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToStringExcluding("errand")));
	}

	@Test
	void builder() {
		// Arrange
		var id = 1L;
		var errand = new ErrandEntity();
		var municipalityId = "municipalityId";
		var namespace = "namespace";
		var description = "description";
		var registeredAt = now();
		var appealConcernCommunicatedAt = now();
		var status = AppealStatus.NEW;
		var timelinessReview = TimelinessReview.NOT_CONDUCTED;
		var decision = new DecisionEntity();
		var created = now();
		var updated = now();

		// Act
		var bean = AppealEntity.builder()
			.withId(id)
			.withErrand(errand)
			.withMunicipalityId(municipalityId)
			.withNamespace(namespace)
			.withDescription(description)
			.withRegisteredAt(registeredAt)
			.withAppealConcernCommunicatedAt(appealConcernCommunicatedAt)
			.withStatus(status)
			.withTimelinessReview(timelinessReview)
			.withDecision(decision)
			.withCreated(created)
			.withUpdated(updated)
			.build();

		// Assert
		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.getId()).isEqualTo(id);
		assertThat(bean.getErrand()).isEqualTo(errand);
		assertThat(bean.getMunicipalityId()).isEqualTo(municipalityId);
		assertThat(bean.getNamespace()).isEqualTo(namespace);
		assertThat(bean.getDescription()).isEqualTo(description);
		assertThat(bean.getRegisteredAt()).isEqualTo(registeredAt);
		assertThat(bean.getAppealConcernCommunicatedAt()).isEqualTo(appealConcernCommunicatedAt);
		assertThat(bean.getStatus()).isEqualTo(status);
		assertThat(bean.getTimelinessReview()).isEqualTo(timelinessReview);
		assertThat(bean.getDecision()).isEqualTo(decision);
		assertThat(bean.getCreated()).isEqualTo(created);
		assertThat(bean.getUpdated()).isEqualTo(updated);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(AppealEntity.builder().build()).hasAllNullFieldsOrPropertiesExcept("status", "timelinessReview", "version");
		assertThat(new AppealEntity()).hasAllNullFieldsOrPropertiesExcept("status", "timelinessReview", "version");
	}

}
