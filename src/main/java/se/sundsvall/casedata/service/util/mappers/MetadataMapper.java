package se.sundsvall.casedata.service.util.mappers;

import java.util.List;
import se.sundsvall.casedata.api.model.CaseType;
import se.sundsvall.casedata.integration.db.model.CaseTypeEntity;

public final class MetadataMapper {

	private MetadataMapper() {}

	public static CaseTypeEntity toCaseTypeEntity(final String municipalityId, final String namespace, final CaseType caseType) {
		return CaseTypeEntity.builder()
			.withMunicipalityId(municipalityId)
			.withNamespace(namespace)
			.withType(caseType.getType())
			.withDisplayName(caseType.getDisplayName())
			.withStartProcess(caseType.isStartProcess())
			.build();
	}

	public static List<CaseType> toCaseTypes(final List<CaseTypeEntity> entities) {
		return entities.stream()
			.map(MetadataMapper::toCaseType)
			.toList();
	}

	public static CaseType toCaseType(final CaseTypeEntity entity) {
		return CaseType.builder()
			.withType(entity.getType())
			.withDisplayName(entity.getDisplayName())
			.withStartProcess(entity.isStartProcess())
			.build();
	}
}
