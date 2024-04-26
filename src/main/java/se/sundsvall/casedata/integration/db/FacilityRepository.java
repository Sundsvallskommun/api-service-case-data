package se.sundsvall.casedata.integration.db;

import java.util.Optional;

import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import se.sundsvall.casedata.integration.db.model.Facility;

@JaversSpringDataAuditable
@CircuitBreaker(name = "facilityRepository")
public interface FacilityRepository extends JpaRepository<Facility, Long>, JpaSpecificationExecutor<Facility> {

	Optional<Facility> findByIdAndErrandId(Long id, Long errandId);
}
