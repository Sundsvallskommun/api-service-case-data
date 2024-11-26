package se.sundsvall.casedata.integration.db.model;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEqualsExcluding;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCodeExcluding;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToStringExcluding;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static com.google.code.beanmatchers.BeanMatchers.registerValueGenerator;
import static java.time.OffsetDateTime.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import se.sundsvall.casedata.integration.db.model.enums.DecisionOutcome;
import se.sundsvall.casedata.integration.db.model.enums.DecisionType;

class DecisionEntityTest {

	@BeforeAll
	static void setup() {
		registerValueGenerator(() -> now().plusDays(new Random().nextInt()), OffsetDateTime.class);
	}

	@Test
	void testBean() {
		MatcherAssert.assertThat(DecisionEntity.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCodeExcluding("errand"),
			hasValidBeanEqualsExcluding("errand"),
			hasValidBeanToStringExcluding("errand")));
	}

	@Test
	void builder() {
		// Arrange
		var id = 1L;
		var errand = new ErrandEntity();
		var municipalityId = "municipalityId";
		var namespace = "namespace";
		var decisionType = DecisionType.FINAL;
		var decisionOutcome = DecisionOutcome.APPROVAL;
		var description = "description";
		var law = List.of(new LawEntity());
		var decidedBy = new StakeholderEntity();
		var decidedAt = now();
		var validFrom = now();
		var validTo = now();
		var attachments = List.of(new AttachmentEntity());
		var extraParameters = Map.of("key", "value");
		var created = now();
		var updated = now();

		// Act
		var bean = DecisionEntity.builder()
			.withId(id)
			.withErrand(errand)
			.withMunicipalityId(municipalityId)
			.withNamespace(namespace)
			.withDecisionType(decisionType)
			.withDecisionOutcome(decisionOutcome)
			.withDescription(description)
			.withLaw(law)
			.withDecidedBy(decidedBy)
			.withDecidedAt(decidedAt)
			.withValidFrom(validFrom)
			.withValidTo(validTo)
			.withAttachments(attachments)
			.withExtraParameters(extraParameters)
			.withCreated(created)
			.withUpdated(updated)
			.build();

		// Assert
		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.getErrand()).isEqualTo(errand);
		assertThat(bean.getMunicipalityId()).isEqualTo(municipalityId);
		assertThat(bean.getNamespace()).isEqualTo(namespace);
		assertThat(bean.getDecisionType()).isEqualTo(decisionType);
		assertThat(bean.getDecisionOutcome()).isEqualTo(decisionOutcome);
		assertThat(bean.getDescription()).isEqualTo(description);
		assertThat(bean.getLaw()).isEqualTo(law);
		assertThat(bean.getDecidedBy()).isEqualTo(decidedBy);
		assertThat(bean.getDecidedAt()).isEqualTo(decidedAt);
		assertThat(bean.getValidFrom()).isEqualTo(validFrom);
		assertThat(bean.getValidTo()).isEqualTo(validTo);
		assertThat(bean.getAttachments()).isEqualTo(attachments);
		assertThat(bean.getExtraParameters()).isEqualTo(extraParameters);
		assertThat(bean.getCreated()).isEqualTo(created);
		assertThat(bean.getUpdated()).isEqualTo(updated);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(DecisionEntity.builder().build()).hasAllNullFieldsOrPropertiesExcept("extraParameters", "version")
			.satisfies(bean -> {
				assertThat(bean.getExtraParameters()).isEmpty();
				assertThat(bean.getVersion()).isZero();
			});
		assertThat(new DecisionEntity()).hasAllNullFieldsOrPropertiesExcept("extraParameters", "version")
			.satisfies(bean -> {
				assertThat(bean.getExtraParameters()).isEmpty();
				assertThat(bean.getVersion()).isZero();
			});
	}

}
