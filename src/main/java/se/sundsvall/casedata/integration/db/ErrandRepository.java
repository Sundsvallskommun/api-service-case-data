package se.sundsvall.casedata.integration.db;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import se.sundsvall.casedata.integration.db.model.ErrandEntity;

@JaversSpringDataAuditable
@CircuitBreaker(name = "errandRepository")
public interface ErrandRepository extends JpaRepository<ErrandEntity, Long>, JpaSpecificationExecutor<ErrandEntity> {

	Page<ErrandEntity> findAllByIdInAndMunicipalityIdAndNamespace(final List<Long> id, final String municipalityId, final String namespace, final Pageable pageable);

	List<ErrandEntity> findAllByErrandNumberStartingWith(final String caseTypeAbbreviation);

	List<ErrandEntity> findAllByMunicipalityIdAndNamespace(final String municipalityId, final String namespace);

	Optional<ErrandEntity> findByExternalCaseId(final String externalCaseId);

	Optional<ErrandEntity> findByErrandNumber(final String errandNumber);

	Optional<ErrandEntity> findByIdAndMunicipalityIdAndNamespace(final Long id, final String municipalityId, final String namespace);

	boolean existsByIdAndMunicipalityIdAndNamespace(final Long id, final String municipalityId, final String namespace);

	void deleteByIdAndMunicipalityIdAndNamespace(final Long id, final String municipalityId, final String namespace);

	List<ErrandEntity> findAllBySuspendedToBefore(OffsetDateTime now);

}
