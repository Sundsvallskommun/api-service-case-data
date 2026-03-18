package se.sundsvall.casedata.integration.db.specification;

import org.springframework.data.jpa.domain.Specification;
import se.sundsvall.casedata.integration.db.model.ErrandEntity;

import static se.sundsvall.casedata.integration.db.model.enums.DecisionType.FINAL;

public final class ErrandEntitySpecification {

	private static final String MUNICIPALITY_ID = "municipalityId";
	private static final String NAMESPACE = "namespace";
	private static final String DECISIONS = "decisions";
	private static final String DECISION_TYPE = "decisionType";
	private static final String STAKEHOLDERS = "stakeholders";
	private static final String PERSON_ID = "personId";
	private static final String ROLES = "roles";

	private ErrandEntitySpecification() {
		// Utility class
	}

	/**
	 * Retrieves the given entities municipality id and compares it to the given value using an equal predicate.
	 *
	 * @param  value the value to compare the municipality id to
	 * @return       a specification that compares the municipality id to the given value
	 */
	public static Specification<ErrandEntity> buildMunicipalityIdFilter(final String value) {
		return (entity, _, cb) -> {
			if (value == null) {
				return null;
			}
			return cb.equal(entity.get(MUNICIPALITY_ID), value);
		};
	}

	/**
	 * Retrieves the given entities namespace and compares it to the given value using an equal predicate.
	 *
	 * @param  value the value to compare the namespace to
	 * @return       a specification that compares the namespace to the given value
	 */
	public static Specification<ErrandEntity> buildNamespaceFilter(final String value) {
		return (entity, _, cb) -> {
			if (value == null) {
				return null;
			}
			return cb.equal(entity.get(NAMESPACE), value);
		};
	}

	/**
	 * Filters errands by stakeholder personId and/or role. Both predicates are applied to the same
	 * stakeholder join, ensuring the same stakeholder matches both criteria. Null values are ignored.
	 *
	 * @param  personId the person id to match on the stakeholder
	 * @param  role     the role to match on the stakeholder
	 * @return          a specification that filters on stakeholder personId and/or role
	 */
	public static Specification<ErrandEntity> buildStakeholderFilter(final String personId, final String role) {
		return (entity, _, cb) -> {
			var stakeholderJoin = entity.join(STAKEHOLDERS);
			var predicates = cb.conjunction();
			if (personId != null) {
				predicates = cb.and(predicates, cb.equal(stakeholderJoin.get(PERSON_ID), personId));
			}
			if (role != null) {
				predicates = cb.and(predicates, cb.isMember(role, stakeholderJoin.get(ROLES)));
			}
			return predicates;
		};
	}

	/**
	 * Filters errands that have a decision with type FINAL by joining the decisions relationship.
	 *
	 * @return a specification that filters on decisions with type FINAL
	 */
	public static Specification<ErrandEntity> buildDecisionTypeFinalFilter() {
		return (entity, _, cb) -> cb.equal(entity.join(DECISIONS).get(DECISION_TYPE), FINAL);
	}

	/**
	 * Creates a distinct specification to avoid any duplicates in the result.
	 *
	 * @return a specification that makes the result distinct
	 */
	public static Specification<ErrandEntity> distinct() {
		return (_, cq, _) -> {
			if (cq == null) {
				return null;
			}
			cq.distinct(true);
			return null;
		};
	}
}
