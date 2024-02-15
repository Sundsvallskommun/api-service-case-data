package se.sundsvall.casedata.integration.db.model;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.hamcrest.CoreMatchers.allOf;

import org.assertj.core.api.Assertions;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import se.sundsvall.casedata.TestUtil;
import se.sundsvall.casedata.integration.db.model.enums.StakeholderType;
import se.sundsvall.casedata.service.util.mappers.EntityMapper;

@ExtendWith(MockitoExtension.class)
class CoordinatesTest {

	@Mock(answer = Answers.CALLS_REAL_METHODS)
	EntityMapper entityMapper;

	@Test
	void testBean() {
		MatcherAssert.assertThat(Coordinates.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testFields() {
		final Coordinates coordinates = entityMapper.toErrand(TestUtil.createErrandDTO()).getStakeholders().stream()
			.filter(stakeholder -> stakeholder.getType().equals(StakeholderType.PERSON))
			.map(Stakeholder::getAddresses)
			.findFirst().orElseThrow()
			.stream().map(Address::getLocation)
			.findFirst().orElseThrow();

		Assertions.assertThat(coordinates).isNotNull().hasNoNullFieldsOrProperties();
	}

}
