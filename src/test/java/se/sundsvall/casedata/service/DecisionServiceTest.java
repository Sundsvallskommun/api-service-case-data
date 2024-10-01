package se.sundsvall.casedata.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.casedata.TestUtil.MUNICIPALITY_ID;
import static se.sundsvall.casedata.TestUtil.NAMESPACE;
import static se.sundsvall.casedata.TestUtil.OBJECT_MAPPER;
import static se.sundsvall.casedata.TestUtil.createDecisionDTO;
import static se.sundsvall.casedata.TestUtil.createErrandDTO;
import static se.sundsvall.casedata.TestUtil.createExtraParameters;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toDecision;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toErrand;

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

import se.sundsvall.casedata.api.model.PatchDecisionDTO;
import se.sundsvall.casedata.integration.db.DecisionRepository;
import se.sundsvall.casedata.integration.db.model.Decision;
import se.sundsvall.casedata.integration.db.model.Errand;
import se.sundsvall.casedata.integration.db.model.enums.DecisionOutcome;

@ExtendWith(MockitoExtension.class)
class DecisionServiceTest {

	@InjectMocks
	private DecisionService decisionService;

	@Mock
	private DecisionRepository decisionRepository;

	@Captor
	private ArgumentCaptor<Decision> decisionCaptor;

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
		patch.setDescription(RandomStringUtils.random(10, true, false));
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

}
