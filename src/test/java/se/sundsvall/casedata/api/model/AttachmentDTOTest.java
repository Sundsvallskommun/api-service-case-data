package se.sundsvall.casedata.api.model;

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

import se.sundsvall.casedata.api.model.validation.enums.AttachmentCategory;

class AttachmentDTOTest {

	@BeforeAll
	static void setup() {
		registerValueGenerator(() -> now().plusDays(new Random().nextInt()), OffsetDateTime.class);
	}

	@Test
	void testBean() {
		MatcherAssert.assertThat(AttachmentDTO.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testFields() {
		final AttachmentDTO dto = new AttachmentDTO();
		dto.setId(new Random().nextLong());
		dto.setCreated(OffsetDateTime.now().plusDays(new Random().nextInt()));
		dto.setUpdated(OffsetDateTime.now().plusDays(new Random().nextInt()));
		dto.setId(new Random().nextLong());
		dto.setCreated(OffsetDateTime.now().plusDays(new Random().nextInt()));
		dto.setUpdated(OffsetDateTime.now().plusDays(new Random().nextInt()));
		dto.setCategory(AttachmentCategory.SIGNATURE.toString());
		dto.setName("someFileName");
		dto.setMimeType("application/pdf");
		dto.setFile("someFile");
		dto.setExtension("pdf");
		dto.setNote("someNote");
		dto.setErrandNumber("someErrandNumber");

		assertThat(dto).isNotNull().hasNoNullFieldsOrProperties();
	}

}
