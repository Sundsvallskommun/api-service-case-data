package se.sundsvall.casedata.service;

import io.github.resilience4j.retry.annotation.Retry;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;
import se.sundsvall.casedata.api.model.Errand;
import se.sundsvall.casedata.api.model.Notification;
import se.sundsvall.casedata.api.model.PatchErrand;
import se.sundsvall.casedata.integration.db.ErrandRepository;
import se.sundsvall.casedata.integration.db.model.ErrandEntity;
import se.sundsvall.casedata.service.util.mappers.EntityMapper;
import se.sundsvall.casedata.service.util.mappers.PatchMapper;

import java.util.List;

import static java.text.MessageFormat.format;
import static java.util.Objects.isNull;
import static org.zalando.problem.Status.NOT_FOUND;
import static se.sundsvall.casedata.service.NotificationService.EventType.CREATE;
import static se.sundsvall.casedata.service.NotificationService.EventType.UPDATE;
import static se.sundsvall.casedata.service.util.Constants.NOTIFICATION_ERRAND_CREATED;
import static se.sundsvall.casedata.service.util.Constants.NOTIFICATION_ERRAND_UPDATED;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toErrand;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toErrandEntity;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toOwnerId;

@Service
public class ErrandService {

	private static final String ERRAND_WAS_NOT_FOUND = "Errand with id: {0} was not found";

	private final ErrandRepository errandRepository;

	private final ProcessService processService;
	private final NotificationService notificationService;

	public ErrandService(final ErrandRepository errandRepository, final ProcessService processService, final NotificationService notificationService) {
		this.errandRepository = errandRepository;
		this.processService = processService;
		this.notificationService = notificationService;
	}

	public Errand findByIdAndMunicipalityIdAndNamespace(final Long errandId, final String municipalityId, final String namespace) {
		return toErrand(errandRepository.findByIdAndMunicipalityIdAndNamespace(errandId, municipalityId, namespace)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND,
				format(ERRAND_WAS_NOT_FOUND, errandId))));
	}

	public ErrandEntity getErrandByIdAndMunicipalityIdAndNamespace(final Long errandId, final String municipalityId, final String namespace) {
		return errandRepository.findByIdAndMunicipalityIdAndNamespace(errandId, municipalityId, namespace)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND,
				format(ERRAND_WAS_NOT_FOUND, errandId)));
	}

	/**
	 * @return Page of Errand without duplicates
	 */
	public Page<Errand> findAll(final Specification<ErrandEntity> specification, final String municipalityId, final String namespace, final Pageable pageable) {
		// Extract all ID's and remove duplicates
		final List<Long> allIds = errandRepository.findAll(specification).stream()
			.filter(errand -> municipalityId.equals(errand.getMunicipalityId()))
			.map(ErrandEntity::getId)
			.distinct()
			.toList();

		return errandRepository.findAllByIdInAndMunicipalityIdAndNamespace(allIds, municipalityId, namespace, pageable)
			.map(EntityMapper::toErrand);
	}

	/**
	 * Saves errand and update the process in ParkingPermit if it's a parking permit errand
	 */
	public Errand createErrand(final Errand errand, final String municipalityId, final String namespace) {
		final var errandEntity = toErrandEntity(errand, municipalityId, namespace);
		final var resultErrand = errandRepository.save(errandEntity);

		// Will not start a process if it's not a parking permit or mex errand
		startProcess(resultErrand);

		// Create notification
		notificationService.createNotification(municipalityId, namespace, Notification.builder()
			.withCreatedBy(errand.getCreatedBy())
			.withDescription(NOTIFICATION_ERRAND_CREATED)
			.withErrandId(resultErrand.getId())
			.withType(CREATE.toString())
			.withOwnerId(toOwnerId(resultErrand))
			.build());

		return toErrand(resultErrand);
	}

	public void updateErrand(final Long errandId, final String municipalityId, final String namespace, final PatchErrand patchErrand) {
		final var oldErrand = getErrandByIdAndMunicipalityIdAndNamespace(errandId, municipalityId, namespace);
		final var updatedErrand = PatchMapper.patchErrand(oldErrand, patchErrand);

		processService.updateProcess(errandRepository.save(updatedErrand));

		// Create notification
		notificationService.createNotification(municipalityId, namespace, Notification.builder()
			.withCreatedBy(updatedErrand.getCreatedBy())
			.withDescription(NOTIFICATION_ERRAND_UPDATED)
			.withErrandId(updatedErrand.getId())
			.withType(UPDATE.toString())
			.withOwnerId(toOwnerId(updatedErrand))
			.build());
	}

	@Transactional
	public void deleteByIdAndMunicipalityIdAndNamespace(final Long errandId, final String municipalityId, final String namespace) {
		if (!errandRepository.existsByIdAndMunicipalityIdAndNamespace(errandId, municipalityId, namespace)) {
			throw Problem.valueOf(NOT_FOUND, format(ERRAND_WAS_NOT_FOUND, errandId));
		}

		errandRepository.deleteByIdAndMunicipalityIdAndNamespace(errandId, municipalityId, namespace);
	}

	@Retry(name = "OptimisticLocking")
	private void startProcess(final ErrandEntity errand) {
		try {
			final var startProcessResponse = processService.startProcess(errand);
			if (!isNull(startProcessResponse)) {
				errand.setProcessId(startProcessResponse.getProcessId());
				errandRepository.save(errand);
			}
		} catch (final Exception e) {
			errandRepository.delete(errand);
			throw e;
		}
	}

}
