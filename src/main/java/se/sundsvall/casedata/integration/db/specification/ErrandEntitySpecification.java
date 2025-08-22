package se.sundsvall.casedata.integration.db.specification;

import org.springframework.data.jpa.domain.Specification;
import se.sundsvall.casedata.integration.db.model.ErrandEntity;

public final class ErrandEntitySpecification {

	private static final String MUNICIPALITY_ID = "municipalityId";
	private static final String NAMESPACE = "namespace";

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
		return (entity, cq, cb) -> {
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
		return (entity, cq, cb) -> {
			if (value == null) {
				return null;
			}
			return cb.equal(entity.get(NAMESPACE), value);
		};
	}

	/**
	 * Creates a distinct specification to avoid any duplicates in the result.
	 *
	 * @return a specification that makes the result distinct
	 */
	public static Specification<ErrandEntity> distinct() {
		return (entity, cq, cb) -> {
			if (cq == null) {
				return null;
			}
			cq.distinct(true);
			return null;
		};
	}
}
