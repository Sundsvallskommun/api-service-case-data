package se.sundsvall.casedata.service;

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
import se.sundsvall.casedata.integration.db.model.Appeal;
import se.sundsvall.casedata.integration.db.model.Errand;
import se.sundsvall.casedata.integration.db.model.enums.AppealStatus;
import se.sundsvall.casedata.integration.db.model.enums.TimelinessReview;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.casedata.TestUtil.createAppeal;
import static se.sundsvall.casedata.TestUtil.createAppealDTO;
import static se.sundsvall.casedata.TestUtil.createDecision;
import static se.sundsvall.casedata.TestUtil.createErrand;

@ExtendWith(MockitoExtension.class)
class AppealServiceTest {

	@InjectMocks
	AppealService appealService;

	@Mock
	private AppealRepository appealRepositoryMock;

	@Captor
	private ArgumentCaptor<Appeal> appealCaptor;

	@Test
	void getAppealById() {
		final Appeal appeal = createAppeal();
		appeal.setId(new Random().nextLong(1, 1000));
		final Errand errand = createErrand();
		errand.setId(new Random().nextLong(1, 1000));
		appeal.setErrand(errand);

		doReturn(Optional.of(appeal)).when(appealRepositoryMock).findById(appeal.getId());

		appealService.findById(appeal.getId());
		verify(appealRepositoryMock).findById(appeal.getId());
	}
	@Test
	void patchAppeal() {
		final Appeal appeal = createAppeal();
		appeal.setId(new Random().nextLong());

		doReturn(Optional.of(appeal)).when(appealRepositoryMock).findById(appeal.getId());

		final PatchAppealDTO patch = new PatchAppealDTO();
		patch.setDescription("New description");
		patch.setStatus(AppealStatus.REJECTED.name());
		patch.setTimelinessReview(TimelinessReview.REJECTED.name());

		appealService.updateAppeal(appeal.getId(), patch);

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

		when(appealRepositoryMock.findById(1L)).thenReturn(Optional.of(entity));

		appealService.replaceAppeal(1L, dto);

		verify(appealRepositoryMock).save(appealCaptor.capture());

		final var savedAppeal = appealCaptor.getValue();

		assertThat(savedAppeal.getDescription()).isEqualTo(dto.getDescription());
		assertThat(savedAppeal.getStatus()).isEqualTo(AppealStatus.REJECTED);
		assertThat(savedAppeal.getTimelinessReview()).isEqualTo(TimelinessReview.REJECTED);

		verify(appealRepositoryMock).findById(1L);
		verify(appealRepositoryMock).save(entity);
		verifyNoMoreInteractions(appealRepositoryMock);
	}

	@ParameterizedTest
	@MethodSource("decisionProvider")
	void putAppealWhenDecisionIsSet(Long decisionId, Long expectedDecisionId) {
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

		when(appealRepositoryMock.findById(1L)).thenReturn(Optional.of(entity));

		appealService.replaceAppeal(1L, dto);

		verify(appealRepositoryMock).save(appealCaptor.capture());

		final var savedAppeal = appealCaptor.getValue();

		assertThat(savedAppeal.getDescription()).isEqualTo(dto.getDescription());
		assertThat(savedAppeal.getStatus()).isEqualTo(AppealStatus.REJECTED);
		assertThat(savedAppeal.getTimelinessReview()).isEqualTo(TimelinessReview.REJECTED);
		Optional.ofNullable(savedAppeal.getDecision()).ifPresent(decision -> assertThat(decision.getId()).isEqualTo(expectedDecisionId));
		if (expectedDecisionId == null) {
			assertThat(savedAppeal.getDecision()).isNull();
		}
		verify(appealRepositoryMock).findById(1L);
		verify(appealRepositoryMock).save(entity);
		verifyNoMoreInteractions(appealRepositoryMock);
	}

	private static Stream<Arguments> decisionProvider() {
		return Stream.of(
			Arguments.of(2L, 2L),
			Arguments.of(999L, null));
	}
}
