package se.sundsvall.casedata.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.zalando.problem.Status.NOT_FOUND;
import static se.sundsvall.casedata.TestUtil.createAppealDTO;
import static se.sundsvall.casedata.TestUtil.createDecisionDTO;
import static se.sundsvall.casedata.TestUtil.createErrand;
import static se.sundsvall.casedata.TestUtil.createErrandDTO;
import static se.sundsvall.casedata.TestUtil.createNoteDTO;
import static se.sundsvall.casedata.TestUtil.createPatchErrandDto;
import static se.sundsvall.casedata.TestUtil.createStakeholderDTO;
import static se.sundsvall.casedata.TestUtil.createStatusDTO;
import static se.sundsvall.casedata.TestUtil.getRandomStakeholderRole;
import static se.sundsvall.casedata.TestUtil.getRandomStakeholderType;
import static se.sundsvall.casedata.api.model.validation.enums.CaseType.ANMALAN_ATTEFALL;
import static se.sundsvall.casedata.api.model.validation.enums.CaseType.PARKING_PERMIT;
import static se.sundsvall.casedata.api.model.validation.enums.CaseType.PARKING_PERMIT_RENEWAL;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toErrand;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.zalando.problem.ThrowableProblem;

import com.turkraft.springfilter.converter.FilterSpecificationConverter;

import generated.se.sundsvall.parkingpermit.StartProcessResponse;
import se.sundsvall.casedata.api.model.PatchErrandDTO;
import se.sundsvall.casedata.api.model.validation.enums.StakeholderRole;
import se.sundsvall.casedata.integration.db.ErrandRepository;
import se.sundsvall.casedata.integration.db.model.Decision;
import se.sundsvall.casedata.integration.db.model.Errand;
import se.sundsvall.casedata.integration.db.model.enums.StakeholderType;
import se.sundsvall.casedata.service.util.mappers.EntityMapper;

@ExtendWith(MockitoExtension.class)
class ErrandServiceTest {

	@InjectMocks
	ErrandService errandService;

	@Spy
	private FilterSpecificationConverter filterSpecificationConverterSpy;

	@Mock
	private ErrandRepository errandRepositoryMock;

	@Mock
	private ProcessService processServiceMock;

	@Captor
	private ArgumentCaptor<List<Long>> idListCapture;

	@Captor
	private ArgumentCaptor<Errand> errandCaptor;

	@Test
	void postWhenParkingPermit() {
		final var inputErrandDTO = createErrandDTO();
		inputErrandDTO.setCaseType(PARKING_PERMIT.name());
		final var inputErrand = toErrand(inputErrandDTO);
		inputErrand.setId(new Random().nextLong(1, 1000));

		// Mock
		when(errandRepositoryMock.save(any())).thenReturn(inputErrand);
		final var startProcessResponse = new StartProcessResponse();
		startProcessResponse.setProcessId(UUID.randomUUID().toString());
		when(processServiceMock.startProcess(inputErrand)).thenReturn(startProcessResponse);

		errandService.createErrand(inputErrandDTO);

		verify(processServiceMock).startProcess(inputErrand);
		verify(errandRepositoryMock, times(2)).save(any());
		verifyNoMoreInteractions(processServiceMock, errandRepositoryMock);
	}

	@Test
	void postWhenAnmalanAttefall() {
		final var inputErrandDTO = createErrandDTO();
		inputErrandDTO.setCaseType(ANMALAN_ATTEFALL.name());
		final var inputErrand = toErrand(inputErrandDTO);
		inputErrand.setId(new Random().nextLong(1, 1000));

		// Mock
		when(errandRepositoryMock.save(any())).thenReturn(inputErrand);
		when(processServiceMock.startProcess(inputErrand)).thenReturn(null);

		errandService.createErrand(inputErrandDTO);

		verify(processServiceMock).startProcess(inputErrand);
		verify(errandRepositoryMock).save(any());
		verifyNoMoreInteractions(processServiceMock, errandRepositoryMock);
	}

	@Test
	void findById() {
		final var errand = mockErrandFindById();

		errandService.findById(errand.getId());
		verify(errandRepositoryMock, times(1)).findById(errand.getId());
	}

	@Test
	void findByIdNotFound() {
		final var errandDTO = createErrandDTO();
		final var errand = toErrand(errandDTO);
		errand.setId(new Random().nextLong(1, 1000));
		doReturn(Optional.empty()).when(errandRepositoryMock).findById(any());

		final var id = errand.getId();
		final var problem = assertThrows(ThrowableProblem.class, () -> errandService.findById(id));

		assertEquals(NOT_FOUND, problem.getStatus());
		verify(errandRepositoryMock, times(1)).findById(errand.getId());
	}

	@Test
	void deleteById() {

		// Arrange
		final var id = 1L;
		when(errandRepositoryMock.existsById(id)).thenReturn(true);

		// Act
		errandService.deleteById(id);

		// Assert
		verify(errandRepositoryMock).existsById(id);
		verify(errandRepositoryMock).deleteById(id);
	}

	@Test
	void deleteByIdNotFound() {

		// Arrange
		final var id = 1L;
		when(errandRepositoryMock.existsById(id)).thenReturn(false);

		// Act
		final var exception = assertThrows(ThrowableProblem.class, () -> errandService.deleteById(id));

		// Assert
		assertThat(exception.getMessage()).isEqualTo("Not Found: Errand with id: 1 was not found");
		assertThat(exception.getStatus()).isEqualTo(NOT_FOUND);

		verify(errandRepositoryMock).existsById(id);
		verify(errandRepositoryMock, never()).deleteById(id);
	}

	@Test
	void addStakeholderToErrandTest() {
		final var errand = createErrand();
		final var newStakeholder = createStakeholderDTO(StakeholderType.PERSON, List.of(StakeholderRole.OPERATOR.name()));
		when(errandRepositoryMock.findById(errand.getId())).thenReturn(Optional.of(errand));
		when(errandRepositoryMock.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

		final var stakeholder = errandService.addStakeholderToErrand(errand.getId(), newStakeholder);

		assertThat(stakeholder).isEqualTo(newStakeholder);
		assertThat(errand.getStakeholders()).isNotEmpty().hasSize(2);

		verify(errandRepositoryMock).findById(errand.getId());
		verify(errandRepositoryMock).save(errand);
		verify(processServiceMock).updateProcess(errand);
	}

	@Test
	void addNoteToErrandTest() {
		final var errand = createErrand();
		final var newNote = createNoteDTO();
		when(errandRepositoryMock.findById(errand.getId())).thenReturn(Optional.of(errand));
		when(errandRepositoryMock.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

		final var note = errandService.addNoteToErrand(errand.getId(), newNote);

		assertThat(note).isEqualTo(newNote);
		assertThat(errand.getNotes()).isNotEmpty().hasSize(2);

		verify(errandRepositoryMock).findById(errand.getId());
		verify(errandRepositoryMock).save(errand);
		verify(processServiceMock).updateProcess(errand);
	}

	@Test
	void addStatusToErrandTest() {
		final var errand = createErrand();
		final var newStatus = createStatusDTO();
		when(errandRepositoryMock.findById(errand.getId())).thenReturn(Optional.of(errand));
		when(errandRepositoryMock.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

		errandService.addStatusToErrand(errand.getId(), newStatus);

		assertThat(errand.getStatuses()).isNotEmpty().hasSize(2);

		verify(errandRepositoryMock).findById(errand.getId());
		verify(errandRepositoryMock).save(errand);
		verify(processServiceMock).updateProcess(errand);
	}

	@Test
	void addDecisionToErrandTest() {
		final var errand = createErrand();
		final var newDecision = createDecisionDTO();
		when(errandRepositoryMock.findById(errand.getId())).thenReturn(Optional.of(errand));
		when(errandRepositoryMock.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

		final var decisionDTO = errandService.addDecisionToErrand(errand.getId(), newDecision);

		assertThat(decisionDTO).isEqualTo(newDecision);
		assertThat(errand.getDecisions()).isNotEmpty().hasSize(2);

		verify(errandRepositoryMock).findById(errand.getId());
		verify(errandRepositoryMock).save(errand);
		verify(processServiceMock).updateProcess(errand);
	}

	@Test
	void addAppealToErrandTest() {
		final var errand = createErrand();
		errand.getDecisions().add(Decision.builder().withId(123L).build());
		final var newAppeal = createAppealDTO();
		when(errandRepositoryMock.findById(errand.getId())).thenReturn(Optional.of(errand));
		when(errandRepositoryMock.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

		final var appealDTO = errandService.addAppealToErrand(errand.getId(), newAppeal);

		assertThat(appealDTO).isEqualTo(newAppeal);
		assertThat(errand.getDecisions()).isNotEmpty().hasSize(2);

		verify(errandRepositoryMock).findById(errand.getId());
		verify(errandRepositoryMock).save(errand);
	}

	@Test
	void updateErrandTest() {
		final var errand = createErrand();
		final var patch = createPatchErrandDto();
		when(errandRepositoryMock.findById(errand.getId())).thenReturn(Optional.of(errand));

		errandService.updateErrand(errand.getId(), patch);

		assertThat(errand).satisfies(e -> {
			assertThat(e.getCaseType()).isEqualTo(patch.getCaseType().name());
			assertThat(e.getExternalCaseId()).isEqualTo(patch.getExternalCaseId());
			assertThat(e.getPriority()).isEqualTo(patch.getPriority());
			assertThat(e.getDescription()).isEqualTo(patch.getDescription());
			assertThat(e.getCaseTitleAddition()).isEqualTo(patch.getCaseTitleAddition());
			assertThat(e.getDiaryNumber()).isEqualTo(patch.getDiaryNumber());
			assertThat(e.getPhase()).isEqualTo(patch.getPhase());
			assertThat(e.getMunicipalityId()).isEqualTo(patch.getMunicipalityId());
			assertThat(e.getStartDate()).isEqualTo(patch.getStartDate());
			assertThat(e.getEndDate()).isEqualTo(patch.getEndDate());
			assertThat(e.getExtraParameters()).containsAllEntriesOf(patch.getExtraParameters());
		});

		verify(errandRepositoryMock).findById(errand.getId());
		verify(errandRepositoryMock).save(errand);
		verify(processServiceMock).updateProcess(errand);
	}

	@Test
	void deleteStakeholderOnErrand() {
		final var errand = toErrand(createErrandDTO());
		errand.setCaseType(PARKING_PERMIT_RENEWAL.name());
		final int sizeBeforeDelete = errand.getStakeholders().size();
		// Set ID on every stakeholder
		errand.getStakeholders().forEach(s -> s.setId(new Random().nextLong(1, 1000)));

		final var errandId = new Random().nextLong(1, 1000);
		when(errandRepositoryMock.findById(errandId)).thenReturn(Optional.of(errand));
		when(errandRepositoryMock.save(any())).thenReturn(errand);

		final var stakeholder = errand.getStakeholders().getFirst();

		errandService.deleteStakeholderOnErrand(errandId, stakeholder.getId());

		verify(errandRepositoryMock).save(errandCaptor.capture());
		verify(processServiceMock).updateProcess(errand);
		final var persistedErrand = errandCaptor.getValue();
		final int sizeAfterDelete = persistedErrand.getStakeholders().size();

		assertTrue(sizeAfterDelete < sizeBeforeDelete);
		assertFalse(persistedErrand.getStakeholders().contains(stakeholder));
	}

	@Test
	void replaceStatusesOnErrandTest() {
		final var errand = createErrand();
		final var statuses = List.of(createStatusDTO(), createStatusDTO(), createStatusDTO());
		when(errandRepositoryMock.findById(any(Long.class))).thenReturn(Optional.of(errand));
		when(errandRepositoryMock.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

		errandService.replaceStatusesOnErrand(123L, statuses);

		assertThat(errand.getStatuses()).isNotEmpty().hasSize(3).allSatisfy(status -> {
			assertThat(status.getDateTime()).isInstanceOf(OffsetDateTime.class).isNotNull();
			assertThat(status.getStatusType()).isInstanceOf(String.class).isNotBlank();
			assertThat(status.getDescription()).isInstanceOf(String.class).isNotBlank();
		});

		verify(errandRepositoryMock).findById(123L);
		verify(errandRepositoryMock).save(any());
		verify(processServiceMock).updateProcess(errand);
	}

	@Test
	void replaceStakeholderOnErrandTest() {
		final var errand = mockErrandFindById();
		final var stakeholders = List.of(createStakeholderDTO(getRandomStakeholderType(), List.of(getRandomStakeholderRole())));
		when(errandRepositoryMock.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

		errandService.replaceStakeholdersOnErrand(errand.getId(), stakeholders);

		assertThat(errand.getStakeholders()).isEqualTo(stakeholders.stream().map(EntityMapper::toStakeholder).toList());

		verify(errandRepositoryMock).save(errand);
		verify(processServiceMock).updateProcess(errand);
	}

	@Test
	void findAllWithoutDuplicates() {
		final var errandDTO = createErrandDTO();
		final var returnErrands = Stream.of(errandDTO, errandDTO, errandDTO, errandDTO, errandDTO)
			.map(EntityMapper::toErrand)
			.toList();

		doReturn(returnErrands).when(errandRepositoryMock).findAll(ArgumentMatchers.<Specification<Errand>>any());
		doReturn(new PageImpl<>(List.of(returnErrands.getFirst()))).when(errandRepositoryMock).findAllByIdIn(anyList(), any(Pageable.class));

		final Specification<Errand> filterSpecification = filterSpecificationConverterSpy.convert("stakeholders.firstName '*kim*' or stakeholders.lastName ~ '*kim*' or stakeholders.contactInformation.value ~ '*kim*'");

		final Pageable pageable = PageRequest.of(0, 20);
		errandService.findAll(filterSpecification, new HashMap<>(), pageable);

		verify(errandRepositoryMock, times(1)).findAllByIdIn(idListCapture.capture(), any(Pageable.class));

		assertEquals(1, idListCapture.getValue().size());
		assertEquals(errandDTO.getId(), idListCapture.getValue().getFirst());
	}

	@Test
	void getDecisionsOnErrand() {
		final var errand = mockErrandFindById();
		final var result = errandService.findDecisionsOnErrand(errand.getId());
		assertEquals(errand.getDecisions().stream().map(EntityMapper::toDecisionDto).toList(), result);
	}

	@Test
	void getDecisionsOnErrandNotFound() {
		final var errand = toErrand(createErrandDTO());
		errand.setId(new Random().nextLong(1, 1000));
		errand.setDecisions(new ArrayList<>());
		doReturn(Optional.of(errand)).when(errandRepositoryMock).findById(any());

		final var id = errand.getId();
		assertThrows(ThrowableProblem.class, () -> errandService.findDecisionsOnErrand(id));
	}

	@Test
	void deleteDecisionOnErrand() {
		final var errand = toErrand(createErrandDTO());
		errand.setCaseType(PARKING_PERMIT_RENEWAL.name());
		final int sizeBeforeDelete = errand.getDecisions().size();
		// Set ID on every decision
		errand.getDecisions().forEach(d -> d.setId(new Random().nextLong()));

		final var errandId = new Random().nextLong(1, 1000);
		final var decision = errand.getDecisions().getFirst();

		when(errandRepositoryMock.findById(errandId)).thenReturn(Optional.of(errand));
		when(errandRepositoryMock.save(any())).thenReturn(errand);

		errandService.deleteDecisionOnErrand(errandId, decision.getId());

		verify(errandRepositoryMock).save(errandCaptor.capture());
		final Errand persistedErrand = errandCaptor.getValue();
		final int sizeAfterDelete = persistedErrand.getDecisions().size();

		verify(processServiceMock).updateProcess(errand);
		assertTrue(sizeAfterDelete < sizeBeforeDelete);
		assertFalse(persistedErrand.getDecisions().contains(decision));
	}

	@Test
	void deleteNoteOnErrand() {
		// Arrange
		final var errand = toErrand(createErrandDTO());
		errand.setCaseType(ANMALAN_ATTEFALL.name());
		final int sizeBeforeDelete = errand.getNotes().size();
		// Set ID on every note
		errand.getNotes().forEach(note -> note.setId(new Random().nextLong()));

		final var errandId = new Random().nextLong(1, 1000);
		final var note = errand.getNotes().getFirst();

		when(errandRepositoryMock.findById(errandId)).thenReturn(Optional.of(errand));
		when(errandRepositoryMock.save(any())).thenReturn(errand);

		// Act
		errandService.deleteNoteOnErrand(errandId, note.getId());

		verify(errandRepositoryMock).save(errandCaptor.capture());
		final var persistedErrand = errandCaptor.getValue();
		final int sizeAfterDelete = persistedErrand.getNotes().size();

		assertTrue(sizeAfterDelete < sizeBeforeDelete);
		assertFalse(persistedErrand.getNotes().contains(note));
		verify(processServiceMock).updateProcess(errand);
		verifyNoMoreInteractions(errandRepositoryMock, processServiceMock);
	}

	@Test
	void deleteAppealOnErrand() {
		final var errand = toErrand(createErrandDTO());
		final int sizeBeforeDelete = errand.getAppeals().size();
		// Set ID on every decision
		errand.getDecisions().forEach(d -> d.setId(new Random().nextLong()));

		final var errandId = new Random().nextLong(1, 1000);
		final var appeal = errand.getAppeals().getFirst();
		appeal.setId(new Random().nextLong());

		when(errandRepositoryMock.findById(errandId)).thenReturn(Optional.of(errand));
		when(errandRepositoryMock.save(any())).thenReturn(errand);

		errandService.deleteAppealOnErrand(errandId, appeal.getId());

		verify(errandRepositoryMock).save(errandCaptor.capture());
		final Errand persistedErrand = errandCaptor.getValue();
		final int sizeAfterDelete = persistedErrand.getAppeals().size();

		assertTrue(sizeAfterDelete < sizeBeforeDelete);
		assertFalse(persistedErrand.getAppeals().contains(appeal));
	}

	private Errand mockErrandFindById() {
		final var errand = toErrand(createErrandDTO());
		errand.setId(new Random().nextLong(1, 1000));
		doReturn(Optional.of(errand)).when(errandRepositoryMock).findById(any());
		return errand;
	}

	@Test
	void testPatch() {
		final var dto = new PatchErrandDTO();
		final var entity = new Errand();
		entity.setCaseType(PARKING_PERMIT_RENEWAL.name());
		when(errandRepositoryMock.findById(1L)).thenReturn(Optional.of(entity));
		when(errandRepositoryMock.save(entity)).thenReturn(entity);

		errandService.updateErrand(1L, dto);

		verify(errandRepositoryMock).save(entity);
		verify(processServiceMock).updateProcess(entity);

		verifyNoMoreInteractions(errandRepositoryMock, processServiceMock);
	}

}
