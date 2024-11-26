package se.sundsvall.casedata.integration.db;

import java.util.Optional;

import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import se.sundsvall.casedata.integration.db.model.StakeholderEntity;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

@JaversSpringDataAuditable
@CircuitBreaker(name = "stakeholderRepository")
public interface StakeholderRepository extends JpaRepository<StakeholderEntity, Long>, JpaSpecificationExecutor<StakeholderEntity> {

	Optional<StakeholderEntity> findByIdAndMunicipalityIdAndNamespace(final Long id, final String municipalityId, final String namespace);

}
