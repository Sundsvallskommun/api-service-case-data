package se.sundsvall.casedata.integration.db.specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.casedata.integration.db.model.enums.DecisionType.FINAL;
import static se.sundsvall.casedata.integration.db.specification.ErrandEntitySpecification.buildDecisionTypeFinalFilter;
import static se.sundsvall.casedata.integration.db.specification.ErrandEntitySpecification.buildMunicipalityIdFilter;
import static se.sundsvall.casedata.integration.db.specification.ErrandEntitySpecification.buildNamespaceFilter;
import static se.sundsvall.casedata.integration.db.specification.ErrandEntitySpecification.buildStakeholderFilter;
import static se.sundsvall.casedata.integration.db.specification.ErrandEntitySpecification.distinct;

@ExtendWith(MockitoExtension.class)
class ErrandEntitySpecificationTest {

	@Mock
	private Path<Object> path;

	@Mock
	private Predicate expected;

	@Mock
	private Predicate conjunctionPredicate;

	@Mock
	private Predicate andPredicate;

	@Mock
	private Root<ErrandEntity> root;

	@Mock
	private Join<Object, Object> join;

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

	@Test
	void buildStakeholderFilterWithPersonIdAndRole() {
		// Arrange
		final var personId = "personId";
		final var role = "role";
		final var spec = buildStakeholderFilter(personId, role);

		when(root.join("stakeholders")).thenReturn(join);
		when(criteriaBuilder.conjunction()).thenReturn(conjunctionPredicate);
		when(join.get("personId")).thenReturn(path);
		when(criteriaBuilder.equal(path, personId)).thenReturn(expected);
		when(criteriaBuilder.and(conjunctionPredicate, expected)).thenReturn(andPredicate);
		when(criteriaBuilder.isMember(eq(role), any())).thenReturn(expected);
		when(criteriaBuilder.and(andPredicate, expected)).thenReturn(expected);

		// Act
		final var result = spec.toPredicate(root, criteriaQuery, criteriaBuilder);

		// Assert
		assertThat(result).isSameAs(expected);
		verify(root).join("stakeholders");
		verify(criteriaBuilder).conjunction();
		verify(join).get("personId");
		verify(criteriaBuilder).equal(path, personId);
		verify(join).get("roles");
		verify(criteriaBuilder).isMember(eq(role), any());
	}

	@Test
	void buildStakeholderFilterWithNullPersonIdAndNullRole() {
		// Arrange
		final var spec = buildStakeholderFilter(null, null);

		when(root.join("stakeholders")).thenReturn(join);
		when(criteriaBuilder.conjunction()).thenReturn(conjunctionPredicate);

		// Act
		final var result = spec.toPredicate(root, criteriaQuery, criteriaBuilder);

		// Assert
		assertThat(result).isSameAs(conjunctionPredicate);
		verify(root).join("stakeholders");
		verify(criteriaBuilder).conjunction();
		verifyNoMoreInteractions(criteriaBuilder);
	}

	@Test
	void buildStakeholderFilterWithOnlyPersonId() {
		// Arrange
		final var personId = "personId";
		final var spec = buildStakeholderFilter(personId, null);

		when(root.join("stakeholders")).thenReturn(join);
		when(criteriaBuilder.conjunction()).thenReturn(conjunctionPredicate);
		when(join.get("personId")).thenReturn(path);
		when(criteriaBuilder.equal(path, personId)).thenReturn(expected);
		when(criteriaBuilder.and(conjunctionPredicate, expected)).thenReturn(andPredicate);

		// Act
		final var result = spec.toPredicate(root, criteriaQuery, criteriaBuilder);

		// Assert
		assertThat(result).isSameAs(andPredicate);
		verify(root).join("stakeholders");
		verify(criteriaBuilder).conjunction();
		verify(join).get("personId");
		verify(criteriaBuilder).equal(path, personId);
		verify(criteriaBuilder).and(conjunctionPredicate, expected);
	}

	@Test
	void buildStakeholderFilterWithOnlyRole() {
		// Arrange
		final var role = "role";
		final var spec = buildStakeholderFilter(null, role);

		when(root.join("stakeholders")).thenReturn(join);
		when(criteriaBuilder.conjunction()).thenReturn(conjunctionPredicate);
		when(criteriaBuilder.isMember(eq(role), any())).thenReturn(expected);
		when(criteriaBuilder.and(conjunctionPredicate, expected)).thenReturn(andPredicate);

		// Act
		final var result = spec.toPredicate(root, criteriaQuery, criteriaBuilder);

		// Assert
		assertThat(result).isSameAs(andPredicate);
		verify(root).join("stakeholders");
		verify(criteriaBuilder).conjunction();
		verify(join).get("roles");
		verify(criteriaBuilder).isMember(eq(role), any());
		verify(criteriaBuilder).and(conjunctionPredicate, expected);
	}

	@Test
	void buildDecisionTypeFinalFilterBuildsEqualPredicate() {
		// Arrange
		final var spec = buildDecisionTypeFinalFilter();

		when(root.join("decisions")).thenReturn(join);
		when(join.get("decisionType")).thenReturn(path);
		when(criteriaBuilder.equal(path, FINAL)).thenReturn(expected);

		// Act
		final var result = spec.toPredicate(root, criteriaQuery, criteriaBuilder);

		// Assert
		assertThat(result).isSameAs(expected);
		verify(root).join("decisions");
		verify(join).get("decisionType");
		verify(criteriaBuilder).equal(path, FINAL);
		verifyNoMoreInteractions(root, criteriaBuilder);
		verifyNoInteractions(criteriaQuery);
	}
}
