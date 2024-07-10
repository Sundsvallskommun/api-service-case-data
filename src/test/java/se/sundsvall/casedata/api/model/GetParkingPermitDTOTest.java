package se.sundsvall.casedata.api.model;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static com.google.code.beanmatchers.BeanMatchers.registerValueGenerator;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;

import java.time.OffsetDateTime;
import java.util.Random;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class GetParkingPermitDTOTest {

	@BeforeAll
	static void setup() {
		registerValueGenerator(() -> OffsetDateTime.now().plusDays(new Random().nextInt()), OffsetDateTime.class);
	}

	@Test
	void testBean() {
		MatcherAssert.assertThat(GetParkingPermitDTO.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void builderTest() {
		final var artefactPermitNumber = "artefactPermitNumber";
		final var artefactPermitStatus = "artefactPermitStatus";
		final var errandId = 123L;
		final var errandDecision = new DecisionDTO();

		final var bean = GetParkingPermitDTO.builder()
			.withArtefactPermitNumber(artefactPermitNumber)
			.withArtefactPermitStatus(artefactPermitStatus)
			.withErrandId(errandId)
			.withErrandDecision(errandDecision)
			.build();

		assertThat(bean.getArtefactPermitNumber()).isEqualTo(artefactPermitNumber);
		assertThat(bean.getArtefactPermitStatus()).isEqualTo(artefactPermitStatus);
		assertThat(bean.getErrandId()).isEqualTo(errandId);
		assertThat(bean.getErrandDecision()).isEqualTo(errandDecision);
	}

	@Test
	void testNoDirtOnEmptyBean() {
		assertThat(GetParkingPermitDTO.builder().build()).hasAllNullFieldsOrProperties();
		assertThat(new GetParkingPermitDTO()).hasAllNullFieldsOrProperties();
	}
}
