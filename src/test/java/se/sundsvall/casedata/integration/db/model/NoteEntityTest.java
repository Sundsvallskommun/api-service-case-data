package se.sundsvall.casedata.integration.db.model;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEqualsExcluding;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCodeExcluding;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToStringExcluding;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static com.google.code.beanmatchers.BeanMatchers.registerValueGenerator;
import static java.time.OffsetDateTime.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Random;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import se.sundsvall.casedata.integration.db.model.enums.NoteType;

class NoteEntityTest {

	@BeforeAll
	static void setup() {
		registerValueGenerator(() -> now().plusDays(new Random().nextInt()), OffsetDateTime.class);
	}

	@Test
	void testBean() {
		MatcherAssert.assertThat(NoteEntity.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCodeExcluding("errand"),
			hasValidBeanEqualsExcluding("errand"),
			hasValidBeanToStringExcluding("errand")));
	}

	@Test
	void builder() {
		// Arrange
		var id = 1L;
		var errand = new ErrandEntity();
		var municipalityId = "municipalityId";
		var namespace = "namespace";
		var title = "title";
		var text = "text";
		var createdBy = "createdBy";
		var updatedBy = "updatedBy";
		var noteType = NoteType.PUBLIC;
		var extraParameters = Map.of("key", "value");
		var created = now();
		var updated = now();

		// Act
		var bean = NoteEntity.builder()
			.withId(id)
			.withErrand(errand)
			.withMunicipalityId(municipalityId)
			.withNamespace(namespace)
			.withTitle(title)
			.withText(text)
			.withCreatedBy(createdBy)
			.withUpdatedBy(updatedBy)
			.withNoteType(noteType)
			.withExtraParameters(extraParameters)
			.withCreated(created)
			.withUpdated(updated)
			.build();

		// Assert
		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.getId()).isEqualTo(id);
		assertThat(bean.getErrand()).isEqualTo(errand);
		assertThat(bean.getMunicipalityId()).isEqualTo(municipalityId);
		assertThat(bean.getNamespace()).isEqualTo(namespace);
		assertThat(bean.getTitle()).isEqualTo(title);
		assertThat(bean.getText()).isEqualTo(text);
		assertThat(bean.getCreatedBy()).isEqualTo(createdBy);
		assertThat(bean.getUpdatedBy()).isEqualTo(updatedBy);
		assertThat(bean.getNoteType()).isEqualTo(noteType);
		assertThat(bean.getExtraParameters()).isEqualTo(extraParameters);
		assertThat(bean.getCreated()).isEqualTo(created);
		assertThat(bean.getUpdated()).isEqualTo(updated);

	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(NoteEntity.builder().build()).hasAllNullFieldsOrPropertiesExcept("extraParameters", "version")
			.satisfies(bean -> {
				assertThat(bean.getVersion()).isZero();
				assertThat(bean.getExtraParameters()).isEmpty();
			});
		assertThat(new NoteEntity()).hasAllNullFieldsOrPropertiesExcept("extraParameters", "version")
			.satisfies(bean -> {
				assertThat(bean.getVersion()).isZero();
				assertThat(bean.getExtraParameters()).isEmpty();
			});
	}

}
