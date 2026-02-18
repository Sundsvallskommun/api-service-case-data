package se.sundsvall.casedata.api.model;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;

class LawTest {

	@Test
	void testBean() {
		MatcherAssert.assertThat(Law.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void builderTest() {
		// Arrange
		final var heading = "heading";
		final var sfs = "sfs";
		final var chapter = "chapter";
		final var article = "article";

		// Act
		final var bean = Law.builder()
			.withHeading(heading)
			.withSfs(sfs)
			.withChapter(chapter)
			.withArticle(article)
			.build();

		// Assert
		assertThat(bean.getHeading()).isEqualTo(heading);
		assertThat(bean.getSfs()).isEqualTo(sfs);
		assertThat(bean.getChapter()).isEqualTo(chapter);
		assertThat(bean.getArticle()).isEqualTo(article);
	}

	@Test
	void testNoDirtOnEmptyBean() {
		assertThat(Law.builder().build()).hasAllNullFieldsOrProperties();
		assertThat(new Law()).hasAllNullFieldsOrProperties();
	}

}
