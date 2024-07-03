package se.sundsvall.casedata.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.casedata.TestUtil.MUNICIPALITY_ID;
import static se.sundsvall.casedata.TestUtil.createAppeal;
import static se.sundsvall.casedata.TestUtil.createAppealDTO;
import static se.sundsvall.casedata.TestUtil.createDecision;
import static se.sundsvall.casedata.TestUtil.createErrand;

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
import se.sundsvall.casedata.integration.db.model.Appeal;
import se.sundsvall.casedata.integration.db.model.enums.AppealStatus;
import se.sundsvall.casedata.integration.db.model.enums.TimelinessReview;

@ExtendWith(MockitoExtension.class)
class AppealServiceTest {

	@InjectMocks
	private AppealService appealService;

	@Mock
	private AppealRepository appealRepositoryMock;

	@Captor
	private ArgumentCaptor<Appeal> appealCaptor;

	@Test
	void getAppealById() {
		final Appeal appeal = createAppeal();

		when(appealRepositoryMock.findByIdAndMunicipalityId(1L, MUNICIPALITY_ID)).thenReturn(Optional.of(appeal));

		var result = appealService.findByIdAndMunicipalityId(appeal.getId(), MUNICIPALITY_ID);

		assertThat(result).isNotNull();

		verify(appealRepositoryMock).findByIdAndMunicipalityId(appeal.getId(), MUNICIPALITY_ID);
		verifyNoMoreInteractions(appealRepositoryMock);
	}

	@Test
	void patchAppeal() {
		final Appeal appeal = createAppeal();
		appeal.setId(new Random().nextLong());

		doReturn(Optional.of(appeal)).when(appealRepositoryMock).findByIdAndMunicipalityId(appeal.getId(), MUNICIPALITY_ID);

		final PatchAppealDTO patch = new PatchAppealDTO();
		patch.setDescription("New description");
		patch.setStatus(AppealStatus.REJECTED.name());
		patch.setTimelinessReview(TimelinessReview.REJECTED.name());

		appealService.updateAppeal(appeal.getId(), MUNICIPALITY_ID, patch);

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

		when(appealRepositoryMock.findByIdAndMunicipalityId(1L, MUNICIPALITY_ID)).thenReturn(Optional.of(entity));

		appealService.replaceAppeal(1L, MUNICIPALITY_ID, dto);

		verify(appealRepositoryMock).save(appealCaptor.capture());

		final var savedAppeal = appealCaptor.getValue();

		assertThat(savedAppeal.getDescription()).isEqualTo(dto.getDescription());
		assertThat(savedAppeal.getStatus()).isEqualTo(AppealStatus.REJECTED);
		assertThat(savedAppeal.getTimelinessReview()).isEqualTo(TimelinessReview.REJECTED);

		verify(appealRepositoryMock).findByIdAndMunicipalityId(1L, MUNICIPALITY_ID);
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

		when(appealRepositoryMock.findByIdAndMunicipalityId(1L, MUNICIPALITY_ID)).thenReturn(Optional.of(entity));

		appealService.replaceAppeal(1L, MUNICIPALITY_ID, dto);

		verify(appealRepositoryMock).save(appealCaptor.capture());

		final var savedAppeal = appealCaptor.getValue();

		assertThat(savedAppeal.getDescription()).isEqualTo(dto.getDescription());
		assertThat(savedAppeal.getStatus()).isEqualTo(AppealStatus.REJECTED);
		assertThat(savedAppeal.getTimelinessReview()).isEqualTo(TimelinessReview.REJECTED);
		Optional.ofNullable(savedAppeal.getDecision()).ifPresent(decision -> assertThat(decision.getId()).isEqualTo(expectedDecisionId));
		if (expectedDecisionId == null) {
			assertThat(savedAppeal.getDecision()).isNull();
		}
		verify(appealRepositoryMock).findByIdAndMunicipalityId(1L, MUNICIPALITY_ID);
		verify(appealRepositoryMock).save(entity);
		verifyNoMoreInteractions(appealRepositoryMock);
	}

	private static Stream<Arguments> decisionProvider() {
		return Stream.of(
			Arguments.of(2L, 2L),
			Arguments.of(999L, null));
	}
}
