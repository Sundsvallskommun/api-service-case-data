package se.sundsvall.casedata.service;

import static java.text.MessageFormat.format;
import static org.zalando.problem.Status.NOT_FOUND;
import static se.sundsvall.casedata.service.util.Constants.ERRAND_WAS_NOT_FOUND;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toFacility;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toFacilityDto;
import static se.sundsvall.casedata.service.util.mappers.PatchMapper.patchFacility;

import java.util.List;

import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;

import se.sundsvall.casedata.api.model.FacilityDTO;
import se.sundsvall.casedata.integration.db.ErrandRepository;
import se.sundsvall.casedata.integration.db.FacilityRepository;
import se.sundsvall.casedata.integration.db.model.Errand;
import se.sundsvall.casedata.integration.db.model.Facility;
import se.sundsvall.casedata.service.util.mappers.EntityMapper;
import se.sundsvall.casedata.service.util.mappers.PutMapper;

import io.github.resilience4j.retry.annotation.Retry;

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


	public List<FacilityDTO> findFacilitiesOnErrand(final Long errandId, final String municipalityId, final String namespace) {
		return getErrandByIdAndMunicipalityIdAndNamespace(errandId, municipalityId, namespace).getFacilities().stream()
			.map(EntityMapper::toFacilityDto)
			.toList();
	}

	public FacilityDTO findFacilityOnErrand(final Long errandId, final Long facilityId, final String municipalityId, final String namespace) {
		return toFacilityDto(facilityRepository.findByIdAndErrandIdAndMunicipalityIdAndNamespace(facilityId, errandId, municipalityId, namespace)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, format(FACILITY_WITH_ID_X_WAS_NOT_FOUND_ON_ERRAND_WITH_ID_X, facilityId, errandId))));
	}


	@Retry(name = "OptimisticLocking")
	public FacilityDTO createFacility(final Long errandId, final String municipalityId, final String namespace, final FacilityDTO facilityDTO) {
		final var errand = getErrandByIdAndMunicipalityIdAndNamespace(errandId, municipalityId, namespace);
		final var facility = toFacility(facilityDTO, municipalityId, namespace);
		facility.setErrand(errand);

		final var facilityDto = toFacilityDto(facilityRepository.save(facility));

		processService.updateProcess(errand);

		return facilityDto;
	}


	@Retry(name = "OptimisticLocking")
	public FacilityDTO updateFacilityOnErrand(final Long errandId, final String municipalityId, final String namespace, final Long facilityId, final FacilityDTO facilityDTO) {
		final var errand = getErrandByIdAndMunicipalityIdAndNamespace(errandId, municipalityId, namespace);
		final var facility = facilityRepository.findByIdAndErrandIdAndMunicipalityIdAndNamespace(facilityId, errandId, municipalityId, namespace)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, format(FACILITY_WITH_ID_X_WAS_NOT_FOUND_ON_ERRAND_WITH_ID_X, facilityId, errandId)));

		final var updatedFacility = patchFacility(facility, facilityDTO);
		final var result = toFacilityDto(facilityRepository.save(updatedFacility));
		processService.updateProcess(errand);

		return result;
	}

	@Retry(name = "OptimisticLocking")
	public void replaceFacilitiesOnErrand(final Long errandId, final String municipalityId, final String namespace, final List<FacilityDTO> dtos) {
		final var oldErrand = getErrandByIdAndMunicipalityIdAndNamespace(errandId, municipalityId, namespace);
		final var facilitiesToChange = oldErrand.getFacilities().stream().filter(facility -> dtos.stream().map(FacilityDTO::getId).toList().contains(facility.getId())).toList();
		final var newFacilities = dtos.stream()
			.filter(dto -> !facilitiesToChange.stream()
				.map(Facility::getId)
				.toList().contains(dto.getId()))
			.map(facilityDTO -> toFacility(facilityDTO, municipalityId, namespace))
			.toList();

		oldErrand.getFacilities().clear();

		oldErrand.getFacilities().addAll(dtos.stream()
			.filter(dto -> facilitiesToChange.stream()
				.map(Facility::getId).toList().contains(dto.getId()))
			.map(dto -> PutMapper.putFacility(facilitiesToChange.stream().
				filter(facility -> facility.getId().equals(dto.getId())).findFirst().orElse(null), dto)).toList());

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

	public Errand getErrandByIdAndMunicipalityIdAndNamespace(final Long errandId, final String municipalityId, final String namespace) {
		return errandRepository.findByIdAndMunicipalityIdAndNamespace(errandId, municipalityId, namespace)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND,
				format(ERRAND_WAS_NOT_FOUND, errandId)));
	}


}
