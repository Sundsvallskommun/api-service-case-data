package se.sundsvall.casedata.integration.db;

import java.util.Optional;

import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import se.sundsvall.casedata.integration.db.model.Facility;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

@JaversSpringDataAuditable
@CircuitBreaker(name = "facilityRepository")
public interface FacilityRepository extends JpaRepository<Facility, Long>, JpaSpecificationExecutor<Facility> {

	Optional<Facility> findByIdAndErrandIdAndMunicipalityIdAndNamespace(final Long id, final Long errandId, final String municipalityId, final String namespace);

	Optional<Facility> findByIdAndMunicipalityIdAndNamespace(final Long id, final String municipalityId, final String namespace);

}
