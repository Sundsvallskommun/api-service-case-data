package se.sundsvall.casedata.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.casedata.TestUtil.MUNICIPALITY_ID;
import static se.sundsvall.casedata.TestUtil.NAMESPACE;
import static se.sundsvall.casedata.TestUtil.OBJECT_MAPPER;
import static se.sundsvall.casedata.TestUtil.createDecisionDTO;
import static se.sundsvall.casedata.TestUtil.createErrand;
import static se.sundsvall.casedata.TestUtil.createErrandDTO;
import static se.sundsvall.casedata.TestUtil.createExtraParameters;
import static se.sundsvall.casedata.api.model.validation.enums.CaseType.PARKING_PERMIT_RENEWAL;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toDecision;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toErrand;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.zalando.problem.ThrowableProblem;

import se.sundsvall.casedata.api.model.PatchDecisionDTO;
import se.sundsvall.casedata.integration.db.DecisionRepository;
import se.sundsvall.casedata.integration.db.ErrandRepository;
import se.sundsvall.casedata.integration.db.model.Decision;
import se.sundsvall.casedata.integration.db.model.Errand;
import se.sundsvall.casedata.integration.db.model.enums.DecisionOutcome;
import se.sundsvall.casedata.service.util.mappers.EntityMapper;

@ExtendWith(MockitoExtension.class)
class DecisionServiceTest {

	@InjectMocks
	private DecisionService decisionService;

	@Mock
	private DecisionRepository decisionRepository;

	@Captor
	private ArgumentCaptor<Decision> decisionCaptor;

	@Mock
	private ErrandRepository errandRepositoryMock;


	@Mock
	private ProcessService processServiceMock;

	@Test
	void patchDecisionOnErrand() throws JsonProcessingException {
		final Errand errand = toErrand(createErrandDTO(), MUNICIPALITY_ID, NAMESPACE);
		errand.setId(new Random().nextLong(1, 1000));
		final Decision decision = toDecision(createDecisionDTO(), MUNICIPALITY_ID, NAMESPACE);
		decision.setId(new Random().nextLong());
		errand.setDecisions(List.of(decision));


		final var mockDecision = OBJECT_MAPPER.readValue(OBJECT_MAPPER.writeValueAsString(decision), Decision.class);
		mockDecision.setErrand(errand);
		doReturn(Optional.of(mockDecision)).when(decisionRepository).findByIdAndMunicipalityIdAndNamespace(decision.getId(), MUNICIPALITY_ID, NAMESPACE);

		final PatchDecisionDTO patch = new PatchDecisionDTO();
		patch.setDecisionOutcome(DecisionOutcome.CANCELLATION);
		patch.setDescription(RandomStringUtils.secure().next(10, true, false));
		patch.setExtraParameters(createExtraParameters());

		decisionService.updateDecision(decision.getId(), MUNICIPALITY_ID, NAMESPACE, patch);
		Mockito.verify(decisionRepository).save(decisionCaptor.capture());
		final Decision persistedDecision = decisionCaptor.getValue();

		assertEquals(patch.getDecisionOutcome(), persistedDecision.getDecisionOutcome());
		assertEquals(patch.getDescription(), persistedDecision.getDescription());

		// ExtraParameters should contain all objects
		final Map<String, Object> extraParams = new HashMap<>();
		extraParams.putAll(patch.getExtraParameters());
		extraParams.putAll(decision.getExtraParameters());
		assertEquals(extraParams, persistedDecision.getExtraParameters());
	}

	@Test
	void testPatch() {
		final var dto = new PatchDecisionDTO();
		final var entity = new Decision();

		when(decisionRepository.findByIdAndMunicipalityIdAndNamespace(1L, MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.of(entity));

		decisionService.updateDecision(1L, MUNICIPALITY_ID, NAMESPACE, dto);

		verify(decisionRepository, times(1)).save(entity);
		verifyNoMoreInteractions(decisionRepository);
	}


	@Test
	void deleteDecisionOnErrand() {

		// Arrange
		final var errand = toErrand(createErrandDTO(), MUNICIPALITY_ID, NAMESPACE);
		errand.setCaseType(PARKING_PERMIT_RENEWAL.name());

		// Set ID on every decision
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


	private Errand mockErrandFindByIdAndMunicipalityIdAndNamespace() {
		final var errand = toErrand(createErrandDTO(), MUNICIPALITY_ID, NAMESPACE);
		errand.setId(new Random().nextLong(1, 1000));
		when(errandRepositoryMock.findByIdAndMunicipalityIdAndNamespace(any(), eq(MUNICIPALITY_ID), eq(NAMESPACE))).thenReturn(Optional.of(errand));
		return errand;
	}

	@Test
	void getDecisionsOnErrand() {

		// Arrange
		final var errand = mockErrandFindByIdAndMunicipalityIdAndNamespace();

		// Act
		final var result = decisionService.findDecisionsOnErrand(errand.getId(), MUNICIPALITY_ID, NAMESPACE);

		// Assert
		assertThat(result).isEqualTo(errand.getDecisions().stream().map(EntityMapper::toDecisionDto).toList());
	}

	@Test
	void getDecisionsOnErrandNotFound() {

		// Arrange
		final var errand = toErrand(createErrandDTO(), MUNICIPALITY_ID, NAMESPACE);
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
		final var errand = createErrand();
		final var newDecision = createDecisionDTO();
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
	}


}
