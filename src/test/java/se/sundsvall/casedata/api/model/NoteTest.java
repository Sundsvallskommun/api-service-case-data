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
import java.util.HashMap;
import java.util.Random;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import se.sundsvall.casedata.integration.db.model.enums.NoteType;

class NoteTest {

	@BeforeAll
	static void setup() {
		registerValueGenerator(() -> OffsetDateTime.now().plusDays(new Random().nextInt()), OffsetDateTime.class);
		registerValueGenerator(() -> LocalDate.now().plusDays(new Random().nextInt()), LocalDate.class);
	}

	@Test
	void testBean() {
		MatcherAssert.assertThat(Note.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void builderTest() {
		// Arrange
		final var title = "title";
		final var text = "text";
		final var createdBy = "createdBy";
		final var updatedBy = "updatedBy";
		final var noteType = NoteType.INTERNAL;
		final var extraParameters = new HashMap<String, String>();
		final var municipalityId = "municipalityId";
		final var namespace = "namespace";

		// Act
		final var bean = Note.builder()
			.withTitle(title)
			.withText(text)
			.withCreatedBy(createdBy)
			.withUpdatedBy(updatedBy)
			.withNoteType(noteType)
			.withExtraParameters(extraParameters)
			.withMunicipalityId(municipalityId)
			.withNamespace(namespace)
			.build();

		// Assert
		assertThat(bean.getTitle()).isEqualTo(title);
		assertThat(bean.getText()).isEqualTo(text);
		assertThat(bean.getCreatedBy()).isEqualTo(createdBy);
		assertThat(bean.getUpdatedBy()).isEqualTo(updatedBy);
		assertThat(bean.getNoteType()).isEqualTo(noteType);
		assertThat(bean.getExtraParameters()).isEqualTo(extraParameters);
		assertThat(bean.getMunicipalityId()).isEqualTo(municipalityId);
		assertThat(bean.getNamespace()).isEqualTo(namespace);
	}

	@Test
	void testNoDirtOnEmptyBean() {
		assertThat(Note.builder().build()).hasAllNullFieldsOrPropertiesExcept("id", "extraParameters", "version")
			.satisfies(bean -> {
				assertThat(bean.getId()).isZero();
				assertThat(bean.getVersion()).isZero();
				assertThat(bean.getExtraParameters()).isEmpty();
			});
		assertThat(new Note()).hasAllNullFieldsOrPropertiesExcept("id", "extraParameters", "version")
			.satisfies(bean -> {
				assertThat(bean.getId()).isZero();
				assertThat(bean.getVersion()).isZero();
				assertThat(bean.getExtraParameters()).isEmpty();
			});
	}

}
