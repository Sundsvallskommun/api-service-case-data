package se.sundsvall.casedata.integration.db;

import java.util.List;
import java.util.Optional;

import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import se.sundsvall.casedata.integration.db.model.AttachmentEntity;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

@JaversSpringDataAuditable
@CircuitBreaker(name = "attachmentRepository")
public interface AttachmentRepository extends JpaRepository<AttachmentEntity, Long>, JpaSpecificationExecutor<AttachmentEntity> {

	List<AttachmentEntity> findAllByErrandNumberAndMunicipalityIdAndNamespace(final String errandNumber, final String municipalityId, final String namespace);

	Optional<AttachmentEntity> findByIdAndMunicipalityIdAndNamespace(final Long id, final String municipalityId, final String namespace);

	boolean existsByIdAndMunicipalityIdAndNamespace(final Long id, final String municipalityId, final String namespace);

	void deleteByIdAndMunicipalityIdAndNamespace(final Long id, final String municipalityId, final String namespace);

}
