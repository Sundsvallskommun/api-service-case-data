package se.sundsvall.casedata.integration.db;

import java.util.Optional;

import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import se.sundsvall.casedata.integration.db.model.Appeal;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

@JaversSpringDataAuditable
@CircuitBreaker(name = "appealRepository")
public interface AppealRepository extends JpaRepository<Appeal, Long>, JpaSpecificationExecutor<Appeal> {

	Optional<Appeal> findByIdAndMunicipalityIdAndNamespace(final Long id, final String municipalityId, final String namespace);

}
