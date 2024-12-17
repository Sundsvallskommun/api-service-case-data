package se.sundsvall.casedata.integration.db;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.util.Optional;
import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import se.sundsvall.casedata.integration.db.model.StakeholderEntity;

@JaversSpringDataAuditable
@CircuitBreaker(name = "stakeholderRepository")
public interface StakeholderRepository extends JpaRepository<StakeholderEntity, Long>, JpaSpecificationExecutor<StakeholderEntity> {

	Optional<StakeholderEntity> findByIdAndErrandIdAndMunicipalityIdAndNamespace(final Long id, final Long errandId, final String municipalityId, final String namespace);
}
