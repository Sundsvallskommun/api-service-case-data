package se.sundsvall.casedata.integration.db;

import java.util.List;
import java.util.Optional;

import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import se.sundsvall.casedata.integration.db.model.Errand;

@JaversSpringDataAuditable
public interface ErrandRepository extends JpaRepository<Errand, Long>, JpaSpecificationExecutor<Errand> {

	Page<Errand> findAllByIdIn(List<Long> id, Pageable pageable);

	List<Errand> findAllByErrandNumberStartingWith(String caseTypeAbbreviation);

	Errand findByExternalCaseId(String externalCaseId);

	Optional<Errand> findByErrandNumber(String errandNumber);

}
