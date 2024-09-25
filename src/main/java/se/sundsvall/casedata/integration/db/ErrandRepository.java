package se.sundsvall.casedata.integration.db;

import java.util.List;
import java.util.Optional;

import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import se.sundsvall.casedata.integration.db.model.Errand;

@JaversSpringDataAuditable
@CircuitBreaker(name = "errandRepository")
public interface ErrandRepository extends JpaRepository<Errand, Long>, JpaSpecificationExecutor<Errand> {

	Page<Errand> findAllByIdInAndMunicipalityId(final List<Long> id, final String municipalityId, final Pageable pageable);

	List<Errand> findAllByErrandNumberStartingWith(final String caseTypeAbbreviation);

	List<Errand> findAllByMunicipalityId(final String municipalityId);

	Optional<Errand> findByExternalCaseId(final String externalCaseId);

	Optional<Errand> findByErrandNumber(final String errandNumber);

	Optional<Errand> findByIdAndMunicipalityId(final Long id, final String municipalityId);

	boolean existsByIdAndMunicipalityId(final Long id, final String municipalityId);

	void deleteByIdAndMunicipalityId(final Long id, final String municipalityId);
}
