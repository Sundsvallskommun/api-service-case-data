package se.sundsvall.casedata.service;

import static java.text.MessageFormat.format;
import static java.util.Objects.isNull;
import static org.zalando.problem.Status.NOT_FOUND;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toErrand;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toErrandDto;

import java.util.List;
import java.util.Map;

import jakarta.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;

import se.sundsvall.casedata.api.model.ErrandDTO;
import se.sundsvall.casedata.api.model.PatchErrandDTO;
import se.sundsvall.casedata.integration.db.ErrandRepository;
import se.sundsvall.casedata.integration.db.model.Errand;
import se.sundsvall.casedata.service.util.mappers.EntityMapper;
import se.sundsvall.casedata.service.util.mappers.PatchMapper;

import io.github.resilience4j.retry.annotation.Retry;

@Service
public class ErrandService {

	private static final String ERRAND_WAS_NOT_FOUND = "Errand with id: {0} was not found";


	private final ErrandRepository errandRepository;


	private final ProcessService processService;

	public ErrandService(final ErrandRepository errandRepository, final ProcessService processService) {
		this.errandRepository = errandRepository;

		this.processService = processService;
	}

	public ErrandDTO findByIdAndMunicipalityIdAndNamespace(final Long errandId, final String municipalityId, final String namespace) {
		return toErrandDto(errandRepository.findByIdAndMunicipalityIdAndNamespace(errandId, municipalityId, namespace)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND,
				format(ERRAND_WAS_NOT_FOUND, errandId))));
	}

	public Errand getErrandByIdAndMunicipalityIdAndNamespace(final Long errandId, final String municipalityId, final String namespace) {
		return errandRepository.findByIdAndMunicipalityIdAndNamespace(errandId, municipalityId, namespace)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND,
				format(ERRAND_WAS_NOT_FOUND, errandId)));
	}

	/**
	 * @return Page of ErrandDTO without duplicates
	 */
	public Page<ErrandDTO> findAll(final Specification<Errand> specification, final String municipalityId, final String namespace, final Map<String, String> extraParameters, final Pageable pageable) {
		// Extract all ID's and remove duplicates
		final List<Long> allIds = errandRepository.findAll(specification).stream()
			.filter(errand -> municipalityId.equals(errand.getMunicipalityId()))
			.filter(errand -> hashmapContainsAllKeyAndValues(errand.getExtraParameters(), extraParameters))
			.map(Errand::getId)
			.distinct()
			.toList();

		return errandRepository.findAllByIdInAndMunicipalityIdAndNamespace(allIds, municipalityId, namespace, pageable)
			.map(EntityMapper::toErrandDto);
	}


	/**
	 * Saves errand and update the process in ParkingPermit if it's a parking permit errand
	 */
	public ErrandDTO createErrand(final ErrandDTO errandDTO, final String municipalityId, final String namespace) {
		final var errand = toErrand(errandDTO, municipalityId, namespace);
		final var resultErrand = errandRepository.save(errand);

		// Will not start a process if it's not a parking permit or mex errand
		startProcess(resultErrand);

		return toErrandDto(resultErrand);
	}


	public void updateErrand(final Long errandId, final String municipalityId, final String namespace, final PatchErrandDTO patchErrandDTO) {
		final var oldErrand = getErrandByIdAndMunicipalityIdAndNamespace(errandId, municipalityId, namespace);
		final var updatedErrand = PatchMapper.patchErrand(oldErrand, patchErrandDTO);
		errandRepository.save(updatedErrand);
		processService.updateProcess(updatedErrand);
	}

	@Transactional
	public void deleteByIdAndMunicipalityIdAndNamespace(final Long errandId, final String municipalityId, final String namespace) {
		if (!errandRepository.existsByIdAndMunicipalityIdAndNamespace(errandId, municipalityId, namespace)) {
			throw Problem.valueOf(NOT_FOUND, format(ERRAND_WAS_NOT_FOUND, errandId));
		}

		errandRepository.deleteByIdAndMunicipalityIdAndNamespace(errandId, municipalityId, namespace);
	}


	private boolean hashmapContainsAllKeyAndValues(final Map<String, String> map, final Map<String, String> mapToCheck) {
		for (final Map.Entry<String, String> entry : mapToCheck.entrySet()) {
			final String mapValue = map.get(entry.getKey());
			if (!entry.getValue().equals(mapValue)) {
				return false;
			}
		}
		return true;
	}


	@Retry(name = "OptimisticLocking")


	private void startProcess(final Errand errand) {
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
