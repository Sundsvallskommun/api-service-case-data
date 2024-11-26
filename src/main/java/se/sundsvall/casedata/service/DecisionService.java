package se.sundsvall.casedata.service;

import static java.text.MessageFormat.format;
import static org.zalando.problem.Status.BAD_REQUEST;
import static org.zalando.problem.Status.NOT_FOUND;
import static se.sundsvall.casedata.service.NotificationService.EventType.UPDATE;
import static se.sundsvall.casedata.service.util.Constants.ERRAND_WAS_NOT_FOUND;
import static se.sundsvall.casedata.service.util.Constants.NOTIFICATION_DECISION_CREATED;
import static se.sundsvall.casedata.service.util.Constants.NOTIFICATION_DECISION_UPDATED;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toDecision;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toDecisionEntity;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toOwnerId;
import static se.sundsvall.casedata.service.util.mappers.PatchMapper.patchDecision;
import static se.sundsvall.casedata.service.util.mappers.PutMapper.putDecision;

import io.github.resilience4j.retry.annotation.Retry;
import java.util.List;
import org.springframework.stereotype.Service;
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

	private static final String DECISION_WAS_NOT_FOUND_ON_ERRAND_WITH_ID = "Decision was not found on errand with id: {0}";

	private static final String DECISION_WITH_ID_X_WAS_NOT_FOUND_ON_ERRAND_WITH_ID_X = "Decision with id: {0} was not found on errand with id: {1}";

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

	public List<Decision> findDecisionsOnErrand(final Long errandId, final String municipalityId, final String namespace) {
		final var decisionList = getErrandByIdAndMunicipalityIdAndNamespace(errandId, municipalityId, namespace).getDecisions();
		if ((decisionList == null) || decisionList.isEmpty()) {
			throw Problem.valueOf(NOT_FOUND, format(DECISION_WAS_NOT_FOUND_ON_ERRAND_WITH_ID, errandId));
		}

		return decisionList.stream().map(EntityMapper::toDecision).toList();
	}

	public Decision findDecisionOnErrand(final Long errandId, final Long id, final String municipalityId, final String namespace) {
		final var decisionList = getErrandByIdAndMunicipalityIdAndNamespace(errandId, municipalityId, namespace).getDecisions();

		return toDecision(decisionList.stream()
			.filter(decision -> decision.getId().equals(id))
			.findFirst()
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, format(DECISION_WITH_ID_X_WAS_NOT_FOUND_ON_ERRAND_WITH_ID_X, id, errandId))));
	}

	@Retry(name = "OptimisticLocking")
	public void replaceDecisionOnErrand(final Long errandId, final Long id, final String municipalityId, final String namespace, final Decision decision) {
		final var decisionList = getErrandByIdAndMunicipalityIdAndNamespace(errandId, municipalityId, namespace).getDecisions();
		final var entity = decisionList.stream()
			.filter(decisionEntity -> decisionEntity.getId().equals(id))
			.findFirst()
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, format(DECISION_WITH_ID_X_WAS_NOT_FOUND_ON_ERRAND_WITH_ID_X, id, errandId)));

		decisionRepository.save(putDecision(entity, decision));

		// Create notification
		notificationService.createNotification(municipalityId, namespace, Notification.builder()
			.withCreatedBy(entity.getErrand().getCreatedBy())
			.withDescription(NOTIFICATION_DECISION_UPDATED)
			.withErrandId(entity.getErrand().getId())
			.withType(UPDATE.toString())
			.withOwnerId(toOwnerId(entity.getErrand()))
			.build());
	}

	@Retry(name = "OptimisticLocking")
	public void updateDecisionOnErrand(final Long errandId, final Long id, final String municipalityId, final String namespace, final PatchDecision decision) {
		final var errand = getErrandByIdAndMunicipalityIdAndNamespace(errandId, municipalityId, namespace);
		final var decisionList = errand.getDecisions();

		final var entity = decisionList.stream()
			.filter(decisionEntity -> id.equals(decisionEntity.getId()))
			.findFirst()
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, format(DECISION_WITH_ID_X_WAS_NOT_FOUND_ON_ERRAND_WITH_ID_X, id, errandId)));

		if (decisionList.stream()
			.anyMatch(decisionEntity -> decisionEntity.getDecisionType().equals(decision.getDecisionType()) && !decisionEntity.getId().equals(id))) {
			throw Problem.valueOf(BAD_REQUEST, format("Decision with type: {0} already exists on errand with id: {1}", decision.getDecisionType(), errandId));
		}

		decisionRepository.save(patchDecision(entity, decision));

		// Create notification
		notificationService.createNotification(municipalityId, namespace, Notification.builder()
			.withCreatedBy(errand.getCreatedBy())
			.withDescription(NOTIFICATION_DECISION_UPDATED)
			.withErrandId(errand.getId())
			.withType(UPDATE.toString())
			.withOwnerId(toOwnerId(errand))
			.build());
	}

	@Retry(name = "OptimisticLocking")
	public Decision addDecisionToErrand(final Long errandId, final String municipalityId, final String namespace, final Decision decision) {

		final var errandEntity = getErrandByIdAndMunicipalityIdAndNamespace(errandId, municipalityId, namespace);
		final var decisionEntity = toDecisionEntity(decision, errandEntity, municipalityId, namespace);
		errandEntity.getDecisions().add(decisionEntity);
		final var updatedErrand = errandRepository.save(errandEntity);
		processService.updateProcess(updatedErrand);

		// Create notification
		notificationService.createNotification(municipalityId, namespace, Notification.builder()
			.withCreatedBy(errandEntity.getCreatedBy())
			.withDescription(NOTIFICATION_DECISION_CREATED)
			.withErrandId(errandEntity.getId())
			.withType(UPDATE.toString())
			.withOwnerId(toOwnerId(errandEntity))
			.build());

		return toDecision(decisionEntity);
	}

	@Retry(name = "OptimisticLocking")
	public void deleteDecisionOnErrand(final Long errandId, final String municipalityId, final String namespace, final Long decisionId) {
		final var errand = getErrandByIdAndMunicipalityIdAndNamespace(errandId, municipalityId, namespace);
		final var decisionToRemove = errand.getDecisions().stream()
			.filter(decision -> decision.getId().equals(decisionId))
			.findAny()
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, format(DECISION_WITH_ID_X_WAS_NOT_FOUND_ON_ERRAND_WITH_ID_X, decisionId, errandId)));
		errand.getDecisions().remove(decisionToRemove);
		errandRepository.save(errand);
		processService.updateProcess(errand);
	}

	public ErrandEntity getErrandByIdAndMunicipalityIdAndNamespace(final Long errandId, final String municipalityId, final String namespace) {
		return errandRepository.findByIdAndMunicipalityIdAndNamespace(errandId, municipalityId, namespace)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND,
				format(ERRAND_WAS_NOT_FOUND, errandId)));
	}
}
