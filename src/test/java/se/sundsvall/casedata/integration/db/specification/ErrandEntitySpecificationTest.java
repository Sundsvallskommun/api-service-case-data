package se.sundsvall.casedata.integration.db.specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.casedata.integration.db.model.ErrandEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.casedata.integration.db.specification.ErrandEntitySpecification.buildMunicipalityIdFilter;
import static se.sundsvall.casedata.integration.db.specification.ErrandEntitySpecification.buildNamespaceFilter;
import static se.sundsvall.casedata.integration.db.specification.ErrandEntitySpecification.distinct;

@ExtendWith(MockitoExtension.class)
class ErrandEntitySpecificationTest {

	@Mock
	private Path<Object> path;

	@Mock
	private Predicate expected;

	@Mock
	private Root<ErrandEntity> root;

	@Mock
	private CriteriaBuilder criteriaBuilder;

	@Mock
	private CriteriaQuery<Object> criteriaQuery;

	@Test
	void buildMunicipalityIdFilterReturnsNullPredicateWhenValueIsNull() {
		// Arrange
		final var spec = buildMunicipalityIdFilter(null);

		// Act
		final var result = spec.toPredicate(root, criteriaQuery, criteriaBuilder);

		// Assert
		assertThat(result).isNull();
		verifyNoInteractions(root, criteriaQuery, criteriaBuilder);
	}

	@Test
	void buildMunicipalityIdFilterBuildsEqualPredicateWhenValueIsProvided() {
		// Arrange
		final var value = "2281";
		final var municipalityId = "municipalityId";

		when(root.get(municipalityId)).thenReturn(path);
		when(criteriaBuilder.equal(path, value)).thenReturn(expected);

		final var spec = buildMunicipalityIdFilter(value);

		// Act
		final var result = spec.toPredicate(root, criteriaQuery, criteriaBuilder);

		// Assert
		assertThat(result).isSameAs(expected);
		final InOrder inOrder = inOrder(root, criteriaBuilder);
		inOrder.verify(root).get(municipalityId);
		inOrder.verify(criteriaBuilder).equal(path, value);
		verifyNoMoreInteractions(root, criteriaBuilder);
		verifyNoInteractions(criteriaQuery);
	}

	@Test
	void buildNamespaceFilterReturnsNullPredicateWhenValueIsNull() {
		// Arrange
		final var spec = buildNamespaceFilter(null);

		// Act
		final var result = spec.toPredicate(root, criteriaQuery, criteriaBuilder);

		// Assert
		assertThat(result).isNull();
		verifyNoInteractions(root, criteriaQuery, criteriaBuilder);
	}

	@Test
	void buildNamespaceFilterBuildsEqualPredicateWhenValueIsProvided() {
		// Arrange
		final var value = "MY_NAMESPACE";
		final var namespace = "namespace";
		final var spec = buildNamespaceFilter(value);

		when(root.get(namespace)).thenReturn(path);
		when(criteriaBuilder.equal(path, value)).thenReturn(expected);

		// Act
		final var result = spec.toPredicate(root, criteriaQuery, criteriaBuilder);

		// Assert
		assertThat(result).isSameAs(expected);
		final InOrder inOrder = inOrder(root, criteriaBuilder);
		inOrder.verify(root).get(namespace);
		inOrder.verify(criteriaBuilder).equal(path, value);
		verifyNoMoreInteractions(root, criteriaBuilder);
		verifyNoInteractions(criteriaQuery);
	}

	@Test
	void distinctMarksQueryAsDistinctAndReturnsNullPredicate() {
		// Arrange
		final var spec = distinct();

		// Act
		final var result = spec.toPredicate(root, criteriaQuery, criteriaBuilder);

		// Assert
		assertThat(result).isNull();
		verify(criteriaQuery).distinct(true);
		verifyNoMoreInteractions(criteriaQuery);
		verifyNoInteractions(root, criteriaBuilder);
	}

	@Test
	void distinctMarksQueryAsDistinctAndReturnsNullPredicateWhenCqIsNull() {
		// Arrange
		final var spec = distinct();

		// Act
		final var result = spec.toPredicate(root, null, criteriaBuilder);

		// Assert
		assertThat(result).isNull();
		verifyNoInteractions(root, criteriaQuery, criteriaBuilder);
	}
}
