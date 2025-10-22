package se.sundsvall.casedata.service.util.mappers;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import se.sundsvall.casedata.api.model.ExtraParameter;
import se.sundsvall.casedata.integration.db.model.ErrandEntity;
import se.sundsvall.casedata.integration.db.model.ExtraParameterEntity;

class ErrandExtraParameterMapperTest {

	@Test
	void testToErrandParameterEntityListExtraParameter() {
		// Arrange
		final var errandEntity = new ErrandEntity();
		final var parameter = ExtraParameter.builder()
			.withDisplayName("Test Display Name")
			.withKey("TestKey")
			.withValues(List.of("Value1", "Value2"))
			.build();
		final var parameters = List.of(parameter);

		// Act
		final var result = ErrandExtraParameterMapper.toErrandParameterEntityList(parameters, errandEntity);

		// Assert
		assertThat(result).hasSize(1);
		assertThat(result.getFirst().getDisplayName()).isEqualTo("Test Display Name");
		assertThat(result.getFirst().getKey()).isEqualTo("TestKey");
		assertThat(result.getFirst().getValues()).containsExactly("Value1", "Value2");
		assertThat(result.getFirst().getErrand()).isEqualTo(errandEntity);
	}

	@Test
	void testToErrandParameterEntityListReplacesExistingParameter() {
		// Arrange
		final var errandEntity = new ErrandEntity();
		final var existingParameter = ExtraParameterEntity.builder()
			.withDisplayName("Old Display Name")
			.withKey("TestKey")
			.withValues(List.of("OldValue"))
			.build();
		errandEntity.setExtraParameters(List.of(existingParameter));

		final var newParameter = ExtraParameter.builder()
			.withDisplayName("New Display Name")
			.withKey("TestKey")
			.withValues(List.of("NewValue1", "NewValue2"))
			.build();
		final var newParameters = List.of(newParameter);

		// Act
		final var result = ErrandExtraParameterMapper.toErrandParameterEntityList(newParameters, errandEntity);

		// Assert
		assertThat(result).hasSize(1);
		assertThat(result.getFirst().getDisplayName()).isEqualTo("New Display Name");
		assertThat(result.getFirst().getKey()).isEqualTo("TestKey");
		assertThat(result.getFirst().getValues()).containsExactly("NewValue1", "NewValue2");
		assertThat(result.getFirst().getErrand()).isEqualTo(errandEntity);
	}

	@Test
	void testToErrandParameterEntityListNullParameters() {
		// Arrange
		final ErrandEntity errandEntity = new ErrandEntity();

		// Act
		final List<ExtraParameterEntity> result = ErrandExtraParameterMapper.toErrandParameterEntityList(null, errandEntity);

		// Assert
		assertThat(result).isEmpty();
	}

	@Test
	void testToErrandParameterEntityExtraParameter() {
		// Arrange
		final ExtraParameter parameter = ExtraParameter.builder()
			.withDisplayName("Test Display Name")
			.withKey("TestKey")
			.withValues(List.of("Value1", "Value2"))
			.build();

		// Act
		final ExtraParameterEntity result = ErrandExtraParameterMapper.toErrandParameterEntity(parameter);

		// Assert
		assertThat(result.getDisplayName()).isEqualTo("Test Display Name");
		assertThat(result.getKey()).isEqualTo("TestKey");
		assertThat(result.getValues()).containsExactly("Value1", "Value2");
	}

	@Test
	void testToParameterExtraParameter() {
		// Arrange
		final ExtraParameterEntity parameterEntity = ExtraParameterEntity.builder()
			.withId("id")
			.withDisplayName("Test Display Name")
			.withKey("TestKey")
			.withValues(List.of("Value1", "Value2"))
			.build();

		// Act
		final ExtraParameter result = ErrandExtraParameterMapper.toParameter(parameterEntity);

		// Assert
		assertThat(result.getId()).isEqualTo("id");
		assertThat(result.getDisplayName()).isEqualTo("Test Display Name");
		assertThat(result.getKey()).isEqualTo("TestKey");
		assertThat(result.getValues()).containsExactly("Value1", "Value2");
	}

	@Test
	void testToParameterListExtraParameter() {
		// Arrange
		final ExtraParameterEntity parameterEntity = ExtraParameterEntity.builder()
			.withDisplayName("Test Display Name")
			.withKey("TestKey")
			.withValues(List.of("Value1", "Value2"))
			.build();
		final List<ExtraParameterEntity> parameterEntities = List.of(parameterEntity);

		// Act
		final List<ExtraParameter> result = ErrandExtraParameterMapper.toParameterList(parameterEntities);

		// Assert
		assertThat(result).hasSize(1);
		assertThat(result.getFirst().getDisplayName()).isEqualTo("Test Display Name");
		assertThat(result.getFirst().getKey()).isEqualTo("TestKey");
		assertThat(result.getFirst().getValues()).containsExactly("Value1", "Value2");
	}

	@Test
	void testToUniqueKeyListExtraParameter() {
		// Arrange
		final ExtraParameter parameter1 = ExtraParameter.builder()
			.withDisplayName("Test Display Name 1")
			.withKey("TestKey")
			.withValues(List.of("Value1"))
			.build();
		final ExtraParameter parameter2 = ExtraParameter.builder()
			.withDisplayName("Test Display Name 2")
			.withKey("TestKey")
			.withValues(List.of("Value2"))
			.build();
		final List<ExtraParameter> parameters = List.of(parameter1, parameter2);

		// Act
		final List<ExtraParameter> result = ErrandExtraParameterMapper.toUniqueKeyList(parameters);

		// Assert
		assertThat(result).hasSize(1);
		assertThat(result.getFirst().getDisplayName()).isEqualTo("Test Display Name 1");
		assertThat(result.getFirst().getKey()).isEqualTo("TestKey");
		assertThat(result.getFirst().getValues()).containsExactly("Value1", "Value2");
	}
}
