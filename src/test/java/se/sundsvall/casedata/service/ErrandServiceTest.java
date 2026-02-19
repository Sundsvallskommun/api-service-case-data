package se.sundsvall.casedata.service;

import com.turkraft.springfilter.converter.FilterSpecificationConverter;
import generated.se.sundsvall.relation.Relation;
import generated.se.sundsvall.relation.ResourceIdentifier;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Stream;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.zalando.problem.ThrowableProblem;
import se.sundsvall.casedata.api.model.Notification;
import se.sundsvall.casedata.api.model.PatchErrand;
import se.sundsvall.casedata.integration.db.ErrandRepository;
import se.sundsvall.casedata.integration.db.model.ErrandEntity;
import se.sundsvall.casedata.integration.eventlog.EventlogIntegration;
import se.sundsvall.casedata.integration.relation.RelationClient;
import se.sundsvall.casedata.service.util.mappers.EntityMapper;
import se.sundsvall.casedata.service.util.mappers.ErrandExtraParameterMapper;
import se.sundsvall.dept44.support.Identifier;
import se.sundsvall.dept44.support.Identifier.Type;

import static java.util.Optional.empty;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatException;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.zalando.problem.Status.BAD_REQUEST;
import static org.zalando.problem.Status.NOT_FOUND;
import static se.sundsvall.casedata.TestUtil.MUNICIPALITY_ID;
import static se.sundsvall.casedata.TestUtil.NAMESPACE;
import static se.sundsvall.casedata.TestUtil.createErrand;
import static se.sundsvall.casedata.TestUtil.createErrandEntity;
import static se.sundsvall.casedata.TestUtil.createPatchErrand;
import static se.sundsvall.casedata.TestUtil.createStatus;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toErrandEntity;

@ExtendWith(MockitoExtension.class)
class ErrandServiceTest {

	private static final Random RANDOM = new Random();

	@InjectMocks
	private ErrandService errandService;

	@Spy
	private FilterSpecificationConverter filterSpecificationConverterSpy;

	@Mock
	private ErrandRepository errandRepositoryMock;

	@Mock
	private ProcessService processServiceMock;

	@Mock
	private ApplicationEventPublisher applicationEventPublisherMock;

	@Mock
	private NotificationService notificationServiceMock;

	@Mock
	private EventlogIntegration eventlogIntegrationMock;

	@Mock
	private RelationClient relationClientMock;

	@Captor
	private ArgumentCaptor<List<Long>> idListCapture;

	@Captor
	private ArgumentCaptor<Relation> relationCaptor;

	@Captor
	private ArgumentCaptor<Notification> notificationCaptor;

	private ErrandEntity mockErrandFindByIdAndMunicipalityIdAndNamespace() {
		final var errand = toErrandEntity(createErrand(), MUNICIPALITY_ID, NAMESPACE);
		errand.setId(RANDOM.nextLong(1, 1000));
		when(errandRepositoryMock.findByIdAndMunicipalityIdAndNamespace(any(), eq(MUNICIPALITY_ID), eq(NAMESPACE))).thenReturn(Optional.of(errand));
		return errand;
	}

	@AfterEach
	void afterEach() {
		Identifier.remove();
	}

	@Test
	void createWhenParkingPermit() {
		// Arrange
		final var inputErrandDTO = createErrand();
		inputErrandDTO.setCaseType("PARKING_PERMIT");
		final var inputErrand = toErrandEntity(inputErrandDTO, MUNICIPALITY_ID, NAMESPACE);
		inputErrand.setId(new Random().nextLong(1, 1000));

		when(errandRepositoryMock.save(any())).thenReturn(inputErrand);
		final var processId = UUID.randomUUID().toString();
		when(processServiceMock.startProcess(inputErrand)).thenReturn(processId);

		// Act
		errandService.create(inputErrandDTO, MUNICIPALITY_ID, NAMESPACE, null);

		// Assert
		verify(processServiceMock).startProcess(inputErrand);
		verify(errandRepositoryMock, times(2)).save(any());
		verifyNoMoreInteractions(processServiceMock, errandRepositoryMock);
	}

	@Test
	void findByIdAndMunicipalityIdAndNamespace() {

		// Arrange
		final var errand = mockErrandFindByIdAndMunicipalityIdAndNamespace();

		// Act
		errandService.findByIdAndMunicipalityIdAndNamespace(errand.getId(), MUNICIPALITY_ID, NAMESPACE);

		// Assert
		verify(errandRepositoryMock).findByIdAndMunicipalityIdAndNamespace(errand.getId(), MUNICIPALITY_ID, NAMESPACE);
	}

	@Test
	void findByIdAndMunicipalityIdAndNamespaceNotFound() {

		// Arrange
		final var errandDTO = createErrand();
		final var errand = toErrandEntity(errandDTO, MUNICIPALITY_ID, NAMESPACE);
		errand.setId(RANDOM.nextLong(1, 1000));
		when(errandRepositoryMock.findByIdAndMunicipalityIdAndNamespace(any(), eq(MUNICIPALITY_ID), eq(NAMESPACE))).thenReturn(Optional.empty());

		final var id = errand.getId();

		// Act
		final var problem = assertThrows(ThrowableProblem.class, () -> errandService.findByIdAndMunicipalityIdAndNamespace(id, MUNICIPALITY_ID, NAMESPACE));

		// Assert
		assertThat(problem.getStatus()).isEqualTo(NOT_FOUND);
		verify(errandRepositoryMock).findByIdAndMunicipalityIdAndNamespace(errand.getId(), MUNICIPALITY_ID, NAMESPACE);
	}

	@Test
	void delete() {

		// Arrange
		final var id = 1L;
		final var entity = createErrandEntity();
		when(errandRepositoryMock.findWithPessimisticLockingByIdAndMunicipalityIdAndNamespace(id, MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.of(entity));

		// Act
		errandService.delete(id, MUNICIPALITY_ID, NAMESPACE);

		// Assert
		verify(errandRepositoryMock).findWithPessimisticLockingByIdAndMunicipalityIdAndNamespace(id, MUNICIPALITY_ID, NAMESPACE);
		verify(errandRepositoryMock).delete(entity);
	}

	@Test
	void deleteWhenNotFound() {

		// Arrange
		final var id = 1L;
		when(errandRepositoryMock.findWithPessimisticLockingByIdAndMunicipalityIdAndNamespace(id, MUNICIPALITY_ID, NAMESPACE)).thenReturn(empty());

		// Act
		final var exception = assertThrows(ThrowableProblem.class, () -> errandService.delete(id, MUNICIPALITY_ID, NAMESPACE));

		// Assert
		assertThat(exception.getMessage()).isEqualTo("Not Found: Errand with id:'1' not found in namespace:'MY_NAMESPACE' for municipality with id:'2281'");
		assertThat(exception.getStatus()).isEqualTo(NOT_FOUND);

		verify(errandRepositoryMock).findWithPessimisticLockingByIdAndMunicipalityIdAndNamespace(id, MUNICIPALITY_ID, NAMESPACE);
		verify(errandRepositoryMock, never()).delete(any(ErrandEntity.class));
	}

	@Test
	void update() {

		// Arrange
		final var errand = createErrandEntity();
		final var updatedErrand = createErrandEntity();
		final var patch = createPatchErrand();
		final var executingUserId = "executingUserId";
		when(errandRepositoryMock.findWithPessimisticLockingByIdAndMunicipalityIdAndNamespace(errand.getId(), MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.of(errand));
		when(errandRepositoryMock.saveAndFlush(errand)).thenReturn(updatedErrand);

		Identifier.set(Identifier.create().withType(Type.AD_ACCOUNT).withValue(executingUserId));

		// Act
		errandService.update(errand.getId(), MUNICIPALITY_ID, NAMESPACE, patch);

		// Assert
		assertThat(errand).satisfies(e -> {
			assertThat(e.getCaseType()).isEqualTo(patch.getCaseType());
			assertThat(e.getExternalCaseId()).isEqualTo(patch.getExternalCaseId());
			assertThat(e.getPriority()).isEqualTo(patch.getPriority());
			assertThat(e.getDescription()).isEqualTo(patch.getDescription());
			assertThat(e.getCaseTitleAddition()).isEqualTo(patch.getCaseTitleAddition());
			assertThat(e.getDiaryNumber()).isEqualTo(patch.getDiaryNumber());
			assertThat(e.getPhase()).isEqualTo(patch.getPhase());
			assertThat(e.getStartDate()).isEqualTo(patch.getStartDate());
			assertThat(e.getEndDate()).isEqualTo(patch.getEndDate());
			assertThat(e.getExtraParameters())
				.containsAll(patch.getExtraParameters().stream()
					.map(parameter -> ErrandExtraParameterMapper.toErrandParameterEntity(parameter, errand))
					.toList());
		});

		verify(errandRepositoryMock).findWithPessimisticLockingByIdAndMunicipalityIdAndNamespace(errand.getId(), MUNICIPALITY_ID, NAMESPACE);
		verify(errandRepositoryMock).saveAndFlush(errand);
		verify(applicationEventPublisherMock).publishEvent(updatedErrand);
		verify(notificationServiceMock).create(eq(MUNICIPALITY_ID), eq(NAMESPACE), notificationCaptor.capture(), same(updatedErrand));

		assertThat(notificationCaptor.getValue().getDescription()).isEqualTo("Ärende uppdaterat");
		assertThat(notificationCaptor.getValue().getType()).isEqualTo("UPDATE");
		assertThat(notificationCaptor.getValue().getCreatedBy()).isEqualTo(executingUserId);
		assertThat(notificationCaptor.getValue().getErrandId()).isEqualTo(errand.getId());
		verifyNoMoreInteractions(errandRepositoryMock, processServiceMock, notificationServiceMock, applicationEventPublisherMock, eventlogIntegrationMock);
	}

	@Test
	void findAllWithoutDuplicates() {

		// Arrange
		final var errandDTO = createErrand();
		final var returnErrands = Stream.of(errandDTO, errandDTO, errandDTO, errandDTO, errandDTO)
			.map(dto -> EntityMapper.toErrandEntity(dto, MUNICIPALITY_ID, NAMESPACE))
			.toList();

		returnErrands.forEach(
			errandEntity -> errandEntity.setId(errandDTO.getId()));

		when(errandRepositoryMock.findAll(ArgumentMatchers.<Specification<ErrandEntity>>any(), any(Pageable.class))).thenReturn(new PageImpl<>(returnErrands));

		final Specification<ErrandEntity> filterSpecification = filterSpecificationConverterSpy.convert("stakeholders.firstName '*kim*' or stakeholders.lastName ~ '*kim*' or stakeholders.contactInformation.value ~ '*kim*'");
		final Pageable pageable = PageRequest.of(0, 20);

		// Act
		final var result = errandService.findAll(filterSpecification, MUNICIPALITY_ID, NAMESPACE, pageable);

		// Assert
		verify(errandRepositoryMock).findAll(ArgumentMatchers.<Specification<ErrandEntity>>any(), any(Pageable.class));

		assertThat(result).hasSize(5);
		assertThat(result.getContent().getFirst().getId()).isEqualTo(errandDTO.getId());
	}

	@Test
	void findAllWithoutNamespaceAndDuplicates() {

		// Arrange
		final var errandDTO = createErrand();
		final var returnErrands = Stream.of(errandDTO, errandDTO, errandDTO, errandDTO, errandDTO)
			.map(dto -> EntityMapper.toErrandEntity(dto, MUNICIPALITY_ID, NAMESPACE))
			.toList();

		returnErrands.forEach(
			errandEntity -> errandEntity.setId(errandDTO.getId()));

		when(errandRepositoryMock.findAll(ArgumentMatchers.<Specification<ErrandEntity>>any(), any(Pageable.class))).thenReturn(new PageImpl<>(returnErrands));

		final Specification<ErrandEntity> filterSpecification = filterSpecificationConverterSpy.convert("stakeholders.firstName '*kim*' or stakeholders.lastName ~ '*kim*' or stakeholders.contactInformation.value ~ '*kim*'");
		final Pageable pageable = PageRequest.of(0, 20);

		// Act
		final var result = errandService.findAllWithoutNamespace(filterSpecification, MUNICIPALITY_ID, pageable);

		// Assert
		verify(errandRepositoryMock).findAll(ArgumentMatchers.<Specification<ErrandEntity>>any(), any(Pageable.class));

		assertThat(result).hasSize(5);
		assertThat(result.getContent().getFirst().getId()).isEqualTo(errandDTO.getId());
	}

	@Test
	void updateWhenParkingPermit() {

		// Arrange
		final var dto = new PatchErrand();
		final var entity = new ErrandEntity();
		final var executingUserId = "executingUserId";
		entity.setCaseType("PARKING_PERMIT_RENEWAL");
		when(errandRepositoryMock.findWithPessimisticLockingByIdAndMunicipalityIdAndNamespace(1L, MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.of(entity));
		when(errandRepositoryMock.saveAndFlush(entity)).thenReturn(entity);

		Identifier.set(Identifier.create().withType(Type.AD_ACCOUNT).withValue(executingUserId));

		// Act
		errandService.update(1L, MUNICIPALITY_ID, NAMESPACE, dto);

		// Assert
		verify(errandRepositoryMock).findWithPessimisticLockingByIdAndMunicipalityIdAndNamespace(1L, MUNICIPALITY_ID, NAMESPACE);
		verify(errandRepositoryMock).saveAndFlush(entity);
		verify(applicationEventPublisherMock).publishEvent(entity);
		verify(notificationServiceMock).create(eq(MUNICIPALITY_ID), eq(NAMESPACE), notificationCaptor.capture(), same(entity));

		assertThat(notificationCaptor.getValue().getDescription()).isEqualTo("Ärende uppdaterat");
		assertThat(notificationCaptor.getValue().getType()).isEqualTo("UPDATE");
		assertThat(notificationCaptor.getValue().getCreatedBy()).isEqualTo(executingUserId);
		assertThat(notificationCaptor.getValue().getErrandId()).isEqualTo(entity.getId());
		verifyNoMoreInteractions(errandRepositoryMock, processServiceMock);
	}

	@Test
	void updateWithStatusChange() {

		// Arrange
		final var errand = createErrandEntity();
		final var updatedErrand = createErrandEntity();
		final var status = createStatus();
		final var patch = createPatchErrand();
		patch.setStatus(status);
		final var executingUserId = "executingUserId";
		when(errandRepositoryMock.findWithPessimisticLockingByIdAndMunicipalityIdAndNamespace(errand.getId(), MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.of(errand));
		when(errandRepositoryMock.saveAndFlush(errand)).thenReturn(updatedErrand);

		Identifier.set(Identifier.create().withType(Type.AD_ACCOUNT).withValue(executingUserId));

		// Act
		errandService.update(errand.getId(), MUNICIPALITY_ID, NAMESPACE, patch);

		// Assert
		verify(errandRepositoryMock).findWithPessimisticLockingByIdAndMunicipalityIdAndNamespace(errand.getId(), MUNICIPALITY_ID, NAMESPACE);
		verify(errandRepositoryMock).saveAndFlush(errand);
		verify(applicationEventPublisherMock).publishEvent(updatedErrand);
		verify(eventlogIntegrationMock).sendEventlogEvent(MUNICIPALITY_ID, updatedErrand, status);
		verify(notificationServiceMock).create(eq(MUNICIPALITY_ID), eq(NAMESPACE), notificationCaptor.capture(), same(updatedErrand));

		assertThat(notificationCaptor.getValue().getDescription()).isEqualTo("Ärende uppdaterat");
		assertThat(notificationCaptor.getValue().getType()).isEqualTo("UPDATE");
		assertThat(notificationCaptor.getValue().getCreatedBy()).isEqualTo(executingUserId);
		assertThat(notificationCaptor.getValue().getErrandId()).isEqualTo(updatedErrand.getId());
		verifyNoMoreInteractions(errandRepositoryMock, processServiceMock, notificationServiceMock, applicationEventPublisherMock, eventlogIntegrationMock);
	}

	@Test
	void createWhenReferredFromPresent() {
		final var referredFromService = "referredFromService";
		final var referredFromNamespace = "referredFromNamespace";
		final var referredFromIdentifier = "referredFromIdentifier";
		final var referredFrom = referredFromService + "," + referredFromNamespace + "," + referredFromIdentifier;

		final var errand = createErrand();
		final var savedErrand = toErrandEntity(errand, MUNICIPALITY_ID, NAMESPACE);

		savedErrand.setId(42L);

		when(errandRepositoryMock.save(any())).thenReturn(savedErrand);

		errandService.create(errand, MUNICIPALITY_ID, referredFromNamespace, referredFrom);

		verify(relationClientMock).createRelation(eq(MUNICIPALITY_ID), relationCaptor.capture());

		final var relation = relationCaptor.getValue();

		assertThat(relation.getType()).isEqualTo("REFERRED_FROM");
		assertThat(relation.getSource())
			.extracting(ResourceIdentifier::getResourceId,
				ResourceIdentifier::getType,
				ResourceIdentifier::getService,
				ResourceIdentifier::getNamespace)
			.containsExactly(
				referredFromIdentifier,
				"case",
				referredFromService,
				referredFromNamespace);
		assertThat(relation.getTarget())
			.extracting(ResourceIdentifier::getResourceId,
				ResourceIdentifier::getType,
				ResourceIdentifier::getService,
				ResourceIdentifier::getNamespace)
			.containsExactly(
				String.valueOf(savedErrand.getId()),
				"case",
				"case-data",
				referredFromNamespace);
	}

	@Test
	void createWhenReferredFromNamespaceNamespaceDoesNotMatchNamespace() {
		final var referredFromService = "referredFromService";
		final var referredFromNamespace = "referredFromNamespace";
		final var referredFromIdentifier = "referredFromIdentifier";
		final var referredFrom = referredFromService + "," + referredFromNamespace + "," + referredFromIdentifier;

		final var errand = createErrand();
		final var savedErrand = toErrandEntity(errand, MUNICIPALITY_ID, NAMESPACE);

		when(errandRepositoryMock.save(any())).thenReturn(savedErrand);

		assertThatException()
			.isThrownBy(() -> errandService.create(errand, MUNICIPALITY_ID, NAMESPACE, referredFrom))
			.asInstanceOf(InstanceOfAssertFactories.type(ThrowableProblem.class))
			.satisfies(thrownProblem -> {
				assertThat(thrownProblem.getStatus()).isEqualTo(BAD_REQUEST);
				assertThat(thrownProblem.getMessage()).endsWith("Mismatch on namespace and referred-from namespace");
			});

		verify(relationClientMock, never()).createRelation(any(), any());
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = {
		" "
	})
	void createWhenReferredFromNotPresent(String referredFrom) {
		final var errand = createErrand();
		final var savedErrand = toErrandEntity(errand, MUNICIPALITY_ID, NAMESPACE);

		when(errandRepositoryMock.save(any())).thenReturn(savedErrand);

		errandService.create(errand, MUNICIPALITY_ID, NAMESPACE, referredFrom);

		verify(relationClientMock, never()).createRelation(any(), any());
	}
}
