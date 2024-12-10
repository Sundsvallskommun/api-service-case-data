package se.sundsvall.casedata.service;

import static java.text.MessageFormat.format;
import static org.zalando.problem.Status.NOT_FOUND;
import static se.sundsvall.casedata.service.util.Constants.ERRAND_WAS_NOT_FOUND;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toFacility;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toFacilityEntity;
import static se.sundsvall.casedata.service.util.mappers.PatchMapper.patchFacility;

import io.github.resilience4j.retry.annotation.Retry;
import java.util.List;
import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;
import se.sundsvall.casedata.api.model.Facility;
import se.sundsvall.casedata.integration.db.ErrandRepository;
import se.sundsvall.casedata.integration.db.FacilityRepository;
import se.sundsvall.casedata.integration.db.model.ErrandEntity;
import se.sundsvall.casedata.integration.db.model.FacilityEntity;
import se.sundsvall.casedata.service.util.mappers.EntityMapper;
import se.sundsvall.casedata.service.util.mappers.PutMapper;

@Service
public class FacilityService {

	private static final String FACILITY_WITH_ID_X_WAS_NOT_FOUND_ON_ERRAND_WITH_ID_X = "Facility with id: {0} was not found on errand with id: {1}";

	private final ErrandRepository errandRepository;

	private final FacilityRepository facilityRepository;

	private final ProcessService processService;

	public FacilityService(final ErrandRepository errandRepository, final FacilityRepository facilityRepository, final ProcessService processService) {
		this.errandRepository = errandRepository;
		this.facilityRepository = facilityRepository;
		this.processService = processService;
	}

	public List<Facility> findFacilitiesOnErrand(final Long errandId, final String municipalityId, final String namespace) {
		return getErrandByIdAndMunicipalityIdAndNamespace(errandId, municipalityId, namespace).getFacilities().stream()
			.map(EntityMapper::toFacility)
			.toList();
	}

	public Facility findFacilityOnErrand(final Long errandId, final Long facilityId, final String municipalityId, final String namespace) {
		return toFacility(facilityRepository.findByIdAndErrandIdAndMunicipalityIdAndNamespace(facilityId, errandId, municipalityId, namespace)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, format(FACILITY_WITH_ID_X_WAS_NOT_FOUND_ON_ERRAND_WITH_ID_X, facilityId, errandId))));
	}

	@Retry(name = "OptimisticLocking")
	public Facility createFacilityOnErrand(final Long errandId, final String municipalityId, final String namespace, final Facility facility) {
		final var errand = getErrandByIdAndMunicipalityIdAndNamespace(errandId, municipalityId, namespace);
		final var entity = toFacilityEntity(facility, municipalityId, namespace);
		entity.setErrand(errand);

		final var createdFacility = toFacility(facilityRepository.save(entity));

		processService.updateProcess(errand);

		return createdFacility;
	}

	@Retry(name = "OptimisticLocking")
	public Facility updateFacilityOnErrand(final Long errandId, final String municipalityId, final String namespace, final Long facilityId, final Facility facility) {
		final var errand = getErrandByIdAndMunicipalityIdAndNamespace(errandId, municipalityId, namespace);
		final var facilityEntity = facilityRepository.findByIdAndErrandIdAndMunicipalityIdAndNamespace(facilityId, errandId, municipalityId, namespace)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, format(FACILITY_WITH_ID_X_WAS_NOT_FOUND_ON_ERRAND_WITH_ID_X, facilityId, errandId)));

		final var updatedFacility = patchFacility(facilityEntity, facility);
		final var result = toFacility(facilityRepository.save(updatedFacility));
		processService.updateProcess(errand);

		return result;
	}

	@Retry(name = "OptimisticLocking")
	public void replaceFacilitiesOnErrand(final Long errandId, final String municipalityId, final String namespace, final List<Facility> dtos) {
		final var oldErrand = getErrandByIdAndMunicipalityIdAndNamespace(errandId, municipalityId, namespace);
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
		processService.updateProcess(updatedErrand);
	}

	@Retry(name = "OptimisticLocking")
	public void deleteFacilityOnErrand(final Long errandId, final String municipalityId, final String namespace, final Long facilityId) {
		final var errand = getErrandByIdAndMunicipalityIdAndNamespace(errandId, municipalityId, namespace);
		final var facilityToRemove = errand.getFacilities().stream()
			.filter(facility -> facility.getId().equals(facilityId))
			.findAny()
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, format(FACILITY_WITH_ID_X_WAS_NOT_FOUND_ON_ERRAND_WITH_ID_X, facilityId, errandId)));

		errand.getFacilities().remove(facilityToRemove);
		errandRepository.save(errand);
		processService.updateProcess(errand);
	}

	public ErrandEntity getErrandByIdAndMunicipalityIdAndNamespace(final Long errandId, final String municipalityId, final String namespace) {
		return errandRepository.findByIdAndMunicipalityIdAndNamespace(errandId, municipalityId, namespace)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND,
				format(ERRAND_WAS_NOT_FOUND, errandId)));
	}

}
