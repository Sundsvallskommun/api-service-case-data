package se.sundsvall.casedata.api.model;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Random;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static com.google.code.beanmatchers.BeanMatchers.registerValueGenerator;
import static java.time.OffsetDateTime.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;

class AttachmentTest {

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
	void builderTest() {
		// Arrange
		final var category = "category";
		final var name = "name";
		final var note = "note";
		final var extension = "extension";
		final var mimeType = "mimeType";
		final var file = "file";
		final var errandId = 123L;
		final var extraParameters = new HashMap<String, String>();
		final var municipalityId = "municipalityId";
		final var namespace = "namespace";

		// Act
		final var bean = Attachment.builder()
			.withCategory(category)
			.withName(name)
			.withNote(note)
			.withExtension(extension)
			.withMimeType(mimeType)
			.withFile(file)
			.withErrandId(errandId)
			.withExtraParameters(extraParameters)
			.withMunicipalityId(municipalityId)
			.withNamespace(namespace)
			.build();

		// Assert
		assertThat(bean.getCategory()).isEqualTo(category);
		assertThat(bean.getName()).isEqualTo(name);
		assertThat(bean.getNote()).isEqualTo(note);
		assertThat(bean.getExtension()).isEqualTo(extension);
		assertThat(bean.getMimeType()).isEqualTo(mimeType);
		assertThat(bean.getFile()).isEqualTo(file);
		assertThat(bean.getErrandId()).isEqualTo(errandId);
		assertThat(bean.getExtraParameters()).isEqualTo(extraParameters);
		assertThat(bean.getMunicipalityId()).isEqualTo(municipalityId);
		assertThat(bean.getNamespace()).isEqualTo(namespace);
	}

	@Test
	void testNoDirtOnEmptyBean() {
		assertThat(Attachment.builder().build()).hasAllNullFieldsOrPropertiesExcept("extraParameters", "version")
			.satisfies(bean -> {
				assertThat(bean.getVersion()).isZero();
				assertThat(bean.getExtraParameters()).isEmpty();
			});
		assertThat(new Attachment()).hasAllNullFieldsOrPropertiesExcept("extraParameters", "version")
			.satisfies(bean -> {
				assertThat(bean.getVersion()).isZero();
				assertThat(bean.getExtraParameters()).isEmpty();
			});
	}

}
