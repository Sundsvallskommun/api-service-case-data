package se.sundsvall.casedata.service;

import static java.util.Collections.emptyList;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;
import static org.springframework.util.CollectionUtils.isEmpty;
import static org.zalando.problem.Status.NOT_FOUND;
import static se.sundsvall.casedata.service.util.mappers.ErrandExtraParameterMapper.toErrandParameterEntity;
import static se.sundsvall.casedata.service.util.mappers.ErrandExtraParameterMapper.toParameter;
import static se.sundsvall.casedata.service.util.mappers.ErrandExtraParameterMapper.toParameterList;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;
import se.sundsvall.casedata.api.model.ExtraParameter;
import se.sundsvall.casedata.integration.db.ErrandRepository;
import se.sundsvall.casedata.integration.db.model.ErrandEntity;
import se.sundsvall.casedata.integration.db.model.ExtraParameterEntity;

@Service
public class ErrandExtraParameterService {

	private static final String ERRAND_ENTITY_NOT_FOUND = "An errand with id '%s' could not be found in namespace '%s' for municipality with id '%s'";

	private static final String PARAMETER_NOT_FOUND = "A parameter with key '%s' could not be found in errand with id '%s'";

	private final ErrandRepository errandsRepository;

	public ErrandExtraParameterService(final ErrandRepository errandsRepository) {
		this.errandsRepository = errandsRepository;
	}

	public List<ExtraParameter> updateErrandExtraParameters(final String namespace, final String municipalityId, final Long errandId, final List<ExtraParameter> parameters) {
		final var errandEntity = findExistingErrand(errandId, namespace, municipalityId);

		// Retrieve extra parameter keys as list for better performance
		final var extraParameterEntityMap = errandEntity.getExtraParameters().stream()
			.filter(entity -> nonNull(entity.getKey()))
			.collect(toMap(ExtraParameterEntity::getKey, Function.identity()));

		// Process incoming list of parameters and update parameter if key already exists for errand or add if it is absent
		ofNullable(parameters).orElse(emptyList()).stream()
			.forEach(parameter -> processParameter(errandEntity, extraParameterEntityMap.get(parameter.getKey()), parameter));

		return toParameterList(errandsRepository.save(errandEntity).getExtraParameters());
	}

	private void processParameter(final ErrandEntity errandEntity, ExtraParameterEntity extraParameterEntity, ExtraParameter parameter) {
		if (nonNull(extraParameterEntity)) {
			extraParameterEntity.setDisplayName(parameter.getDisplayName());
			extraParameterEntity.setValues(parameter.getValues());
		} else {
			errandEntity.getExtraParameters().add(toErrandParameterEntity(parameter, errandEntity));
		}
	}

	public List<String> readErrandExtraParameter(final String namespace, final String municipalityId, final Long errandId, final String parameterKey) {
		final var errand = findExistingErrand(errandId, namespace, municipalityId);
		return findParameterEntityOrElseThrow(errand, parameterKey);
	}

	public List<ExtraParameter> findErrandExtraParameters(final String namespace, final String municipalityId, final Long errandId) {
		final var errandEntity = findExistingErrand(errandId, namespace, municipalityId);

		return toParameterList(errandEntity.getExtraParameters());
	}

	public ExtraParameter updateErrandExtraParameter(final String namespace, final String municipalityId, final Long errandId, final String parameterKey, final List<String> parameterValues) {
		final var errandEntity = findExistingErrand(errandId, namespace, municipalityId);

		final var parameterEntity = errandEntity.getExtraParameters().stream()
			.filter(paramEntity -> Objects.equals(paramEntity.getKey(), parameterKey))
			.findFirst()
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, String.format(PARAMETER_NOT_FOUND, parameterKey, errandEntity.getId())))
			.withValues(parameterValues);

		errandsRepository.save(errandEntity);

		return toParameter(parameterEntity);
	}

	public void deleteErrandExtraParameter(final String namespace, final String municipalityId, final Long errandId, final String parameterKey) {
		final var errandEntity = findExistingErrand(errandId, namespace, municipalityId);

		if (isEmpty(errandEntity.getExtraParameters())) {
			return;
		}

		final var parameterToRemove = errandEntity.getExtraParameters().stream()
			.filter(paramEntity -> Objects.equals(paramEntity.getKey(), parameterKey))
			.findFirst()
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, String.format(PARAMETER_NOT_FOUND, parameterKey, errandEntity.getId())));

		errandEntity.getExtraParameters().remove(parameterToRemove);

		errandsRepository.save(errandEntity);
	}

	ErrandEntity findExistingErrand(final Long id, final String namespace, final String municipalityId) {
		return errandsRepository.findByIdAndMunicipalityIdAndNamespace(id, municipalityId, namespace)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, String.format(ERRAND_ENTITY_NOT_FOUND, id, namespace, municipalityId)));
	}

	List<String> findParameterEntityOrElseThrow(final ErrandEntity errandEntity, final String parameterKey) {
		return Optional.ofNullable(errandEntity.getExtraParameters()).orElse(emptyList()).stream()
			.filter(paramEntity -> Objects.equals(paramEntity.getKey(), parameterKey))
			.findAny()
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, String.format(PARAMETER_NOT_FOUND, parameterKey, errandEntity.getId())))
			.getValues();
	}
}
