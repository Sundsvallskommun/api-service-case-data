package se.sundsvall.casedata.service;

import static org.zalando.problem.Status.BAD_REQUEST;
import static org.zalando.problem.Status.NOT_FOUND;
import static se.sundsvall.casedata.integration.db.model.enums.NotificationSubType.DECISION;
import static se.sundsvall.casedata.service.NotificationService.EventType.UPDATE;
import static se.sundsvall.casedata.service.util.Constants.ERRAND_ENTITY_NOT_FOUND;
import static se.sundsvall.casedata.service.util.Constants.NOTIFICATION_DECISION_CREATED;
import static se.sundsvall.casedata.service.util.Constants.NOTIFICATION_DECISION_UPDATED;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toDecision;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toDecisionEntity;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toOwnerId;
import static se.sundsvall.casedata.service.util.mappers.PatchMapper.patchDecision;
import static se.sundsvall.casedata.service.util.mappers.PutMapper.putDecision;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zalando.problem.Problem;
import se.sundsvall.casedata.api.model.Decision;
import se.sundsvall.casedata.api.model.Notification;
import se.sundsvall.casedata.api.model.PatchDecision;
import se.sundsvall.casedata.integration.db.DecisionRepository;
import se.sundsvall.casedata.integration.db.ErrandRepository;
import se.sundsvall.casedata.integration.db.model.ErrandEntity;
import se.sundsvall.casedata.service.util.mappers.EntityMapper;

@Service
public class DecisionService {

	private static final String DECISION_WAS_NOT_FOUND_ON_ERRAND_WITH_ID = "Decision was not found on errand with id: %s";
	private static final String DECISION_WITH_ID_X_WAS_NOT_FOUND_ON_ERRAND_WITH_ID_X = "Decision with id: %s was not found on errand with id: %s";

	private final DecisionRepository decisionRepository;
	private final ErrandRepository errandRepository;
	private final ProcessService processService;
	private final NotificationService notificationService;

	public DecisionService(final DecisionRepository decisionRepository, final ErrandRepository errandRepository, final ProcessService processService, final NotificationService notificationService) {
		this.decisionRepository = decisionRepository;
		this.errandRepository = errandRepository;
		this.processService = processService;
		this.notificationService = notificationService;
	}

	public List<Decision> findDecisions(final Long errandId, final String municipalityId, final String namespace) {
		final var decisionList = findErrandEntity(errandId, municipalityId, namespace, false).getDecisions();
		if ((decisionList == null) || decisionList.isEmpty()) {
			throw Problem.valueOf(NOT_FOUND, DECISION_WAS_NOT_FOUND_ON_ERRAND_WITH_ID.formatted(errandId));
		}

		return decisionList.stream().map(EntityMapper::toDecision).toList();
	}

	public Decision findDecision(final Long errandId, final Long id, final String municipalityId, final String namespace) {
		final var decisionList = findErrandEntity(errandId, municipalityId, namespace, false).getDecisions();

		return toDecision(decisionList.stream()
			.filter(decision -> decision.getId().equals(id))
			.findFirst()
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, DECISION_WITH_ID_X_WAS_NOT_FOUND_ON_ERRAND_WITH_ID_X.formatted(id, errandId))));
	}

	@Transactional
	public void replaceOnErrand(final Long errandId, final Long id, final String municipalityId, final String namespace, final Decision decision) {
		final var errandEntity = findErrandEntity(errandId, municipalityId, namespace, true);
		final var decisionList = errandEntity.getDecisions();
		final var entity = decisionList.stream()
			.filter(decisionEntity -> decisionEntity.getId().equals(id))
			.findFirst()
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, DECISION_WITH_ID_X_WAS_NOT_FOUND_ON_ERRAND_WITH_ID_X.formatted(id, errandId)));

		decisionRepository.save(putDecision(entity, decision));

		// Create notification
		notificationService.create(municipalityId, namespace, Notification.builder()
			.withCreatedBy(entity.getErrand().getCreatedBy())
			.withDescription(NOTIFICATION_DECISION_UPDATED)
			.withErrandId(entity.getErrand().getId())
			.withType(UPDATE.toString())
			.withSubType(DECISION.toString())
			.withOwnerId(toOwnerId(entity.getErrand()))
			.build(), errandEntity);
	}

	@Transactional
	public void update(final Long errandId, final Long id, final String municipalityId, final String namespace, final PatchDecision decision) {
		final var errand = findErrandEntity(errandId, municipalityId, namespace, true);
		final var decisionList = errand.getDecisions();

		final var entity = decisionList.stream()
			.filter(decisionEntity -> id.equals(decisionEntity.getId()))
			.findFirst()
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, DECISION_WITH_ID_X_WAS_NOT_FOUND_ON_ERRAND_WITH_ID_X.formatted(id, errandId)));

		if (decisionList.stream()
			.anyMatch(decisionEntity -> decisionEntity.getDecisionType().equals(decision.getDecisionType()) && !decisionEntity.getId().equals(id))) {
			throw Problem.valueOf(BAD_REQUEST, "Decision with type: %s already exists on errand with id: %s".formatted(decision.getDecisionType(), errandId));
		}

		decisionRepository.save(patchDecision(entity, decision));

		// Create notification
		notificationService.create(municipalityId, namespace, Notification.builder()
			.withCreatedBy(errand.getCreatedBy())
			.withDescription(NOTIFICATION_DECISION_UPDATED)
			.withErrandId(errand.getId())
			.withType(UPDATE.toString())
			.withSubType(DECISION.toString())
			.withOwnerId(toOwnerId(errand))
			.build(), errand);
	}

	public Decision addToErrand(final Long errandId, final String municipalityId, final String namespace, final Decision decision) {

		final var errandEntity = findErrandEntity(errandId, municipalityId, namespace, true);
		final var decisionEntity = toDecisionEntity(decision, errandEntity, municipalityId, namespace);
		errandEntity.getDecisions().add(decisionEntity);
		final var updatedErrand = errandRepository.save(errandEntity);
		processService.updateProcess(updatedErrand);

		// Create notification
		notificationService.create(municipalityId, namespace, Notification.builder()
			.withCreatedBy(errandEntity.getCreatedBy())
			.withDescription(NOTIFICATION_DECISION_CREATED)
			.withErrandId(errandEntity.getId())
			.withType(UPDATE.toString())
			.withSubType(DECISION.toString())
			.withOwnerId(toOwnerId(errandEntity))
			.build(), errandEntity);

		return toDecision(decisionEntity);
	}

	public void delete(final Long errandId, final String municipalityId, final String namespace, final Long decisionId) {
		final var errand = findErrandEntity(errandId, municipalityId, namespace, true);
		final var decisionToRemove = errand.getDecisions().stream()
			.filter(decision -> decision.getId().equals(decisionId))
			.findAny()
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, DECISION_WITH_ID_X_WAS_NOT_FOUND_ON_ERRAND_WITH_ID_X.formatted(decisionId, errandId)));
		errand.getDecisions().remove(decisionToRemove);
		errandRepository.save(errand);
		processService.updateProcess(errand);
	}

	private ErrandEntity findErrandEntity(final Long errandId, final String municipalityId, final String namespace, boolean locking) {
		if (locking) {
			return errandRepository.findWithPessimisticLockingByIdAndMunicipalityIdAndNamespace(errandId, municipalityId, namespace)
				.orElseThrow(() -> Problem.valueOf(NOT_FOUND, ERRAND_ENTITY_NOT_FOUND.formatted(errandId, namespace, municipalityId)));
		} else {
			return errandRepository.findByIdAndMunicipalityIdAndNamespace(errandId, municipalityId, namespace)
				.orElseThrow(() -> Problem.valueOf(NOT_FOUND, ERRAND_ENTITY_NOT_FOUND.formatted(errandId, namespace, municipalityId)));
		}
	}
}
