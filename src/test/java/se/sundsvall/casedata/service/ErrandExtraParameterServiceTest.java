package se.sundsvall.casedata.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.zalando.problem.Status.NOT_FOUND;
import static se.sundsvall.casedata.TestUtil.createErrandEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.zalando.problem.ThrowableProblem;
import se.sundsvall.casedata.api.model.ExtraParameter;
import se.sundsvall.casedata.integration.db.ErrandRepository;
import se.sundsvall.casedata.integration.db.model.ErrandEntity;
import se.sundsvall.casedata.integration.db.model.ExtraParameterEntity;

@ExtendWith(MockitoExtension.class)
class ErrandExtraParameterServiceTest {

	private static final String NAMESPACE = "namespace";
	private static final String MUNICIPALITY_ID = "municipalityId";
	private static final Long ERRAND_ID = 1L;
	private static final String PARAMETER_KEY = "parameterKey";
	private static final String PARAMETER_VALUE = "parameterValue";

	@Mock
	private ErrandRepository errandRepositoryMock;

	@Captor
	private ArgumentCaptor<ErrandEntity> errandEntityArgumentCaptor;

	@InjectMocks
	private ErrandExtraParameterService errandExtraParameterService;

	@ParameterizedTest(name = "{0}")
	@MethodSource("updateErrandExtraParametersArgumentProvider")
	void updateErrandExtraParameters(String testDescription, List<ExtraParameterEntity> existingParameters, List<ExtraParameterEntity> expectedListInCapture) {

		// Arrange
		final var errandEntity = ErrandEntity.builder().withExtraParameters(existingParameters).build();
		final var parameters = List.of(ExtraParameter.builder().withKey(PARAMETER_KEY).withValues(List.of(PARAMETER_VALUE)).build());

		existingParameters.stream().forEach(e -> e.setErrand(errandEntity)); // Set connection to errandEntity for existing parameter entities
		expectedListInCapture.stream().forEach(e -> e.setErrand(errandEntity)); // Set connection to errandEntity for expected list of parameter entities

		when(errandRepositoryMock.findWithPessimisticLockingByIdAndMunicipalityIdAndNamespace(ERRAND_ID, MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.of(errandEntity));
		when(errandRepositoryMock.save(any(ErrandEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

		// Act
		errandExtraParameterService.updateErrandExtraParameters(NAMESPACE, MUNICIPALITY_ID, ERRAND_ID, parameters);

		// Assert
		verify(errandRepositoryMock).save(errandEntityArgumentCaptor.capture());

		assertThat(errandEntityArgumentCaptor.getValue().getExtraParameters()).usingRecursiveComparison().isEqualTo(expectedListInCapture);
	}

	private static Stream<Arguments> updateErrandExtraParametersArgumentProvider() {
		final var existingParameterEntityWithNonMatchingKey = ExtraParameterEntity.builder().withKey("key-that-should-be-untouched").withValues(new ArrayList<>(List.of("untouchedValue"))).build();
		ExtraParameterEntity.builder().withKey(PARAMETER_KEY).withValues(new ArrayList<>(List.of("existingValue"))).build();
		final var parameterEntitiesWithNullKey = new ArrayList<>(List.of(existingParameterEntityWithNonMatchingKey));
		parameterEntitiesWithNullKey.addFirst(ExtraParameterEntity.builder().build());

		return Stream.of(
			Arguments.of("updateExtraParametersWhenKeyExistsWithOtherValue",
				new ArrayList<>(List.of(existingParameterEntityWithNonMatchingKey, ExtraParameterEntity.builder().withKey(PARAMETER_KEY).withValues(new ArrayList<>(List.of("existingValue"))).build())),
				List.of(existingParameterEntityWithNonMatchingKey, ExtraParameterEntity.builder().withKey(PARAMETER_KEY).withValues(new ArrayList<>(List.of(PARAMETER_VALUE))).build())),
			Arguments.of("updateExtraParametersWhenKeyDoesNotExist",
				new ArrayList<>(List.of(existingParameterEntityWithNonMatchingKey)),
				List.of(existingParameterEntityWithNonMatchingKey, ExtraParameterEntity.builder().withKey(PARAMETER_KEY).withValues(new ArrayList<>(List.of(PARAMETER_VALUE))).build())),
			Arguments.of("updateExtraParametersWhenNullKeyPresentInEntity",
				parameterEntitiesWithNullKey,
				List.of(ExtraParameterEntity.builder().build(), existingParameterEntityWithNonMatchingKey, ExtraParameterEntity.builder().withKey(PARAMETER_KEY).withValues(new ArrayList<>(List.of(PARAMETER_VALUE))).build())));
	}

	@Test
	void readErrandExtraParameter() {

		// Arrange
		final var spy = Mockito.spy(errandExtraParameterService);
		final var errand = createErrandEntity().withExtraParameters(List.of(ExtraParameterEntity.builder().withKey(PARAMETER_KEY).withValues(List.of(PARAMETER_VALUE)).build()));
		when(errandRepositoryMock.findByIdAndMunicipalityIdAndNamespace(ERRAND_ID, MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.of(errand));

		// Act
		final var result = spy.readErrandExtraParameter(NAMESPACE, MUNICIPALITY_ID, ERRAND_ID, PARAMETER_KEY);

		// Assert
		assertThat(result).hasSize(1).containsExactly(PARAMETER_VALUE);
		verify(spy).readErrandExtraParameter(NAMESPACE, MUNICIPALITY_ID, ERRAND_ID, PARAMETER_KEY);
		verify(spy).findExistingErrand(ERRAND_ID, NAMESPACE, MUNICIPALITY_ID, false);
		verify(spy).findParameterEntityOrElseThrow(errand, PARAMETER_KEY);

		verifyNoMoreInteractions(errandRepositoryMock, spy);
	}

	@Test
	void findErrandExtraParameters() {

		// Arrange
		final var errand = createErrandEntity().withExtraParameters(List.of(ExtraParameterEntity.builder().withKey(PARAMETER_KEY).withValues(List.of(PARAMETER_VALUE)).build()));
		when(errandRepositoryMock.findByIdAndMunicipalityIdAndNamespace(ERRAND_ID, MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.of(errand));

		// Act
		final var result = errandExtraParameterService.findErrandExtraParameters(NAMESPACE, MUNICIPALITY_ID, ERRAND_ID);

		// Assert
		assertThat(result).hasSize(1).isEqualTo(List.of(ExtraParameter.builder().withKey(PARAMETER_KEY).withValues(List.of(PARAMETER_VALUE)).build()));
		verify(errandRepositoryMock).findByIdAndMunicipalityIdAndNamespace(ERRAND_ID, MUNICIPALITY_ID, NAMESPACE);
		verifyNoMoreInteractions(errandRepositoryMock);
	}

	@Test
	void updateErrandExtraParameter() {

		// Arrange
		final var errand = createErrandEntity().withExtraParameters(List.of(ExtraParameterEntity.builder().withKey(PARAMETER_KEY).withValues(List.of(PARAMETER_VALUE)).build()));
		final var errandExtraParameterValues = List.of("anotherValue");

		when(errandRepositoryMock.findWithPessimisticLockingByIdAndMunicipalityIdAndNamespace(ERRAND_ID, MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.of(errand));
		when(errandRepositoryMock.save(errand)).thenReturn(errand);

		// Act
		final var result = errandExtraParameterService.updateErrandExtraParameter(NAMESPACE, MUNICIPALITY_ID, ERRAND_ID, PARAMETER_KEY, errandExtraParameterValues);

		// Assert
		verify(errandRepositoryMock).findWithPessimisticLockingByIdAndMunicipalityIdAndNamespace(ERRAND_ID, MUNICIPALITY_ID, NAMESPACE);
		verify(errandRepositoryMock).save(errandEntityArgumentCaptor.capture());
		verifyNoMoreInteractions(errandRepositoryMock);

		assertThat(result).isNotNull();
		assertThat(result.getValues()).isEqualTo(List.of("anotherValue"));
		assertThat(errandEntityArgumentCaptor.getValue().getExtraParameters()).satisfiesExactlyInAnyOrder(extraParameterEntity -> {
			assertThat(extraParameterEntity.getKey()).isEqualTo(PARAMETER_KEY);
			assertThat(extraParameterEntity.getValues()).containsExactly("anotherValue");
		});
	}

	@Test
	void deleteErrandExtraParameter() {
		// Arrange
		final var errand = createErrandEntity().withExtraParameters(new ArrayList<>(List.of(ExtraParameterEntity.builder().withKey(PARAMETER_KEY).withValues(List.of(PARAMETER_VALUE)).build())));
		when(errandRepositoryMock.findWithPessimisticLockingByIdAndMunicipalityIdAndNamespace(ERRAND_ID, MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.of(errand));

		// Act
		errandExtraParameterService.deleteErrandExtraParameter(NAMESPACE, MUNICIPALITY_ID, ERRAND_ID, PARAMETER_KEY);

		// Assert
		verify(errandRepositoryMock).save(errandEntityArgumentCaptor.capture());
		assertThat(errandEntityArgumentCaptor.getValue().getExtraParameters()).isEmpty();
		verifyNoMoreInteractions(errandRepositoryMock);
	}

	@ParameterizedTest
	@NullAndEmptySource
	void deleteErrandExtraParameterWhenErrandHasNoParameters(List<ExtraParameterEntity> extraParameterEntities) {
		// Arrange
		final var errand = createErrandEntity().withExtraParameters(extraParameterEntities);
		when(errandRepositoryMock.findWithPessimisticLockingByIdAndMunicipalityIdAndNamespace(ERRAND_ID, MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.of(errand));

		// Act
		errandExtraParameterService.deleteErrandExtraParameter(NAMESPACE, MUNICIPALITY_ID, ERRAND_ID, PARAMETER_KEY);

		// Assert
		verify(errandRepositoryMock).findWithPessimisticLockingByIdAndMunicipalityIdAndNamespace(ERRAND_ID, MUNICIPALITY_ID, NAMESPACE);
		verifyNoMoreInteractions(errandRepositoryMock);
	}

	@Test
	void findExistingErrand() {

		// Arrange
		final var errand = createErrandEntity();
		when(errandRepositoryMock.findByIdAndMunicipalityIdAndNamespace(ERRAND_ID, MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.of(errand));

		// Act
		final var result = errandExtraParameterService.findExistingErrand(ERRAND_ID, NAMESPACE, MUNICIPALITY_ID, false);

		// Assert
		assertThat(result).isEqualTo(errand);
		verify(errandRepositoryMock).findByIdAndMunicipalityIdAndNamespace(ERRAND_ID, MUNICIPALITY_ID, NAMESPACE);
		verifyNoMoreInteractions(errandRepositoryMock);
	}

	@Test
	void findParameterEntityOrElseThrow_Ok() {

		// Arrange
		final var parameter = ExtraParameterEntity.builder()
			.withId(UUID.randomUUID().toString())
			.withKey(PARAMETER_KEY)
			.withValues(List.of(PARAMETER_VALUE))
			.build();

		final var errand = ErrandEntity.builder().withExtraParameters(List.of(parameter)).build();

		// Act
		final var result = errandExtraParameterService.findParameterEntityOrElseThrow(errand, PARAMETER_KEY);

		// Assert
		assertThat(result).isEqualTo(parameter.getValues());
	}

	@Test
	void findParameterEntityOrElseThrow_Throw() {

		// Arrange
		final var errand = ErrandEntity.builder()
			.withId(12L)
			.withExtraParameters(List.of())
			.build();

		// Act
		final var exception = Assert.assertThrows(ThrowableProblem.class, () -> errandExtraParameterService.findParameterEntityOrElseThrow(errand, "parameterId"));

		// Assert
		assertThat(exception.getStatus()).isEqualTo(NOT_FOUND);
		assertThat(exception.getTitle()).isEqualTo(NOT_FOUND.getReasonPhrase());
		assertThat(exception.getMessage()).isEqualTo("Not Found: A parameter with key 'parameterId' could not be found in errand with id '12'");

	}
}
