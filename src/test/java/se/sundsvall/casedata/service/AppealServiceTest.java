package se.sundsvall.casedata.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.casedata.TestUtil.MUNICIPALITY_ID;
import static se.sundsvall.casedata.TestUtil.NAMESPACE;
import static se.sundsvall.casedata.TestUtil.createAppeal;
import static se.sundsvall.casedata.TestUtil.createAppealDTO;
import static se.sundsvall.casedata.TestUtil.createDecision;
import static se.sundsvall.casedata.TestUtil.createErrand;
import static se.sundsvall.casedata.TestUtil.createErrandDTO;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toErrand;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import se.sundsvall.casedata.api.model.PatchAppealDTO;
import se.sundsvall.casedata.integration.db.AppealRepository;
import se.sundsvall.casedata.integration.db.ErrandRepository;
import se.sundsvall.casedata.integration.db.model.Appeal;
import se.sundsvall.casedata.integration.db.model.Decision;
import se.sundsvall.casedata.integration.db.model.enums.AppealStatus;
import se.sundsvall.casedata.integration.db.model.enums.TimelinessReview;

@ExtendWith(MockitoExtension.class)
class AppealServiceTest {

	@InjectMocks
	private AppealService appealService;

	@Mock
	private AppealRepository appealRepositoryMock;

	@Mock
	private ErrandRepository errandRepositoryMock;

	@Mock
	private ProcessService processServiceMock;

	@Captor
	private ArgumentCaptor<Appeal> appealCaptor;

	private static Stream<Arguments> decisionProvider() {
		return Stream.of(
			Arguments.of(2L, 2L),
			Arguments.of(999L, null));
	}

	@Test
	void getAppealById() {
		final Appeal appeal = createAppeal();

		when(appealRepositoryMock.findByIdAndMunicipalityIdAndNamespace(1L, MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.of(appeal));

		var result = appealService.findByIdAndMunicipalityIdAndNamespace(appeal.getId(), MUNICIPALITY_ID, NAMESPACE);

		assertThat(result).isNotNull();

		verify(appealRepositoryMock).findByIdAndMunicipalityIdAndNamespace(appeal.getId(), MUNICIPALITY_ID, NAMESPACE);
		verifyNoMoreInteractions(appealRepositoryMock);
	}

	@Test
	void patchAppeal() {
		final Appeal appeal = createAppeal();
		appeal.setId(new Random().nextLong());

		doReturn(Optional.of(appeal)).when(appealRepositoryMock).findByIdAndMunicipalityIdAndNamespace(appeal.getId(), MUNICIPALITY_ID, NAMESPACE);

		final PatchAppealDTO patch = new PatchAppealDTO();
		patch.setDescription("New description");
		patch.setStatus(AppealStatus.REJECTED.name());
		patch.setTimelinessReview(TimelinessReview.REJECTED.name());

		appealService.updateAppeal(appeal.getId(), MUNICIPALITY_ID, NAMESPACE, patch);

		verify(appealRepositoryMock).save(appealCaptor.capture());

		final var savedAppeal = appealCaptor.getValue();

		assertThat(savedAppeal.getDescription()).isEqualTo(patch.getDescription());
		assertThat(savedAppeal.getStatus().name()).isEqualTo(patch.getStatus());
		assertThat(savedAppeal.getTimelinessReview()).isEqualTo(TimelinessReview.REJECTED);
	}

	@Test
	void putAppeal() {
		final var dto = createAppealDTO();
		dto.setDescription("New description");
		dto.setStatus(AppealStatus.REJECTED.name());
		dto.setTimelinessReview(TimelinessReview.REJECTED.name());

		final var entity = createAppeal();
		entity.setErrand(createErrand());

		when(appealRepositoryMock.findByIdAndMunicipalityIdAndNamespace(1L, MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.of(entity));

		appealService.replaceAppeal(1L, MUNICIPALITY_ID, NAMESPACE, dto);

		verify(appealRepositoryMock).save(appealCaptor.capture());

		final var savedAppeal = appealCaptor.getValue();

		assertThat(savedAppeal.getDescription()).isEqualTo(dto.getDescription());
		assertThat(savedAppeal.getStatus()).isEqualTo(AppealStatus.REJECTED);
		assertThat(savedAppeal.getTimelinessReview()).isEqualTo(TimelinessReview.REJECTED);

		verify(appealRepositoryMock).findByIdAndMunicipalityIdAndNamespace(1L, MUNICIPALITY_ID, NAMESPACE);
		verify(appealRepositoryMock).save(entity);
		verifyNoMoreInteractions(appealRepositoryMock);
	}

	@ParameterizedTest
	@MethodSource("decisionProvider")
	void putAppealWhenDecisionIsSet(final Long decisionId, final Long expectedDecisionId) {
		final var dto = createAppealDTO();
		dto.setDescription("New description");
		dto.setStatus(AppealStatus.REJECTED.name());
		dto.setTimelinessReview(TimelinessReview.REJECTED.name());
		dto.setDecisionId(decisionId);

		final var entity = createAppeal();

		final var currentDecision = createDecision();
		currentDecision.setId(1L);

		final var newDecision = createDecision();
		newDecision.setId(2L);

		final var errand = createErrand();
		errand.setDecisions(List.of(currentDecision, newDecision));
		entity.setErrand(errand);

		when(appealRepositoryMock.findByIdAndMunicipalityIdAndNamespace(1L, MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.of(entity));

		appealService.replaceAppeal(1L, MUNICIPALITY_ID, NAMESPACE, dto);

		verify(appealRepositoryMock).save(appealCaptor.capture());

		final var savedAppeal = appealCaptor.getValue();

		assertThat(savedAppeal.getDescription()).isEqualTo(dto.getDescription());
		assertThat(savedAppeal.getStatus()).isEqualTo(AppealStatus.REJECTED);
		assertThat(savedAppeal.getTimelinessReview()).isEqualTo(TimelinessReview.REJECTED);
		Optional.ofNullable(savedAppeal.getDecision()).ifPresent(decision -> assertThat(decision.getId()).isEqualTo(expectedDecisionId));
		if (expectedDecisionId == null) {
			assertThat(savedAppeal.getDecision()).isNull();
		}
		verify(appealRepositoryMock).findByIdAndMunicipalityIdAndNamespace(1L, MUNICIPALITY_ID, NAMESPACE);
		verify(appealRepositoryMock).save(entity);
		verifyNoMoreInteractions(appealRepositoryMock);
	}

	@Test
	void addAppealToErrandTest() {

		// Arrange
		final var errand = createErrand();
		errand.getDecisions().add(Decision.builder().withId(123L).build());
		final var newAppeal = createAppealDTO();
		when(errandRepositoryMock.findByIdAndMunicipalityIdAndNamespace(errand.getId(), MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.of(errand));
		when(errandRepositoryMock.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

		// Act
		final var appealDTO = appealService.addAppealToErrand(errand.getId(), MUNICIPALITY_ID, NAMESPACE, newAppeal);

		// Assert
		assertThat(appealDTO).isEqualTo(newAppeal);
		assertThat(errand.getDecisions()).isNotEmpty().hasSize(2);

		verify(errandRepositoryMock).findByIdAndMunicipalityIdAndNamespace(errand.getId(), MUNICIPALITY_ID, NAMESPACE);
		verify(errandRepositoryMock).save(errand);
	}

	@Test
	void deleteAppealOnErrand() {

		// Arrange
		final var errand = toErrand(createErrandDTO(), MUNICIPALITY_ID, NAMESPACE);
		// Set ID on every decision
		errand.getDecisions().forEach(d -> d.setId(new Random().nextLong()));

		final var errandId = new Random().nextLong(1, 1000);
		final var appeal = errand.getAppeals().getFirst();
		appeal.setId(new Random().nextLong());

		when(errandRepositoryMock.findByIdAndMunicipalityIdAndNamespace(errandId, MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.of(errand));

		// Act
		appealService.deleteAppealOnErrand(errandId, MUNICIPALITY_ID, NAMESPACE, appeal.getId());

		// Assert
		verify(errandRepositoryMock).findByIdAndMunicipalityIdAndNamespace(errandId, MUNICIPALITY_ID, NAMESPACE);
		verify(errandRepositoryMock).save(errand);
		verify(processServiceMock).updateProcess(errand);
	}

}
