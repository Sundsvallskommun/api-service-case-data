package se.sundsvall.casedata.integration.db;

import java.util.Optional;

import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import se.sundsvall.casedata.integration.db.model.Note;

@JaversSpringDataAuditable
@CircuitBreaker(name = "noteRepository")
public interface NoteRepository extends JpaRepository<Note, Long>, JpaSpecificationExecutor<Note> {

	Optional<Note> findByIdAndMunicipalityId(final Long id, final String municipalityId);
}
