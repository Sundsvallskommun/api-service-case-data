package se.sundsvall.casedata.integration.db;

import java.util.List;
import java.util.Optional;

import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import se.sundsvall.casedata.integration.db.model.Stakeholder;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

@JaversSpringDataAuditable
@CircuitBreaker(name = "stakeholderRepository")
public interface StakeholderRepository extends JpaRepository<Stakeholder, Long>, JpaSpecificationExecutor<Stakeholder> {

	List<Stakeholder> findByRolesAndMunicipalityId(final String stakeholderRole, final String municipalityId);

	Optional<Stakeholder> findByIdAndMunicipalityId(final Long id, final String municipalityId);

	List<Stakeholder> findAllByMunicipalityId(final String municipalityId);
}
