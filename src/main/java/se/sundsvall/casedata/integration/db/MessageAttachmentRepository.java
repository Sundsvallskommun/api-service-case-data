package se.sundsvall.casedata.integration.db;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import se.sundsvall.casedata.integration.db.model.MessageAttachmentEntity;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

@CircuitBreaker(name = "messageAttachmentRepository")
public interface MessageAttachmentRepository extends JpaRepository<MessageAttachmentEntity, String> {

	Optional<MessageAttachmentEntity> findByAttachmentIdAndMunicipalityIdAndNamespace(final String attachmentId, final String municipalityId, final String namespace);

}
