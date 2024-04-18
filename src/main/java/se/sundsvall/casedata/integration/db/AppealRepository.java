package se.sundsvall.casedata.integration.db;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import se.sundsvall.casedata.integration.db.model.Appeal;

@JaversSpringDataAuditable
@CircuitBreaker(name = "appealRepository")
public interface AppealRepository extends JpaRepository<Appeal, Long>, JpaSpecificationExecutor<Appeal> {

}
