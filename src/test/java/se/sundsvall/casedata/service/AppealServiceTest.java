package se.sundsvall.casedata.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.casedata.TestUtil.MUNICIPALITY_ID;
import static se.sundsvall.casedata.TestUtil.NAMESPACE;
import static se.sundsvall.casedata.TestUtil.createAppeal;
import static se.sundsvall.casedata.TestUtil.createAppealEntity;
import static se.sundsvall.casedata.TestUtil.createDecisionEntity;
import static se.sundsvall.casedata.TestUtil.createErrand;
import static se.sundsvall.casedata.TestUtil.createErrandEntity;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toErrandEntity;

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
import se.sundsvall.casedata.api.model.Notification;
import se.sundsvall.casedata.api.model.PatchAppeal;
import se.sundsvall.casedata.integration.db.AppealRepository;
import se.sundsvall.casedata.integration.db.ErrandRepository;
import se.sundsvall.casedata.integration.db.model.AppealEntity;
import se.sundsvall.casedata.integration.db.model.DecisionEntity;
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

	@Mock
	private NotificationService notificationServiceMock;

	@Captor
	private ArgumentCaptor<AppealEntity> appealCaptor;

	@Captor
	private ArgumentCaptor<Notification> notificationCaptor;

	private static Stream<Arguments> decisionProvider() {
		return Stream.of(
			Arguments.of(2L, 2L),
			Arguments.of(999L, null));
	}

	@Test
	void getAppealById() {
		// Arrange
		final var errand = createErrandEntity();
		when(errandRepositoryMock.findByIdAndMunicipalityIdAndNamespace(errand.getId(), MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.of(errand));

		// Act
		final var result = appealService.findAppealOnErrand(errand.getId(), 1L, MUNICIPALITY_ID, NAMESPACE);

		// Assert
		assertThat(result).isNotNull();
		verify(errandRepositoryMock).findByIdAndMunicipalityIdAndNamespace(errand.getId(), MUNICIPALITY_ID, NAMESPACE);
		verifyNoMoreInteractions(errandRepositoryMock);
	}

	@Test
	void updateAppeal() {
		// Arrange
		final var errand = createErrandEntity();
		final var appealEntity = errand.getAppeals().getFirst();
		when(errandRepositoryMock.findByIdAndMunicipalityIdAndNamespace(errand.getId(), MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.of(errand));

		final var patch = PatchAppeal.builder()
			.withDescription("New description")
			.withStatus(AppealStatus.REJECTED.name())
			.withTimelinessReview(TimelinessReview.REJECTED.name())
			.build();

		// Act
		appealService.updateAppeal(errand.getId(), appealEntity.getId(), MUNICIPALITY_ID, NAMESPACE, patch);

		// Assert
		verify(appealRepositoryMock).save(appealCaptor.capture());
		final var savedAppeal = appealCaptor.getValue();
		verify(errandRepositoryMock).findByIdAndMunicipalityIdAndNamespace(errand.getId(), MUNICIPALITY_ID, NAMESPACE);
		verify(notificationServiceMock).createNotification(eq(MUNICIPALITY_ID), eq(NAMESPACE), notificationCaptor.capture());

		assertThat(savedAppeal.getDescription()).isEqualTo(patch.getDescription());
		assertThat(savedAppeal.getStatus().name()).isEqualTo(patch.getStatus());
		assertThat(savedAppeal.getTimelinessReview()).isEqualTo(TimelinessReview.REJECTED);

		assertThat(notificationCaptor.getValue().getDescription()).isEqualTo("Överklagan uppdaterad");
		assertThat(notificationCaptor.getValue().getType()).isEqualTo("UPDATE");
		assertThat(notificationCaptor.getValue().getCreatedBy()).isEqualTo(errand.getCreatedBy());
		assertThat(notificationCaptor.getValue().getErrandId()).isEqualTo(errand.getId());
	}

	@Test
	void replaceAppeal() {
		// Arrange
		final var dto = createAppeal();
		dto.setDescription("New description");
		dto.setStatus(AppealStatus.REJECTED.name());
		dto.setTimelinessReview(TimelinessReview.REJECTED.name());

		final var entity = createAppealEntity();
		final var errand = createErrandEntity();
		errand.setAppeals(List.of(entity));
		when(errandRepositoryMock.findByIdAndMunicipalityIdAndNamespace(errand.getId(), MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.of(errand));

		// Act
		appealService.replaceAppeal(errand.getId(), 1L, MUNICIPALITY_ID, NAMESPACE, dto);

		// Assert
		verify(appealRepositoryMock).save(appealCaptor.capture());
		final var savedAppeal = appealCaptor.getValue();
		assertThat(savedAppeal.getDescription()).isEqualTo(dto.getDescription());
		assertThat(savedAppeal.getStatus()).isEqualTo(AppealStatus.REJECTED);
		assertThat(savedAppeal.getTimelinessReview()).isEqualTo(TimelinessReview.REJECTED);
		verify(errandRepositoryMock).findByIdAndMunicipalityIdAndNamespace(errand.getId(), MUNICIPALITY_ID, NAMESPACE);
		verify(appealRepositoryMock).save(entity);
		verify(notificationServiceMock).createNotification(eq(MUNICIPALITY_ID), eq(NAMESPACE), notificationCaptor.capture());
		verifyNoMoreInteractions(appealRepositoryMock, errandRepositoryMock);

		assertThat(notificationCaptor.getValue().getDescription()).isEqualTo("Överklagan uppdaterad");
		assertThat(notificationCaptor.getValue().getType()).isEqualTo("UPDATE");
		assertThat(notificationCaptor.getValue().getCreatedBy()).isEqualTo(errand.getCreatedBy());
		assertThat(notificationCaptor.getValue().getErrandId()).isEqualTo(errand.getId());
	}

	@ParameterizedTest
	@MethodSource("decisionProvider")
	void putAppealWhenDecisionIsSet(final Long decisionId, final Long expectedDecisionId) {
		// Arrange
		final var dto = createAppeal();
		dto.setDescription("New description");
		dto.setStatus(AppealStatus.REJECTED.name());
		dto.setTimelinessReview(TimelinessReview.REJECTED.name());
		dto.setDecisionId(decisionId);

		final var entity = createAppealEntity();
		final var currentDecision = createDecisionEntity();
		currentDecision.setId(1L);
		final var newDecision = createDecisionEntity();
		newDecision.setId(2L);
		final var errand = createErrandEntity();
		errand.setDecisions(List.of(currentDecision, newDecision));
		entity.setErrand(errand);
		errand.setAppeals(List.of(entity));
		when(errandRepositoryMock.findByIdAndMunicipalityIdAndNamespace(errand.getId(), MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.of(errand));

		// Act
		appealService.replaceAppeal(errand.getId(), 1L, MUNICIPALITY_ID, NAMESPACE, dto);

		// Assert
		verify(appealRepositoryMock).save(appealCaptor.capture());
		final var savedAppeal = appealCaptor.getValue();
		assertThat(savedAppeal.getDescription()).isEqualTo(dto.getDescription());
		assertThat(savedAppeal.getStatus()).isEqualTo(AppealStatus.REJECTED);
		assertThat(savedAppeal.getTimelinessReview()).isEqualTo(TimelinessReview.REJECTED);
		Optional.ofNullable(savedAppeal.getDecision()).ifPresent(decision -> assertThat(decision.getId()).isEqualTo(expectedDecisionId));
		if (expectedDecisionId == null) {
			assertThat(savedAppeal.getDecision()).isNull();
		}
		verify(errandRepositoryMock).findByIdAndMunicipalityIdAndNamespace(errand.getId(), MUNICIPALITY_ID, NAMESPACE);
		verify(appealRepositoryMock).save(entity);
		verify(notificationServiceMock).createNotification(eq(MUNICIPALITY_ID), eq(NAMESPACE), notificationCaptor.capture());
		verifyNoMoreInteractions(appealRepositoryMock);

		assertThat(notificationCaptor.getValue().getDescription()).isEqualTo("Överklagan uppdaterad");
		assertThat(notificationCaptor.getValue().getType()).isEqualTo("UPDATE");
		assertThat(notificationCaptor.getValue().getCreatedBy()).isEqualTo(errand.getCreatedBy());
		assertThat(notificationCaptor.getValue().getErrandId()).isEqualTo(errand.getId());
	}

	@Test
	void addAppealToErrandTest() {
		// Arrange
		final var errand = createErrandEntity();
		errand.getDecisions().add(DecisionEntity.builder().withId(123L).build());
		final var newAppeal = createAppeal();
		when(errandRepositoryMock.findByIdAndMunicipalityIdAndNamespace(errand.getId(), MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.of(errand));
		when(errandRepositoryMock.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

		// Act
		final var appeal = appealService.addAppealToErrand(errand.getId(), MUNICIPALITY_ID, NAMESPACE, newAppeal);

		// Assert
		assertThat(appeal).isEqualTo(newAppeal);
		assertThat(errand.getDecisions()).isNotEmpty().hasSize(2);
		verify(errandRepositoryMock).findByIdAndMunicipalityIdAndNamespace(errand.getId(), MUNICIPALITY_ID, NAMESPACE);
		verify(errandRepositoryMock).save(errand);
		verify(notificationServiceMock).createNotification(eq(MUNICIPALITY_ID), eq(NAMESPACE), notificationCaptor.capture());

		assertThat(notificationCaptor.getValue().getDescription()).isEqualTo("Överklagan skapad");
		assertThat(notificationCaptor.getValue().getType()).isEqualTo("UPDATE");
		assertThat(notificationCaptor.getValue().getCreatedBy()).isEqualTo(errand.getCreatedBy());
		assertThat(notificationCaptor.getValue().getErrandId()).isEqualTo(errand.getId());
	}

	@Test
	void deleteAppealOnErrand() {
		// Arrange
		final var errand = toErrandEntity(createErrand(), MUNICIPALITY_ID, NAMESPACE);
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
