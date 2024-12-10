package se.sundsvall.casedata.integration.db;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.util.Optional;
import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import se.sundsvall.casedata.integration.db.model.DecisionEntity;

@JaversSpringDataAuditable
@CircuitBreaker(name = "decisionRepository")
public interface DecisionRepository extends JpaRepository<DecisionEntity, Long>, JpaSpecificationExecutor<DecisionEntity> {

	Optional<DecisionEntity> findByIdAndMunicipalityIdAndNamespace(final Long id, final String municipalityId, final String namespace);

}
