package se.sundsvall.casedata.integration.db.model;

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
import java.util.Map;
import java.util.Random;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class AttachmentEntityTest {

	@BeforeAll
	static void setup() {
		registerValueGenerator(() -> now().plusDays(new Random().nextInt()), OffsetDateTime.class);
	}

	@Test
	void testBean() {
		MatcherAssert.assertThat(AttachmentEntity.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void builder() {
		// Arrange
		var id = 1L;
		var category = "category";
		var name = "name";
		var note = "note";
		var extension = "extension";
		var mimeType = "mimeType";
		var file = "file";
		var errandNumber = "errandNumber";
		var municipalityId = "municipalityId";
		var namespace = "namespace";
		var created = now();
		var updated = now();
		var extraParameters = Map.of("key", "value");

		// Act
		var bean = AttachmentEntity.builder()
			.withId(id)
			.withCategory(category)
			.withName(name)
			.withNote(note)
			.withExtension(extension)
			.withMimeType(mimeType)
			.withFile(file)
			.withErrandNumber(errandNumber)
			.withMunicipalityId(municipalityId)
			.withNamespace(namespace)
			.withCreated(created)
			.withUpdated(updated)
			.withExtraParameters(extraParameters)
			.build();

		// Assert
		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.getId()).isEqualTo(id);
		assertThat(bean.getCategory()).isEqualTo(category);
		assertThat(bean.getName()).isEqualTo(name);
		assertThat(bean.getNote()).isEqualTo(note);
		assertThat(bean.getExtension()).isEqualTo(extension);
		assertThat(bean.getMimeType()).isEqualTo(mimeType);
		assertThat(bean.getFile()).isEqualTo(file);
		assertThat(bean.getErrandNumber()).isEqualTo(errandNumber);
		assertThat(bean.getMunicipalityId()).isEqualTo(municipalityId);
		assertThat(bean.getNamespace()).isEqualTo(namespace);
		assertThat(bean.getCreated()).isEqualTo(created);
		assertThat(bean.getUpdated()).isEqualTo(updated);
		assertThat(bean.getExtraParameters()).isEqualTo(extraParameters);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(AttachmentEntity.builder().build()).hasAllNullFieldsOrPropertiesExcept("extraParameters", "version")
			.satisfies(bean -> {
				assertThat(bean.getExtraParameters()).isEmpty();
				assertThat(bean.getVersion()).isZero();
			});
		assertThat(new AttachmentEntity()).hasAllNullFieldsOrPropertiesExcept("extraParameters", "version")
			.satisfies(bean -> {
				assertThat(bean.getExtraParameters()).isEmpty();
				assertThat(bean.getVersion()).isZero();
			});
	}

}
