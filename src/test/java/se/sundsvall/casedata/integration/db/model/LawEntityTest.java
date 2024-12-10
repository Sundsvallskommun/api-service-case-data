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
import java.util.Random;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class LawEntityTest {

	@BeforeAll
	static void setup() {
		registerValueGenerator(() -> now().plusDays(new Random().nextInt()), OffsetDateTime.class);
	}

	@Test
	void testBean() {
		MatcherAssert.assertThat(LawEntity.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void builder() {
		// Arrange
		var heading = "heading";
		var sfs = "sfs";
		var chapter = "chapter";
		var article = "article";

		// Act
		var bean = LawEntity.builder()
			.withHeading(heading)
			.withSfs(sfs)
			.withChapter(chapter)
			.withArticle(article)
			.build();

		// Assert
		assertThat(bean).hasNoNullFieldsOrProperties();
		assertThat(bean.getHeading()).isEqualTo(heading);
		assertThat(bean.getSfs()).isEqualTo(sfs);
		assertThat(bean.getChapter()).isEqualTo(chapter);
		assertThat(bean.getArticle()).isEqualTo(article);

	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(LawEntity.builder().build()).hasAllNullFieldsOrProperties();
		assertThat(new LawEntity()).hasAllNullFieldsOrProperties();
	}

}
