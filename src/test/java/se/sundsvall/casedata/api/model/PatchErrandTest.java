package se.sundsvall.casedata.api.model;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import se.sundsvall.casedata.api.model.validation.enums.CaseType;
import se.sundsvall.casedata.integration.db.model.enums.Priority;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static com.google.code.beanmatchers.BeanMatchers.registerValueGenerator;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;
import static se.sundsvall.casedata.TestUtil.createExtraParametersList;
import static se.sundsvall.casedata.TestUtil.createFacility;

class PatchErrandTest {

	@BeforeAll
	static void setup() {
		registerValueGenerator(() -> OffsetDateTime.now().plusDays(new Random().nextInt()), OffsetDateTime.class);
		registerValueGenerator(() -> LocalDate.now().plusDays(new Random().nextInt()), LocalDate.class);
	}

	@Test
	void bean() {
		MatcherAssert.assertThat(PatchErrand.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void builderMethods() {
		// Arrange
		final var externalCaseId = UUID.randomUUID().toString();
		final var caseType = CaseType.PARKING_PERMIT;
		final var priority = Priority.HIGH;
		final var description = "abc";
		final var caseTitleAddition = "abc";
		final var diaryNumber = "abc";
		final var phase = "Aktualisering";
		final var startDate = LocalDate.now();
		final var endDate = LocalDate.now();
		final var applicationReceived = OffsetDateTime.now();
		final var extraParameters = createExtraParametersList();
		final var facilities = List.of(createFacility());
		final var suspension = new Suspension();
		final var relatesTo = List.of(new RelatedErrand());

		// Act
		final var result = PatchErrand.builder()
			.withExternalCaseId(externalCaseId)
			.withCaseType(caseType)
			.withPriority(priority)
			.withDescription(description)
			.withCaseTitleAddition(caseTitleAddition)
			.withDiaryNumber(diaryNumber)
			.withPhase(phase)
			.withStartDate(startDate)
			.withEndDate(endDate)
			.withApplicationReceived(applicationReceived)
			.withExtraParameters(extraParameters)
			.withFacilities(facilities)
			.withSuspension(suspension)
			.withRelatesTo(relatesTo)
			.build();

		// Assert
		assertThat(result).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(result.getExternalCaseId()).isEqualTo(externalCaseId);
		assertThat(result.getCaseType()).isEqualTo(caseType);
		assertThat(result.getPriority()).isEqualTo(priority);
		assertThat(result.getDescription()).isEqualTo(description);
		assertThat(result.getCaseTitleAddition()).isEqualTo(caseTitleAddition);
		assertThat(result.getDiaryNumber()).isEqualTo(diaryNumber);
		assertThat(result.getPhase()).isEqualTo(phase);
		assertThat(result.getStartDate()).isEqualTo(startDate);
		assertThat(result.getEndDate()).isEqualTo(endDate);
		assertThat(result.getApplicationReceived()).isEqualTo(applicationReceived);
		assertThat(result.getExtraParameters()).isEqualTo(extraParameters);
		assertThat(result.getFacilities()).isEqualTo(facilities);
		assertThat(result.getSuspension()).isEqualTo(suspension);
		assertThat(result.getRelatesTo()).isEqualTo(relatesTo);
	}

	@Test
	void noDirtOnCreatedBean() {
		assertThat(PatchErrand.builder().build()).hasAllNullFieldsOrProperties();
		assertThat(new PatchErrand()).hasAllNullFieldsOrProperties();
	}

}
