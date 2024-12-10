package se.sundsvall.casedata.integration.db;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.util.Optional;
import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import se.sundsvall.casedata.integration.db.model.FacilityEntity;

@JaversSpringDataAuditable
@CircuitBreaker(name = "facilityRepository")
public interface FacilityRepository extends JpaRepository<FacilityEntity, Long>, JpaSpecificationExecutor<FacilityEntity> {

	Optional<FacilityEntity> findByIdAndErrandIdAndMunicipalityIdAndNamespace(final Long id, final Long errandId, final String municipalityId, final String namespace);

	Optional<FacilityEntity> findByIdAndMunicipalityIdAndNamespace(final Long id, final String municipalityId, final String namespace);

}
