package se.sundsvall.casedata.service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zalando.problem.Problem;
import se.sundsvall.casedata.api.model.ExtraParameter;
import se.sundsvall.casedata.integration.db.ErrandRepository;
import se.sundsvall.casedata.integration.db.model.ErrandEntity;
import se.sundsvall.casedata.integration.db.model.ExtraParameterEntity;

import static java.util.Collections.emptyList;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;
import static org.springframework.util.CollectionUtils.isEmpty;
import static org.zalando.problem.Status.NOT_FOUND;
import static se.sundsvall.casedata.service.util.mappers.ErrandExtraParameterMapper.toErrandParameterEntity;
import static se.sundsvall.casedata.service.util.mappers.ErrandExtraParameterMapper.toParameter;
import static se.sundsvall.casedata.service.util.mappers.ErrandExtraParameterMapper.toParameterList;

@Service
public class ErrandExtraParameterService {

	private static final String ERRAND_ENTITY_NOT_FOUND = "An errand with id '%s' could not be found in namespace '%s' for municipality with id '%s'";

	private static final String PARAMETER_NOT_FOUND = "A parameter with key '%s' could not be found in errand with id '%s'";

	private final ErrandRepository errandsRepository;

	private final ApplicationEventPublisher applicationEventPublisher;

	public ErrandExtraParameterService(final ErrandRepository errandsRepository, final ApplicationEventPublisher applicationEventPublisher) {
		this.applicationEventPublisher = applicationEventPublisher;
		this.errandsRepository = errandsRepository;
	}

	@Transactional
	public List<ExtraParameter> updateErrandExtraParameters(final String namespace, final String municipalityId, final Long errandId, final List<ExtraParameter> parameters) {
		final var errandEntity = findExistingErrand(errandId, namespace, municipalityId, true);

		// Retrieve extra parameter as map for better performance
		final var extraParameterEntityMap = errandEntity.getExtraParameters().stream()
			.filter(entity -> nonNull(entity.getKey()))
			.collect(toMap(ExtraParameterEntity::getKey, Function.identity()));

		// Process incoming list of parameters and update parameter values if key already exists for errand or add it as new
		// parameter if key is absent
		ofNullable(parameters).orElse(emptyList()).stream()
			.forEach(parameter -> processParameter(errandEntity, extraParameterEntityMap.get(parameter.getKey()), parameter));

		final var updatedErrand = errandsRepository.save(errandEntity);
		applicationEventPublisher.publishEvent(updatedErrand);

		return toParameterList(updatedErrand.getExtraParameters());
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
		final var errand = findExistingErrand(errandId, namespace, municipalityId, false);
		return findParameterEntityOrElseThrow(errand, parameterKey);
	}

	public List<ExtraParameter> findErrandExtraParameters(final String namespace, final String municipalityId, final Long errandId) {
		final var errandEntity = findExistingErrand(errandId, namespace, municipalityId, false);

		return toParameterList(errandEntity.getExtraParameters());
	}

	@Transactional
	public ExtraParameter updateErrandExtraParameter(final String namespace, final String municipalityId, final Long errandId, final String parameterKey, final List<String> parameterValues) {
		final var errandEntity = findExistingErrand(errandId, namespace, municipalityId, true);

		final var parameterEntity = errandEntity.getExtraParameters().stream()
			.filter(paramEntity -> Objects.equals(paramEntity.getKey(), parameterKey))
			.findFirst()
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, String.format(PARAMETER_NOT_FOUND, parameterKey, errandEntity.getId())))
			.withValues(parameterValues);

		final var updatedErrand = errandsRepository.save(errandEntity);
		applicationEventPublisher.publishEvent(updatedErrand);

		return toParameter(parameterEntity);
	}

	@Transactional
	public void deleteErrandExtraParameter(final String namespace, final String municipalityId, final Long errandId, final String parameterKey) {
		final var errandEntity = findExistingErrand(errandId, namespace, municipalityId, true);

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

	ErrandEntity findExistingErrand(final Long id, final String namespace, final String municipalityId, final boolean usePessimisticLocking) {
		if (usePessimisticLocking) {
			return errandsRepository.findWithPessimisticLockingByIdAndMunicipalityIdAndNamespace(id, municipalityId, namespace)
				.orElseThrow(() -> Problem.valueOf(NOT_FOUND, String.format(ERRAND_ENTITY_NOT_FOUND, id, namespace, municipalityId)));
		}
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
