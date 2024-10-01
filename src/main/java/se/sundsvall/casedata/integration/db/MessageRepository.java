package se.sundsvall.casedata.integration.db;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import se.sundsvall.casedata.integration.db.model.Message;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

@CircuitBreaker(name = "messageRepository")
public interface MessageRepository extends JpaRepository<Message, String> {

	List<Message> findAllByErrandNumberAndMunicipalityIdAndNamespace(final String errandNumber, final String municipalityId, final String namespace);

	Optional<Message> findByMessageIDAndMunicipalityIdAndNamespace(final String messageId, final String municipalityId, final String namespace);

}
