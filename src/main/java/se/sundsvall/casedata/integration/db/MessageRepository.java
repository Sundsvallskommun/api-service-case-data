package se.sundsvall.casedata.integration.db;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import se.sundsvall.casedata.integration.db.model.MessageEntity;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

@CircuitBreaker(name = "messageRepository")
public interface MessageRepository extends JpaRepository<MessageEntity, String> {

	List<MessageEntity> findAllByErrandNumberAndMunicipalityIdAndNamespace(final String errandNumber, final String municipalityId, final String namespace);

	Optional<MessageEntity> findByMessageIdAndMunicipalityIdAndNamespace(final String messageId, final String municipalityId, final String namespace);

}
