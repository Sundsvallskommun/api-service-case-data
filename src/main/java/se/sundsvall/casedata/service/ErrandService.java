package se.sundsvall.casedata.service;

import generated.se.sundsvall.relation.Relation;
import generated.se.sundsvall.relation.ResourceIdentifier;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.hibernate.query.sqm.PathElementException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zalando.problem.Problem;
import se.sundsvall.casedata.api.model.Errand;
import se.sundsvall.casedata.api.model.Notification;
import se.sundsvall.casedata.api.model.PatchErrand;
import se.sundsvall.casedata.integration.db.ErrandRepository;
import se.sundsvall.casedata.integration.db.model.ErrandEntity;
import se.sundsvall.casedata.integration.eventlog.EventlogIntegration;
import se.sundsvall.casedata.integration.relation.RelationClient;
import se.sundsvall.casedata.service.model.ReferredFrom;
import se.sundsvall.casedata.service.util.mappers.EntityMapper;
import se.sundsvall.casedata.service.util.mappers.PatchMapper;

import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.zalando.problem.Status.BAD_REQUEST;
import static org.zalando.problem.Status.NOT_FOUND;
import static se.sundsvall.casedata.integration.db.model.enums.NotificationSubType.ERRAND;
import static se.sundsvall.casedata.integration.db.model.enums.NotificationSubType.SYSTEM;
import static se.sundsvall.casedata.integration.db.specification.ErrandEntitySpecification.buildMunicipalityIdFilter;
import static se.sundsvall.casedata.integration.db.specification.ErrandEntitySpecification.buildNamespaceFilter;
import static se.sundsvall.casedata.integration.db.specification.ErrandEntitySpecification.distinct;
import static se.sundsvall.casedata.service.model.EventType.UPDATE;
import static se.sundsvall.casedata.service.util.Constants.CAMUNDA_USERS;
import static se.sundsvall.casedata.service.util.Constants.ERRAND_ENTITY_NOT_FOUND;
import static se.sundsvall.casedata.service.util.Constants.NOTIFICATION_ERRAND_UPDATED;
import static se.sundsvall.casedata.service.util.ServiceUtil.getAdUser;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toErrand;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toErrandEntity;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toOwnerId;

@Service
@Transactional
public class ErrandService {

	private static final String REFERRED_FROM_RELATION_TYPE = "REFERRED_FROM";
	private static final String REFERRED_FROM_RESOURCE_IDENTIFIER_TYPE = "case";
	private static final String REFERRED_FROM_RESOURCE_IDENTIFIER_SERVICE = "case-data";

	private final ErrandRepository errandRepository;
	private final ProcessService processService;
	private final NotificationService notificationService;
	private final ApplicationEventPublisher applicationEventPublisher;
	private final EventlogIntegration eventlogIntegration;
	private final RelationClient relationClient;

	public ErrandService(final ErrandRepository errandRepository,
		final ProcessService processService,
		final NotificationService notificationService,
		final ApplicationEventPublisher applicationEventPublisher,
		final EventlogIntegration eventlogIntegration,
		final RelationClient relationClient) {
		this.errandRepository = errandRepository;
		this.processService = processService;
		this.notificationService = notificationService;
		this.applicationEventPublisher = applicationEventPublisher;
		this.eventlogIntegration = eventlogIntegration;
		this.relationClient = relationClient;
	}

	private String determineSubType(final ErrandEntity updatedErrand) {
		var subtype = ERRAND;
		if (CAMUNDA_USERS.contains(Optional.ofNullable(updatedErrand.getUpdatedByClient()).orElse(""))) {
			subtype = SYSTEM;
		}
		return subtype.toString();
	}

	public Errand findByIdAndMunicipalityIdAndNamespace(final Long errandId, final String municipalityId, final String namespace) {
		final var errandEntity = errandRepository.findByIdAndMunicipalityIdAndNamespace(errandId, municipalityId, namespace)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, ERRAND_ENTITY_NOT_FOUND.formatted(errandId, namespace, municipalityId)));
		return toErrand(errandEntity);
	}

	public Page<Errand> findAll(final Specification<ErrandEntity> specification, final String municipalityId, final String namespace, final Pageable pageable) {
		try {
			return errandRepository.findAll(Specification
				.allOf(buildMunicipalityIdFilter(municipalityId)
					.and(buildNamespaceFilter(namespace))
					.and(specification)
					.and(distinct())), pageable)
				.map(EntityMapper::toErrand);
		} catch (final PropertyReferenceException | PathElementException | InvalidDataAccessApiUsageException e) {
			throw Problem.valueOf(BAD_REQUEST, "Invalid filter parameter: " + e.getMessage());
		}
	}

	/**
	 * Saves an errand and update the process in ParkingPermit if it's a parking permit errand
	 */
	public Errand create(final Errand errand, final String municipalityId, final String namespace, final String referredFrom) {

		final var statuses = Optional.ofNullable(errand.getStatus())
			.map(List::of)
			.orElse(emptyList());

		errand.setStatuses(statuses);

		final var errandEntity = toErrandEntity(errand, municipalityId, namespace);
		final var resultErrand = errandRepository.save(errandEntity);

		// Will not start a process if it's not a parking permit or mex errand
		startProcess(resultErrand);

		if (isNotBlank(referredFrom)) {
			final var expandedReferredFrom = expandReferredFrom(referredFrom);

			// Make sure namespaces match
			if (!namespace.equalsIgnoreCase(expandedReferredFrom.namespace())) {
				throw Problem.valueOf(BAD_REQUEST, "Mismatch on namespace and referred-from namespace");
			}

			final var relation = new Relation()
				.type(REFERRED_FROM_RELATION_TYPE)
				.source(new ResourceIdentifier()
					.resourceId(expandedReferredFrom.identifier())
					.type(REFERRED_FROM_RESOURCE_IDENTIFIER_TYPE)
					.service(expandedReferredFrom.service())
					.namespace(namespace))
				.target(new ResourceIdentifier()
					.resourceId(resultErrand.getId().toString())
					.type(REFERRED_FROM_RESOURCE_IDENTIFIER_TYPE)
					.service(REFERRED_FROM_RESOURCE_IDENTIFIER_SERVICE)
					.namespace(namespace));

			relationClient.createRelation(municipalityId, relation);
		}

		return toErrand(resultErrand);
	}

	public void update(final Long errandId, final String municipalityId, final String namespace, final PatchErrand patchErrand) {
		final var oldErrand = findErrandEntity(errandId, municipalityId, namespace);
		final var updatedErrand = errandRepository.saveAndFlush(PatchMapper.patchErrand(oldErrand, patchErrand));

		applicationEventPublisher.publishEvent(updatedErrand);

		if (patchErrand.getStatus() != null) {
			eventlogIntegration.sendEventlogEvent(municipalityId, updatedErrand, patchErrand.getStatus());
		}

		// Create notification
		notificationService.create(municipalityId, namespace, Notification.builder()
			.withCreatedBy(getAdUser())
			.withDescription(NOTIFICATION_ERRAND_UPDATED)
			.withErrandId(updatedErrand.getId())
			.withType(UPDATE.toString())
			.withSubType(determineSubType(updatedErrand))
			.withOwnerId(toOwnerId(updatedErrand))
			.build(), updatedErrand);
	}

	public void delete(final Long errandId, final String municipalityId, final String namespace) {
		errandRepository.delete(findErrandEntity(errandId, municipalityId, namespace));
	}

	private ErrandEntity findErrandEntity(final Long errandId, final String municipalityId, final String namespace) {
		return errandRepository.findWithPessimisticLockingByIdAndMunicipalityIdAndNamespace(errandId, municipalityId, namespace)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, ERRAND_ENTITY_NOT_FOUND.formatted(errandId, namespace, municipalityId)));
	}

	private void startProcess(final ErrandEntity errand) {
		try {
			final var startProcessId = processService.startProcess(errand);
			if (!isNull(startProcessId)) {
				errand.setProcessId(startProcessId);
				errandRepository.save(errand);
			}

		} catch (final Exception e) {
			errandRepository.delete(errand);
			throw e;
		}
	}

	public Page<Errand> findAllWithoutNamespace(final Specification<ErrandEntity> specification, final String municipalityId, final Pageable pageable) {
		try {
			return errandRepository.findAll(Specification
				.allOf(buildMunicipalityIdFilter(municipalityId)
					.and(specification)
					.and(distinct())), pageable)
				.map(EntityMapper::toErrand);
		} catch (final PropertyReferenceException | PathElementException | InvalidDataAccessApiUsageException e) {
			throw Problem.valueOf(BAD_REQUEST, "Invalid filter parameter: " + e.getMessage());
		}
	}

	ReferredFrom expandReferredFrom(final String referredFromAsString) {
		if (isNotBlank(referredFromAsString)) {
			var parts = referredFromAsString.split(",");
			if (parts.length == 3 && Arrays.stream(parts).map(String::trim).noneMatch(String::isBlank)) {
				return new ReferredFrom(parts[0].trim(), parts[1].trim(), parts[2].trim());
			}
		}

		throw Problem.valueOf(BAD_REQUEST, "Referred from should be three non-blank comma-separated parts: <service>,<namespace>,<identifier>");
	}
}
