package se.sundsvall.casedata.integration.db;

import java.util.List;
import java.util.Optional;

import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import se.sundsvall.casedata.integration.db.model.Errand;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

@JaversSpringDataAuditable
@CircuitBreaker(name = "errandRepository")
public interface ErrandRepository extends JpaRepository<Errand, Long>, JpaSpecificationExecutor<Errand> {

	Page<Errand> findAllByIdInAndMunicipalityIdAndNamespace(final List<Long> id, final String municipalityId, final String namespace, final Pageable pageable);

	List<Errand> findAllByErrandNumberStartingWith(final String caseTypeAbbreviation);

	List<Errand> findAllByMunicipalityIdAndNamespace(final String municipalityId, final String namespace);

	Optional<Errand> findByExternalCaseId(final String externalCaseId);

	Optional<Errand> findByErrandNumber(final String errandNumber);

	Optional<Errand> findByIdAndMunicipalityIdAndNamespace(final Long id, final String municipalityId, final String namespace);

	boolean existsByIdAndMunicipalityIdAndNamespace(final Long id, final String municipalityId, final String namespace);

	void deleteByIdAndMunicipalityIdAndNamespace(final Long id, final String municipalityId, final String namespace);

}
