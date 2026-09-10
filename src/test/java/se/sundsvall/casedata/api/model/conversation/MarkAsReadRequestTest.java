package se.sundsvall.casedata.api.model.conversation;

import java.util.List;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;

class MarkAsReadRequestTest {

	@Test
	void testBean() {
		MatcherAssert.assertThat(MarkAsReadRequest.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void builder() {
		// Arrange
		final var messageIds = List.of("d82bd8ac-1507-4d9a-958d-369261eecc15");

		// Act
		final var result = MarkAsReadRequest.builder()
			.withMessageIds(messageIds)
			.build();

		// Assert
		assertThat(result.getMessageIds()).isEqualTo(messageIds);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(MarkAsReadRequest.builder().build()).hasAllNullFieldsOrProperties();
		assertThat(new MarkAsReadRequest()).hasAllNullFieldsOrProperties();
	}

}
