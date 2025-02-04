package se.sundsvall.casedata.service;

import static java.util.Collections.emptyList;
import static org.zalando.problem.Status.NOT_FOUND;
import static se.sundsvall.casedata.service.util.Constants.ERRAND_ENTITY_NOT_FOUND;
import static se.sundsvall.casedata.service.util.Constants.STAKEHOLDER_WITH_ID_X_WAS_NOT_FOUND_ON_ERRAND_WITH_ID_X;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toStakeholder;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toStakeholderEntity;
import static se.sundsvall.casedata.service.util.mappers.PatchMapper.patchStakeholder;
import static se.sundsvall.casedata.service.util.mappers.PutMapper.putStakeholder;

import io.github.resilience4j.retry.annotation.Retry;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;
import se.sundsvall.casedata.api.model.Stakeholder;
import se.sundsvall.casedata.integration.db.ErrandRepository;
import se.sundsvall.casedata.integration.db.StakeholderRepository;
import se.sundsvall.casedata.integration.db.model.ErrandEntity;
import se.sundsvall.casedata.integration.db.model.StakeholderEntity;
import se.sundsvall.casedata.service.util.mappers.EntityMapper;

@Service
@Transactional
public class StakeholderService {

	private final StakeholderRepository stakeholderRepository;
	private final ErrandRepository errandRepository;
	private final ProcessService processService;

	public StakeholderService(final StakeholderRepository stakeholderRepository, final ErrandRepository errandRepository, final ProcessService processService) {
		this.stakeholderRepository = stakeholderRepository;
		this.errandRepository = errandRepository;
		this.processService = processService;
	}

	public List<Stakeholder> findStakeholders(final Long errandId, final String municipalityId, final String namespace) {
		final var errand = findErrandEntity(errandId, municipalityId, namespace);

		final List<StakeholderEntity> stakeholderList = Optional.ofNullable(errand.getStakeholders()).orElse(emptyList());
		if (stakeholderList.isEmpty()) {
			return emptyList();
		}
		return stakeholderList.stream().map(EntityMapper::toStakeholder).toList();
	}

	public List<Stakeholder> findStakeholdersByRole(final Long errandId, final String stakeholderRole, final String municipalityId, final String namespace) {

		final var errand = findErrandEntity(errandId, municipalityId, namespace);

		final List<StakeholderEntity> stakeholderList = Optional.ofNullable(errand.getStakeholders())
			.orElse(emptyList())
			.stream()
			.filter(stakeholder -> stakeholder.getRoles().contains(stakeholderRole))
			.toList();

		if (stakeholderList.isEmpty()) {
			return emptyList();
		}

		return stakeholderList.stream().map(EntityMapper::toStakeholder).toList();
	}

	public Stakeholder findStakeholder(final Long errandId, final Long id, final String municipalityId, final String namespace) {
		final var errand = findErrandEntity(errandId, municipalityId, namespace);

		return toStakeholder(Optional.ofNullable(errand.getStakeholders())
			.orElse(emptyList()).stream()
			.filter(stakeholder -> stakeholder.getId().equals(id))
			.findFirst()
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, STAKEHOLDER_WITH_ID_X_WAS_NOT_FOUND_ON_ERRAND_WITH_ID_X.formatted(id, errandId))));
	}

	@Retry(name = "OptimisticLocking")
	public void update(final Long errandId, final Long stakeholderId, final String municipalityId, final String namespace, final Stakeholder stakeholder) {
		final var errand = findErrandEntity(errandId, municipalityId, namespace);
		final var entity = Optional.ofNullable(errand.getStakeholders())
			.orElse(emptyList()).stream()
			.filter(stakeholderEntity -> stakeholderEntity.getId().equals(stakeholderId))
			.findFirst()
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, STAKEHOLDER_WITH_ID_X_WAS_NOT_FOUND_ON_ERRAND_WITH_ID_X.formatted(stakeholderId, errandId)));

		patchStakeholder(entity, stakeholder);
		stakeholderRepository.save(entity);
	}

	@Retry(name = "OptimisticLocking")
	public void replaceOnErrand(final Long errandId, final Long id, final String municipalityId, final String namespace, final Stakeholder stakeholder) {
		final var errand = findErrandEntity(errandId, municipalityId, namespace);

		final var entity = Optional.ofNullable(errand.getStakeholders())
			.orElse(emptyList()).stream()
			.filter(stakeholderEntity -> stakeholderEntity.getId().equals(id))
			.findFirst()
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, STAKEHOLDER_WITH_ID_X_WAS_NOT_FOUND_ON_ERRAND_WITH_ID_X.formatted(id, errandId)));

		putStakeholder(entity, stakeholder);
		stakeholderRepository.save(entity);
	}

	@Retry(name = "OptimisticLocking")
	public void delete(final Long errandId, final String municipalityId, final String namespace, final Long stakeholderId) {
		final var errand = findErrandEntity(errandId, municipalityId, namespace);

		final var stakeholderToRemove = Optional.ofNullable(errand.getStakeholders())
			.orElse(emptyList()).stream()
			.filter(stakeholder -> stakeholder.getId().equals(stakeholderId))
			.findFirst().orElseThrow(() -> Problem.valueOf(NOT_FOUND, STAKEHOLDER_WITH_ID_X_WAS_NOT_FOUND_ON_ERRAND_WITH_ID_X.formatted(stakeholderId, errandId)));

		errand.getStakeholders().remove(stakeholderToRemove);
		errandRepository.save(errand);
		processService.updateProcess(errand);
	}

	@Retry(name = "OptimisticLocking")
	public Stakeholder addToErrand(final Long errandId, final String municipalityId, final String namespace, final Stakeholder stakeholder) {
		final var oldErrand = findErrandEntity(errandId, municipalityId, namespace);
		final var stakeholderEntity = toStakeholderEntity(stakeholder, municipalityId, namespace);

		stakeholderEntity.setErrand(oldErrand);
		oldErrand.getStakeholders().add(stakeholderEntity);
		final var updatedErrand = errandRepository.save(oldErrand);
		processService.updateProcess(updatedErrand);
		return toStakeholder(stakeholderEntity);
	}

	@Retry(name = "OptimisticLocking")
	public void replaceOnErrand(final Long errandId, final String municipalityId, final String namespace, final List<Stakeholder> stakeholders) {
		final var oldErrand = findErrandEntity(errandId, municipalityId, namespace);
		oldErrand.getStakeholders().clear();

		stakeholders.stream()
			.map(stakeholder -> toStakeholderEntity(stakeholder, municipalityId, namespace))
			.toList()
			.forEach(stakeholder -> {
				stakeholder.setErrand(oldErrand);
				oldErrand.getStakeholders().add(stakeholder);
			});

		final var updatedErrand = errandRepository.save(oldErrand);
		processService.updateProcess(updatedErrand);
	}

	private ErrandEntity findErrandEntity(final Long errandId, final String municipalityId, final String namespace) {
		return errandRepository.findByIdAndMunicipalityIdAndNamespace(errandId, municipalityId, namespace)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, ERRAND_ENTITY_NOT_FOUND.formatted(errandId, namespace, municipalityId)));
	}
}
