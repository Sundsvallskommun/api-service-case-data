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
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

	@Test
	void updateErrandExtraParameters() {

		// Arrange
		final var parameters = List.of(ExtraParameter.builder().withKey(PARAMETER_KEY).withValues(List.of(PARAMETER_VALUE)).build());
		when(errandRepositoryMock.findByIdAndMunicipalityIdAndNamespace(ERRAND_ID, MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.of(ErrandEntity.builder().withExtraParameters(new ArrayList<>()).build()));
		when(errandRepositoryMock.save(any(ErrandEntity.class))).thenReturn(ErrandEntity.builder().build());

		// Act
		errandExtraParameterService.updateErrandExtraParameters(NAMESPACE, MUNICIPALITY_ID, ERRAND_ID, parameters);

		// Assert
		verify(errandRepositoryMock).save(errandEntityArgumentCaptor.capture());
		final var errandEntity = errandEntityArgumentCaptor.getValue();

		assertThat(errandEntity.getExtraParameters()).hasSize(1).allSatisfy(parameterEntity -> {
			assertThat(parameterEntity.getKey()).isEqualTo(PARAMETER_KEY);
			assertThat(parameterEntity.getValues()).containsExactly(PARAMETER_VALUE);
		});
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
		verify(spy).findExistingErrand(ERRAND_ID, NAMESPACE, MUNICIPALITY_ID);
		verify(spy).findParameterEntityOrElseThrow(errand, PARAMETER_KEY);
	}

	@Test
	void findErrandExtraParameters() {

		// Arrange
		final var spy = Mockito.spy(errandExtraParameterService);
		final var errand = createErrandEntity().withExtraParameters(List.of(ExtraParameterEntity.builder().withKey(PARAMETER_KEY).withValues(List.of(PARAMETER_VALUE)).build()));
		when(errandRepositoryMock.findByIdAndMunicipalityIdAndNamespace(ERRAND_ID, MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.of(errand));

		// Act
		final var result = spy.findErrandExtraParameters(NAMESPACE, MUNICIPALITY_ID, ERRAND_ID);

		// Assert
		assertThat(result).hasSize(1).isEqualTo(List.of(ExtraParameter.builder().withKey(PARAMETER_KEY).withValues(List.of(PARAMETER_VALUE)).build()));
		verify(spy).findErrandExtraParameters(NAMESPACE, MUNICIPALITY_ID, ERRAND_ID);
		verify(spy).findExistingErrand(ERRAND_ID, NAMESPACE, MUNICIPALITY_ID);
		verify(errandRepositoryMock).findByIdAndMunicipalityIdAndNamespace(ERRAND_ID, MUNICIPALITY_ID, NAMESPACE);
		verifyNoMoreInteractions(errandRepositoryMock, spy);
	}

	@Test
	void updateErrandExtraParameter() {

		// Arrange
		final var spy = Mockito.spy(errandExtraParameterService);
		final var errand = createErrandEntity().withExtraParameters(List.of(ExtraParameterEntity.builder().withKey(PARAMETER_KEY).withValues(List.of(PARAMETER_VALUE)).build()));
		final var errandExtraParameterValues = List.of("anotherValue");

		when(errandRepositoryMock.findByIdAndMunicipalityIdAndNamespace(ERRAND_ID, MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.of(errand));
		when(errandRepositoryMock.save(errand)).thenReturn(errand);

		// Act
		final var result = spy.updateErrandExtraParameter(NAMESPACE, MUNICIPALITY_ID, ERRAND_ID, PARAMETER_KEY, errandExtraParameterValues);

		// Assert
		assertThat(result).isNotNull();
		assertThat(result.getValues()).isEqualTo(List.of("anotherValue"));
		verify(spy).findExistingErrand(ERRAND_ID, NAMESPACE, MUNICIPALITY_ID);
		verify(spy).updateErrandExtraParameter(NAMESPACE, MUNICIPALITY_ID, ERRAND_ID, PARAMETER_KEY, errandExtraParameterValues);
		verifyNoMoreInteractions(errandRepositoryMock, spy);
	}

	@Test
	void deleteErrandExtraParameter() {
		// Arrange
		final var spy = Mockito.spy(errandExtraParameterService);
		final var errand = createErrandEntity().withExtraParameters(new ArrayList<>(List.of(ExtraParameterEntity.builder().withKey(PARAMETER_KEY).withValues(List.of(PARAMETER_VALUE)).build())));
		when(errandRepositoryMock.findByIdAndMunicipalityIdAndNamespace(ERRAND_ID, MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.of(errand));

		// Act
		spy.deleteErrandExtraParameter(NAMESPACE, MUNICIPALITY_ID, ERRAND_ID, PARAMETER_KEY);

		// Assert
		verify(errandRepositoryMock).save(errandEntityArgumentCaptor.capture());
		assertThat(errandEntityArgumentCaptor.getValue().getExtraParameters()).isEmpty();
		verifyNoMoreInteractions(errandRepositoryMock);
	}

	@Test
	void findExistingErrand() {

		// Arrange
		final var errand = createErrandEntity();
		when(errandRepositoryMock.findByIdAndMunicipalityIdAndNamespace(ERRAND_ID, MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.of(errand));

		// Act
		final var result = errandExtraParameterService.findExistingErrand(ERRAND_ID, NAMESPACE, MUNICIPALITY_ID);

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
