package se.sundsvall.casedata.integration.db.model;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static com.google.code.beanmatchers.BeanMatchers.registerValueGenerator;
import static java.time.OffsetDateTime.now;
import static org.hamcrest.CoreMatchers.allOf;

import java.time.OffsetDateTime;
import java.util.Random;

import org.assertj.core.api.Assertions;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import se.sundsvall.casedata.integration.db.model.enums.AttachmentCategory;
import se.sundsvall.casedata.service.util.mappers.EntityMapper;

@ExtendWith(MockitoExtension.class)
class AttachmentTest {

	@Mock(answer = Answers.CALLS_REAL_METHODS)
	EntityMapper entityMapper;

	@BeforeAll
	static void setup() {
		registerValueGenerator(() -> now().plusDays(new Random().nextInt()), OffsetDateTime.class);
	}

	@Test
	void testBean() {
		MatcherAssert.assertThat(Attachment.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testFields() {

		final var attachment = new Attachment();

		attachment.setId(new Random().nextLong());
		attachment.setCreated(OffsetDateTime.now().plusDays(new Random().nextInt()));
		attachment.setUpdated(OffsetDateTime.now().plusDays(new Random().nextInt()));
		attachment.setCategory(AttachmentCategory.SIGNATURE);
		attachment.setName("someFileName");
		attachment.setMimeType("application/pdf");
		attachment.setFile("someFile");
		attachment.setExtension("pdf");
		attachment.setNote("someNote");
		attachment.setErrandNumber("someErrandNumber");

		Assertions.assertThat(attachment).isNotNull().hasNoNullFieldsOrProperties();
	}

}
