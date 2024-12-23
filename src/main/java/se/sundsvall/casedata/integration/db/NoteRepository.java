package se.sundsvall.casedata.integration.db;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.util.Optional;
import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import se.sundsvall.casedata.integration.db.model.NoteEntity;

@JaversSpringDataAuditable
@CircuitBreaker(name = "noteRepository")
public interface NoteRepository extends JpaRepository<NoteEntity, Long>, JpaSpecificationExecutor<NoteEntity> {

	Optional<NoteEntity> findByIdAndErrandIdAndMunicipalityIdAndNamespace(final Long id, Long errandId, final String municipalityId, final String namespace);
}
