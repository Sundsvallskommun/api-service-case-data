package se.sundsvall.casedata.integration.db;

import java.util.List;

import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import se.sundsvall.casedata.integration.db.model.Stakeholder;
import se.sundsvall.casedata.integration.db.model.enums.StakeholderRole;

@JaversSpringDataAuditable
public interface StakeholderRepository extends JpaRepository<Stakeholder, Long>, JpaSpecificationExecutor<Stakeholder> {

	List<Stakeholder> findByRoles(StakeholderRole stakeholderRole);

}
