package se.sundsvall.casedata.integration.db;

import java.util.List;
import java.util.Optional;

import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import se.sundsvall.casedata.integration.db.model.Attachment;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

@JaversSpringDataAuditable
@CircuitBreaker(name = "attachmentRepository")
public interface AttachmentRepository extends JpaRepository<Attachment, Long>, JpaSpecificationExecutor<Attachment> {

	List<Attachment> findAllByErrandNumberAndMunicipalityId(final String errandNumber, final String municipalityId);

	Optional<Attachment> findByIdAndMunicipalityId(final Long id, final String municipalityId);

	boolean existsByIdAndMunicipalityId(final Long id, final String municipalityId);

	void deleteByIdAndMunicipalityId(final Long id, final String municipalityId);
}
