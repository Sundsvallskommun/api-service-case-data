package se.sundsvall.casedata.service;

import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static org.zalando.problem.Status.BAD_REQUEST;
import static org.zalando.problem.Status.NOT_FOUND;
import static se.sundsvall.casedata.integration.db.model.enums.NotificationSubType.ERRAND;
import static se.sundsvall.casedata.integration.db.model.enums.NotificationSubType.SYSTEM;
import static se.sundsvall.casedata.service.NotificationService.EventType.UPDATE;
import static se.sundsvall.casedata.service.util.Constants.CAMUNDA_USER;
import static se.sundsvall.casedata.service.util.Constants.ERRAND_ENTITY_NOT_FOUND;
import static se.sundsvall.casedata.service.util.Constants.NOTIFICATION_ERRAND_UPDATED;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toErrand;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toErrandEntity;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toOwnerId;

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
import se.sundsvall.casedata.service.util.mappers.EntityMapper;
import se.sundsvall.casedata.service.util.mappers.PatchMapper;

@Service
@Transactional
public class ErrandService {

	private final ErrandRepository errandRepository;
	private final ProcessService processService;
	private final NotificationService notificationService;
	private final ApplicationEventPublisher applicationEventPublisher;

	public ErrandService(final ErrandRepository errandRepository, final ProcessService processService, final NotificationService notificationService,
		final ApplicationEventPublisher applicationEventPublisher) {
		this.errandRepository = errandRepository;
		this.processService = processService;
		this.notificationService = notificationService;
		this.applicationEventPublisher = applicationEventPublisher;
	}

	private String determineSubType(final ErrandEntity updatedErrand) {
		var subtype = ERRAND;
		if (CAMUNDA_USER.equals(updatedErrand.getUpdatedByClient())) {
			subtype = SYSTEM;
		}
		return subtype.toString();
	}

	public Errand findByIdAndMunicipalityIdAndNamespace(final Long errandId, final String municipalityId, final String namespace) {
		final var errandEntity = errandRepository.findByIdAndMunicipalityIdAndNamespace(errandId, municipalityId, namespace)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, ERRAND_ENTITY_NOT_FOUND.formatted(errandId, namespace, municipalityId)));
		return toErrand(errandEntity);
	}

	/**
	 * @return Page of Errand without duplicates
	 */
	public Page<Errand> findAll(final Specification<ErrandEntity> specification, final String municipalityId, final String namespace, final Pageable pageable) {
		// Extract all ID's and remove duplicates
		try {
			final List<Long> allIds = errandRepository.findAll(specification).stream()
				.filter(errand -> municipalityId.equals(errand.getMunicipalityId()))
				.filter(errand -> namespace.equals(errand.getNamespace()))
				.map(ErrandEntity::getId)
				.distinct()
				.toList();

			return errandRepository.findAllByIdInAndMunicipalityIdAndNamespace(allIds, municipalityId, namespace, pageable).map(EntityMapper::toErrand);
		} catch (final PropertyReferenceException | PathElementException | InvalidDataAccessApiUsageException e) {
			throw Problem.valueOf(BAD_REQUEST, "Invalid filter parameter: " + e.getMessage());
		}
	}

	/**
	 * Saves errand and update the process in ParkingPermit if it's a parking permit errand
	 */
	public Errand create(final Errand errand, final String municipalityId, final String namespace) {

		final var statuses = Optional.ofNullable(errand.getStatus())
			.map(List::of)
			.orElse(emptyList());

		errand.setStatuses(statuses);

		final var errandEntity = toErrandEntity(errand, municipalityId, namespace);
		final var resultErrand = errandRepository.save(errandEntity);

		// Will not start a process if it's not a parking permit or mex errand
		startProcess(resultErrand);

		return toErrand(resultErrand);
	}

	public void update(final Long errandId, final String municipalityId, final String namespace, final PatchErrand patchErrand) {
		final var oldErrand = findErrandEntity(errandId, municipalityId, namespace);
		final var updatedErrand = errandRepository.save(PatchMapper.patchErrand(oldErrand, patchErrand));

		applicationEventPublisher.publishEvent(updatedErrand);

		// Create notification
		notificationService.create(municipalityId, namespace, Notification.builder()
			.withCreatedBy(updatedErrand.getCreatedBy())
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
		// Extract all ID's and remove duplicates
		try {
			final List<Long> allIds = errandRepository.findAll(specification).stream()
				.filter(errand -> municipalityId.equals(errand.getMunicipalityId()))
				.map(ErrandEntity::getId)
				.distinct()
				.toList();

			return errandRepository.findAllByIdInAndMunicipalityId(allIds, municipalityId, pageable).map(EntityMapper::toErrand);
		} catch (final PropertyReferenceException | PathElementException | InvalidDataAccessApiUsageException e) {
			throw Problem.valueOf(BAD_REQUEST, "Invalid filter parameter: " + e.getMessage());
		}
	}

}
