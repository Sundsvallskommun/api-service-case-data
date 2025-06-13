package se.sundsvall.casedata.service;

import static org.zalando.problem.Status.NOT_FOUND;
import static se.sundsvall.casedata.service.util.Constants.ERRAND_ENTITY_NOT_FOUND;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toFacility;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toFacilityEntity;
import static se.sundsvall.casedata.service.util.mappers.PatchMapper.patchFacility;

import java.util.List;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zalando.problem.Problem;
import se.sundsvall.casedata.api.model.Facility;
import se.sundsvall.casedata.integration.db.ErrandRepository;
import se.sundsvall.casedata.integration.db.FacilityRepository;
import se.sundsvall.casedata.integration.db.model.ErrandEntity;
import se.sundsvall.casedata.integration.db.model.FacilityEntity;
import se.sundsvall.casedata.service.util.mappers.EntityMapper;
import se.sundsvall.casedata.service.util.mappers.PutMapper;

@Service
@Transactional
public class FacilityService {

	private static final String FACILITY_WITH_ID_X_WAS_NOT_FOUND_ON_ERRAND_WITH_ID_X = "Facility with id: %s was not found on errand with id: %s";
	private final ErrandRepository errandRepository;
	private final FacilityRepository facilityRepository;
	private final ApplicationEventPublisher applicationEventPublisher;

	public FacilityService(final ErrandRepository errandRepository, final FacilityRepository facilityRepository, final ApplicationEventPublisher applicationEventPublisher) {
		this.errandRepository = errandRepository;
		this.facilityRepository = facilityRepository;
		this.applicationEventPublisher = applicationEventPublisher;
	}

	public List<Facility> findFacilities(final Long errandId, final String municipalityId, final String namespace) {
		return findErrandEntity(errandId, municipalityId, namespace, false).getFacilities().stream()
			.map(EntityMapper::toFacility)
			.toList();
	}

	public Facility findFacility(final Long errandId, final Long facilityId, final String municipalityId, final String namespace) {
		return toFacility(facilityRepository.findByIdAndErrandIdAndMunicipalityIdAndNamespace(facilityId, errandId, municipalityId, namespace)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, FACILITY_WITH_ID_X_WAS_NOT_FOUND_ON_ERRAND_WITH_ID_X.formatted(facilityId, errandId))));
	}

	public Facility create(final Long errandId, final String municipalityId, final String namespace, final Facility facility) {
		final var errand = findErrandEntity(errandId, municipalityId, namespace, true);
		final var entity = toFacilityEntity(facility, municipalityId, namespace);
		entity.setErrand(errand);

		final var createdFacility = toFacility(facilityRepository.save(entity));

		applicationEventPublisher.publishEvent(errand);

		return createdFacility;
	}

	public Facility update(final Long errandId, final String municipalityId, final String namespace, final Long facilityId, final Facility facility) {
		final var errand = findErrandEntity(errandId, municipalityId, namespace, true);
		final var facilityEntity = facilityRepository.findByIdAndErrandIdAndMunicipalityIdAndNamespace(facilityId, errandId, municipalityId, namespace)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, FACILITY_WITH_ID_X_WAS_NOT_FOUND_ON_ERRAND_WITH_ID_X.formatted(facilityId, errandId)));

		final var updatedFacility = patchFacility(facilityEntity, facility);
		final var result = toFacility(facilityRepository.save(updatedFacility));
		applicationEventPublisher.publishEvent(errand);

		return result;
	}

	public void replaceFacilities(final Long errandId, final String municipalityId, final String namespace, final List<Facility> dtos) {
		final var oldErrand = findErrandEntity(errandId, municipalityId, namespace, true);
		final var facilitiesToChange = oldErrand.getFacilities().stream().filter(facility -> dtos.stream().map(Facility::getId).toList().contains(facility.getId())).toList();
		final var newFacilities = dtos.stream()
			.filter(dto -> !facilitiesToChange.stream()
				.map(FacilityEntity::getId)
				.toList().contains(dto.getId()))
			.map(facilityDTO -> toFacilityEntity(facilityDTO, municipalityId, namespace))
			.toList();

		oldErrand.getFacilities().clear();

		oldErrand.getFacilities().addAll(dtos.stream()
			.filter(dto -> facilitiesToChange.stream()
				.map(FacilityEntity::getId).toList().contains(dto.getId()))
			.map(dto -> PutMapper.putFacility(facilitiesToChange.stream().filter(facility -> facility.getId().equals(dto.getId())).findFirst().orElse(null), dto)).toList());

		oldErrand.getFacilities().addAll(newFacilities.stream().map(facility -> {
			facility.setErrand(oldErrand);
			return facility;
		}).toList());

		final var updatedErrand = errandRepository.save(oldErrand);
		applicationEventPublisher.publishEvent(updatedErrand);
	}

	public void delete(final Long errandId, final String municipalityId, final String namespace, final Long facilityId) {
		final var errand = findErrandEntity(errandId, municipalityId, namespace, true);
		final var facilityToRemove = errand.getFacilities().stream()
			.filter(facility -> facility.getId().equals(facilityId))
			.findAny()
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, FACILITY_WITH_ID_X_WAS_NOT_FOUND_ON_ERRAND_WITH_ID_X.formatted(facilityId, errandId)));

		errand.getFacilities().remove(facilityToRemove);
		errandRepository.save(errand);
		applicationEventPublisher.publishEvent(errand);
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
