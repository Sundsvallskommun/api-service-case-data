package se.sundsvall.casedata.api.model;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static com.google.code.beanmatchers.BeanMatchers.registerValueGenerator;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import se.sundsvall.casedata.TestUtil;
import se.sundsvall.casedata.api.model.validation.enums.CaseType;
import se.sundsvall.casedata.integration.db.model.enums.Priority;

class PatchErrandDTOTest {

	@BeforeAll
	static void setup() {
		registerValueGenerator(() -> OffsetDateTime.now().plusDays(new Random().nextInt()), OffsetDateTime.class);
		registerValueGenerator(() -> LocalDate.now().plusDays(new Random().nextInt()), LocalDate.class);
	}

	@Test
	void bean() {
		MatcherAssert.assertThat(PatchErrandDTO.class, allOf(
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
		final var municipalityId = "123";
		final var startDate = LocalDate.now();
		final var endDate = LocalDate.now();
		final var applicationReceived = OffsetDateTime.now();
		final var extraParameters = TestUtil.createExtraParameters();
		final var facilities = List.of(TestUtil.createFacilityDTO());

		// Act
		final var dto = PatchErrandDTO.builder()
			.withExternalCaseId(externalCaseId)
			.withCaseType(caseType)
			.withPriority(priority)
			.withDescription(description)
			.withCaseTitleAddition(caseTitleAddition)
			.withDiaryNumber(diaryNumber)
			.withPhase(phase)
			.withMunicipalityId(municipalityId)
			.withStartDate(startDate)
			.withEndDate(endDate)
			.withApplicationReceived(applicationReceived)
			.withExtraParameters(extraParameters)
			.withFacilities(facilities)
			.build();

		// Assert
		assertThat(dto).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(dto.getExternalCaseId()).isEqualTo(externalCaseId);
		assertThat(dto.getCaseType()).isEqualTo(caseType);
		assertThat(dto.getPriority()).isEqualTo(priority);
		assertThat(dto.getDescription()).isEqualTo(description);
		assertThat(dto.getCaseTitleAddition()).isEqualTo(caseTitleAddition);
		assertThat(dto.getDiaryNumber()).isEqualTo(diaryNumber);
		assertThat(dto.getPhase()).isEqualTo(phase);
		assertThat(dto.getMunicipalityId()).isEqualTo(municipalityId);
		assertThat(dto.getStartDate()).isEqualTo(startDate);
		assertThat(dto.getEndDate()).isEqualTo(endDate);
		assertThat(dto.getApplicationReceived()).isEqualTo(applicationReceived);
		assertThat(dto.getExtraParameters()).isEqualTo(extraParameters);
		assertThat(dto.getFacilities()).isEqualTo(facilities);
	}

	@Test
	void noDirtOnCreatedBean() {
		assertThat(PatchErrandDTO.builder().build()).hasAllNullFieldsOrPropertiesExcept("extraParameters")
			.satisfies(dto -> assertThat(dto.getExtraParameters()).isEmpty());
		assertThat(new PatchErrandDTO()).hasAllNullFieldsOrPropertiesExcept("extraParameters")
			.satisfies(dto -> assertThat(dto.getExtraParameters()).isEmpty());
	}
}
