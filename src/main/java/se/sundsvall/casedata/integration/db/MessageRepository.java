package se.sundsvall.casedata.integration.db;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import se.sundsvall.casedata.integration.db.model.Message;

@CircuitBreaker(name = "messageRepository")
public interface MessageRepository extends JpaRepository<Message, String> {

	List<Message> findAllByErrandNumberAndMunicipalityId(final String errandNumber, final String municipalityId);

	Optional<Message> findByMessageIDAndMunicipalityId(final String messageId, final String municipalityId);

}
