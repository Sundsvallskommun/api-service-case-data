package se.sundsvall.casedata.service.util.mappers;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import se.sundsvall.casedata.api.model.ExtraParameter;
import se.sundsvall.casedata.integration.db.model.ErrandEntity;
import se.sundsvall.casedata.integration.db.model.ExtraParameterEntity;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toCollection;

public final class ErrandExtraParameterMapper {

	private ErrandExtraParameterMapper() {
		// Intentionally empty
	}

	public static List<ExtraParameterEntity> toErrandParameterEntityList(final List<ExtraParameter> parameters, final ErrandEntity entity) {
		return toUniqueKeyList(parameters).stream()
			.map(parameter -> toErrandParameterEntity(parameter, entity))
			.collect(toCollection(ArrayList::new));
	}

	public static ExtraParameterEntity toErrandParameterEntity(final ExtraParameter parameter, final ErrandEntity errandEntity) {
		return ExtraParameterEntity.builder()
			.withDisplayName(parameter.getDisplayName())
			.withErrand(errandEntity)
			.withKey(parameter.getKey())
			.withValues(parameter.getValues())
			.build();
	}

	public static ExtraParameter toParameter(final ExtraParameterEntity parameter) {
		return ExtraParameter.builder()
			.withId(parameter.getId())
			.withDisplayName(parameter.getDisplayName())
			.withKey(parameter.getKey())
			.withValues(parameter.getValues())
			.build();
	}

	public static List<ExtraParameter> toParameterList(final List<ExtraParameterEntity> parameters) {
		return ofNullable(parameters).orElse(emptyList()).stream()
			.map(ErrandExtraParameterMapper::toParameter)
			.toList();
	}

	public static List<ExtraParameter> toUniqueKeyList(final List<ExtraParameter> parameterList) {
		return ofNullable(parameterList).orElse(emptyList()).stream()
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
			.collect(toCollection(ArrayList::new));
	}

}
