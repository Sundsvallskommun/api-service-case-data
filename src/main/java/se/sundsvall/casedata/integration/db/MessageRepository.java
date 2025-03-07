package se.sundsvall.casedata.integration.db;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import se.sundsvall.casedata.integration.db.model.MessageEntity;

@CircuitBreaker(name = "messageRepository")
public interface MessageRepository extends JpaRepository<MessageEntity, String> {

	List<MessageEntity> findAllByErrandIdAndMunicipalityIdAndNamespace(final Long errandId, final String municipalityId, final String namespace);

	List<MessageEntity> findAllByErrandIdAndMunicipalityIdAndNamespaceAndInternalFalse(final Long errandId, final String municipalityId, final String namespace);

	Optional<MessageEntity> findByMunicipalityIdAndNamespaceAndErrandIdAndMessageId(final String municipalityId, final String namespace, final Long errandId, final String messageId);
}
