package se.sundsvall.casedata.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.casedata.TestUtil.MUNICIPALITY_ID;
import static se.sundsvall.casedata.TestUtil.NAMESPACE;
import static se.sundsvall.casedata.TestUtil.OBJECT_MAPPER;
import static se.sundsvall.casedata.TestUtil.createDecision;
import static se.sundsvall.casedata.TestUtil.createErrand;
import static se.sundsvall.casedata.TestUtil.createErrandEntity;
import static se.sundsvall.casedata.TestUtil.createExtraParameters;
import static se.sundsvall.casedata.api.model.validation.enums.CaseType.PARKING_PERMIT_RENEWAL;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toErrandEntity;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.zalando.problem.ThrowableProblem;
import se.sundsvall.casedata.api.model.Notification;
import se.sundsvall.casedata.api.model.PatchDecision;
import se.sundsvall.casedata.integration.db.DecisionRepository;
import se.sundsvall.casedata.integration.db.ErrandRepository;
import se.sundsvall.casedata.integration.db.model.DecisionEntity;
import se.sundsvall.casedata.integration.db.model.enums.DecisionOutcome;
import se.sundsvall.casedata.service.util.mappers.EntityMapper;

@ExtendWith(MockitoExtension.class)
class DecisionServiceTest {

	@Mock
	private DecisionRepository decisionRepository;

	@Mock
	private ErrandRepository errandRepositoryMock;

	@Mock
	private ProcessService processServiceMock;

	@Mock
	private NotificationService notificationServiceMock;

	@InjectMocks
	private DecisionService decisionService;

	@Captor
	private ArgumentCaptor<DecisionEntity> decisionCaptor;

	@Captor
	private ArgumentCaptor<Notification> notificationCaptor;

	@Test
	void patchDecisionOnErrand() throws JsonProcessingException {
		// Arrange
		final var errand = toErrandEntity(createErrand(), MUNICIPALITY_ID, NAMESPACE);
		final var decision = errand.getDecisions().getFirst();
		final var mockDecision = OBJECT_MAPPER.readValue(OBJECT_MAPPER.writeValueAsString(decision), DecisionEntity.class);
		mockDecision.setErrand(errand);
		when(errandRepositoryMock.findByIdAndMunicipalityIdAndNamespace(errand.getId(), MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.of(errand));
		when(decisionRepository.save(any())).thenReturn(mockDecision);
		final PatchDecision patch = new PatchDecision();
		patch.setDecisionOutcome(DecisionOutcome.CANCELLATION);
		patch.setDescription(RandomStringUtils.secure().next(10, true, false));
		patch.setExtraParameters(createExtraParameters());

		// Act
		decisionService.updateDecisionOnErrand(errand.getId(), decision.getId(), MUNICIPALITY_ID, NAMESPACE, patch);

		// Assert
		verify(decisionRepository).save(decisionCaptor.capture());
		final DecisionEntity persistedDecision = decisionCaptor.getValue();
		assertEquals(patch.getDecisionOutcome(), persistedDecision.getDecisionOutcome());
		assertEquals(patch.getDescription(), persistedDecision.getDescription());
		final Map<String, Object> extraParams = new HashMap<>();
		extraParams.putAll(patch.getExtraParameters());
		extraParams.putAll(decision.getExtraParameters());
		assertEquals(extraParams, persistedDecision.getExtraParameters());

		verify(notificationServiceMock).createNotification(eq(MUNICIPALITY_ID), eq(NAMESPACE), notificationCaptor.capture());
		assertThat(notificationCaptor.getValue().getDescription()).isEqualTo("Beslut uppdaterat");
		assertThat(notificationCaptor.getValue().getType()).isEqualTo("UPDATE");
		assertThat(notificationCaptor.getValue().getCreatedBy()).isEqualTo(errand.getCreatedBy());
		assertThat(notificationCaptor.getValue().getErrandId()).isEqualTo(errand.getId());
	}

	@Test
	void testPatch() {
		// Arrange
		final var dto = new PatchDecision();
		final var errand = createErrandEntity();
		final var entity = errand.getDecisions().getFirst();
		when(errandRepositoryMock.findByIdAndMunicipalityIdAndNamespace(1L, MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.of(errand));

		// Act
		decisionService.updateDecisionOnErrand(1L, 1L, MUNICIPALITY_ID, NAMESPACE, dto);

		// Assert
		verify(errandRepositoryMock).findByIdAndMunicipalityIdAndNamespace(1L, MUNICIPALITY_ID, NAMESPACE);
		verify(decisionRepository, times(1)).save(entity);
		verifyNoMoreInteractions(errandRepositoryMock, decisionRepository);

		verify(notificationServiceMock).createNotification(eq(MUNICIPALITY_ID), eq(NAMESPACE), notificationCaptor.capture());
		assertThat(notificationCaptor.getValue().getDescription()).isEqualTo("Beslut uppdaterat");
		assertThat(notificationCaptor.getValue().getType()).isEqualTo("UPDATE");
		assertThat(notificationCaptor.getValue().getCreatedBy()).isEqualTo(errand.getCreatedBy());
		assertThat(notificationCaptor.getValue().getErrandId()).isEqualTo(errand.getId());
	}

	@Test
	void deleteDecisionOnErrand() {
		// Arrange
		final var errand = toErrandEntity(createErrand(), MUNICIPALITY_ID, NAMESPACE);
		errand.setCaseType(PARKING_PERMIT_RENEWAL.name());
		errand.getDecisions().forEach(d -> d.setId(new Random().nextLong()));
		final var errandId = new Random().nextLong(1, 1000);
		final var decision = errand.getDecisions().getFirst();
		when(errandRepositoryMock.findByIdAndMunicipalityIdAndNamespace(errandId, MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.of(errand));

		// Act
		decisionService.deleteDecisionOnErrand(errandId, MUNICIPALITY_ID, NAMESPACE, decision.getId());

		// Assert
		verify(errandRepositoryMock).findByIdAndMunicipalityIdAndNamespace(errandId, MUNICIPALITY_ID, NAMESPACE);
		verify(errandRepositoryMock).save(errand);
		verify(processServiceMock).updateProcess(errand);
		verifyNoMoreInteractions(errandRepositoryMock, processServiceMock);
	}

	@Test
	void getDecisionsOnErrand() {
		// Arrange
		final var errand = toErrandEntity(createErrand(), MUNICIPALITY_ID, NAMESPACE);
		errand.setId(new Random().nextLong(1, 1000));
		when(errandRepositoryMock.findByIdAndMunicipalityIdAndNamespace(any(), eq(MUNICIPALITY_ID), eq(NAMESPACE))).thenReturn(Optional.of(errand));

		// Act
		final var result = decisionService.findDecisionsOnErrand(errand.getId(), MUNICIPALITY_ID, NAMESPACE);

		// Assert
		assertThat(result).isEqualTo(errand.getDecisions().stream().map(EntityMapper::toDecision).toList());
	}

	@Test
	void getDecisionsOnErrandNotFound() {
		// Arrange
		final var errand = toErrandEntity(createErrand(), MUNICIPALITY_ID, NAMESPACE);
		errand.setId(new Random().nextLong(1, 1000));
		errand.setDecisions(new ArrayList<>());
		when(errandRepositoryMock.findByIdAndMunicipalityIdAndNamespace(any(), eq(MUNICIPALITY_ID), eq(NAMESPACE))).thenReturn(Optional.of(errand));
		final var id = errand.getId();

		// Act/Assert
		assertThrows(ThrowableProblem.class, () -> decisionService.findDecisionsOnErrand(id, MUNICIPALITY_ID, NAMESPACE));
	}

	@Test
	void addDecisionToErrandTest() {
		// Arrange
		final var errand = createErrandEntity();
		final var newDecision = createDecision();
		when(errandRepositoryMock.findByIdAndMunicipalityIdAndNamespace(errand.getId(), MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.of(errand));
		when(errandRepositoryMock.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

		// Act
		final var decisionDTO = decisionService.addDecisionToErrand(errand.getId(), MUNICIPALITY_ID, NAMESPACE, newDecision);

		// Assert
		assertThat(decisionDTO).isEqualTo(newDecision);
		assertThat(errand.getDecisions()).isNotEmpty().hasSize(2);
		verify(errandRepositoryMock).findByIdAndMunicipalityIdAndNamespace(errand.getId(), MUNICIPALITY_ID, NAMESPACE);
		verify(errandRepositoryMock).save(errand);
		verify(processServiceMock).updateProcess(errand);

		verify(notificationServiceMock).createNotification(eq(MUNICIPALITY_ID), eq(NAMESPACE), notificationCaptor.capture());
		assertThat(notificationCaptor.getValue().getDescription()).isEqualTo("Beslut skapat");
		assertThat(notificationCaptor.getValue().getType()).isEqualTo("UPDATE");
		assertThat(notificationCaptor.getValue().getCreatedBy()).isEqualTo(errand.getCreatedBy());
		assertThat(notificationCaptor.getValue().getErrandId()).isEqualTo(errand.getId());
	}

}
