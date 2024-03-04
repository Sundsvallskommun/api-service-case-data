package se.sundsvall.casedata.integration.db;

import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.jpa.repository.JpaRepository;

import se.sundsvall.casedata.integration.db.model.MessageAttachment;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

@JaversSpringDataAuditable
@CircuitBreaker(name = "messageAttachmentRepository")
public interface MessageAttachmentRepository extends JpaRepository<MessageAttachment, String> {

}
