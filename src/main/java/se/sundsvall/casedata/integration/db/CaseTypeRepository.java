package se.sundsvall.casedata.integration.db;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.data.jpa.repository.JpaRepository;
import se.sundsvall.casedata.integration.db.model.CaseTypeEntity;

@CircuitBreaker(name = "caseTypeRepository")
public interface CaseTypeRepository extends JpaRepository<CaseTypeEntity, String> {
}
