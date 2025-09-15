package se.sundsvall.casedata.service.util.mappers;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import se.sundsvall.casedata.api.model.CaseType;
import se.sundsvall.casedata.integration.db.model.CaseTypeEntity;

class MetadataMapperTest {

	@Test
	void toCaseTypeEntity() {

		// Arrange
		final var municipalityId = "1234";
		final var namespace = "namespace";
		final var type = "type";
		final var displayName = "displayName";
		final var caseType = CaseType.builder().withType(type).withDisplayName(displayName).build();

		// Act
		final var bean = MetadataMapper.toCaseTypeEntity(municipalityId, namespace, caseType);

		// Assert
		assertThat(bean).hasNoNullFieldsOrPropertiesExcept("id");
		assertThat(bean.getMunicipalityId()).isEqualTo(municipalityId);
		assertThat(bean.getNamespace()).isEqualTo(namespace);
		assertThat(bean.getType()).isEqualTo(type);
		assertThat(bean.getDisplayName()).isEqualTo(displayName);
	}

	@Test
	void toCaseTypes() {
		// Arrange
		final var type = "type";
		final var displayName = "displayName";
		final var type2 = "type2";
		final var displayName2 = "displayName2";
		final var caseType = CaseTypeEntity.builder().withType(type).withDisplayName(displayName).build();
		final var caseType2 = CaseTypeEntity.builder().withType(type2).withDisplayName(displayName2).build();
		final var caseTypes = List.of(caseType, caseType2);

		// Act
		final var bean = MetadataMapper.toCaseTypes(caseTypes);

		//
		assertThat(bean).hasSize(2);
		assertThat(bean.get(0).getType()).isEqualTo(type);
		assertThat(bean.get(0).getDisplayName()).isEqualTo(displayName);
		assertThat(bean.get(1).getType()).isEqualTo(type2);
		assertThat(bean.get(1).getDisplayName()).isEqualTo(displayName2);
	}

	@Test
	void toCaseType() {
		// Arrange
		final var type = "type";
		final var displayName = "displayName";
		final var caseType = CaseTypeEntity.builder().withType(type).withDisplayName(displayName).build();

		// Act
		final var bean = MetadataMapper.toCaseType(caseType);

		// Assert
		assertThat(bean).hasNoNullFieldsOrProperties();
		assertThat(bean.getType()).isEqualTo(type);
		assertThat(bean.getDisplayName()).isEqualTo(displayName);
	}
}
