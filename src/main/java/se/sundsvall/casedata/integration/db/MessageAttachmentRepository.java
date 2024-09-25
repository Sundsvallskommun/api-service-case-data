package se.sundsvall.casedata.integration.db;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import se.sundsvall.casedata.integration.db.model.MessageAttachment;

@CircuitBreaker(name = "messageAttachmentRepository")
public interface MessageAttachmentRepository extends JpaRepository<MessageAttachment, String> {

	Optional<MessageAttachment> findByAttachmentIDAndMunicipalityId(final String attachmentId, final String municipalityId);
}
