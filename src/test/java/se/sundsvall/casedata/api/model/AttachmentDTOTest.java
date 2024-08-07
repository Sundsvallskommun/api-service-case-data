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
import java.util.HashMap;
import java.util.Random;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

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
	void builderTest() {
		final var category = "category";
		final var name = "name";
		final var note = "note";
		final var extension = "extension";
		final var mimeType = "mimeType";
		final var file = "file";
		final var errandNumber = "errandNumber";
		final var extraParameters = new HashMap<String, String>();

		final var bean = AttachmentDTO.builder()
			.withCategory(category)
			.withName(name)
			.withNote(note)
			.withExtension(extension)
			.withMimeType(mimeType)
			.withFile(file)
			.withErrandNumber(errandNumber)
			.withExtraParameters(extraParameters)
			.build();

		assertThat(bean.getCategory()).isEqualTo(category);
		assertThat(bean.getName()).isEqualTo(name);
		assertThat(bean.getNote()).isEqualTo(note);
		assertThat(bean.getExtension()).isEqualTo(extension);
		assertThat(bean.getMimeType()).isEqualTo(mimeType);
		assertThat(bean.getFile()).isEqualTo(file);
		assertThat(bean.getErrandNumber()).isEqualTo(errandNumber);
		assertThat(bean.getExtraParameters()).isEqualTo(extraParameters);
	}

	@Test
	void testNoDirtOnEmptyBean() {
		assertThat(AttachmentDTO.builder().build()).hasAllNullFieldsOrPropertiesExcept("extraParameters", "version");
		assertThat(new AttachmentDTO()).hasAllNullFieldsOrPropertiesExcept("extraParameters", "version");
	}
}
