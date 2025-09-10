package se.sundsvall.casedata.integration.db;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import se.sundsvall.casedata.integration.db.model.CaseTypeEntity;

@CircuitBreaker(name = "caseTypeRepository")
public interface CaseTypeRepository extends JpaRepository<CaseTypeEntity, String> {

	List<CaseTypeEntity> findAllByMunicipalityIdAndNamespace(String municipalityId, String namespace);

	CaseTypeEntity findByMunicipalityIdAndNamespaceAndType(String municipalityId, String namespace, String type);
}
