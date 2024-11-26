package se.sundsvall.casedata.service.util.mappers;

import se.sundsvall.casedata.api.model.ExtraParameter;
import se.sundsvall.casedata.integration.db.model.ErrandEntity;
import se.sundsvall.casedata.integration.db.model.ExtraParameterEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.groupingBy;

public final class ErrandExtraParameterMapper {

	private ErrandExtraParameterMapper() {
		// Intentionally empty
	}

	public static List<ExtraParameterEntity> toErrandParameterEntityList( List<ExtraParameter> parameters, ErrandEntity entity) {
		return new ArrayList<>(toUniqueKeyList(parameters).stream()
			.map(parameter -> toErrandParameterEntity(parameter).withErrand(entity))
			.toList());
	}

	public static ExtraParameterEntity toErrandParameterEntity(final ExtraParameter parameter) {
		return ExtraParameterEntity.builder()
			.withDisplayName(parameter.getDisplayName())
			.withKey(parameter.getKey())
			.withValues(parameter.getValues())
			.build();
	}

	public static ExtraParameter toParameter(final ExtraParameterEntity parameter) {
		return ExtraParameter.builder()
			.withDisplayName(parameter.getDisplayName())
			.withKey(parameter.getKey())
			.withValues(parameter.getValues())
			.build();
	}

	public static List<ExtraParameter> toParameterList(final List<ExtraParameterEntity> parameters) {
		return Optional.ofNullable(parameters).orElse(emptyList()).stream()
			.map(ErrandExtraParameterMapper::toParameter)
			.toList();
	}

	public static List<ExtraParameter> toUniqueKeyList(List<ExtraParameter> parameterList) {
		return new ArrayList<>(Optional.ofNullable(parameterList).orElse(emptyList()).stream()
			.collect(groupingBy(ExtraParameter::getKey))
			.entrySet()
			.stream()
			.map(entry -> ExtraParameter.builder()
				.withDisplayName(entry.getValue().getFirst().getDisplayName())
				.withKey(entry.getKey())
				.withValues(new ArrayList<>(entry.getValue().stream()
					.map(ExtraParameter::getValues)
					.filter(Objects::nonNull)
					.flatMap(List::stream)
					.toList())).build())
			.toList());
	}

}
