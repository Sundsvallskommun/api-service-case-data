package se.sundsvall.casedata.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.casedata.TestUtil.MUNICIPALITY_ID;
import static se.sundsvall.casedata.TestUtil.NAMESPACE;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;
import se.sundsvall.casedata.api.model.CaseType;
import se.sundsvall.casedata.integration.db.CaseTypeRepository;
import se.sundsvall.casedata.integration.db.model.CaseTypeEntity;

@ExtendWith(MockitoExtension.class)
class MetadataServiceTest {

	@Mock
	private CaseTypeRepository caseTypeRepositoryMock;

	@InjectMocks
	private MetadataService metadataService;

	@Test
	void getCaseTypes() {
		// Arrange
		final var entity1 = CaseTypeEntity.builder()
			.withType("TYPE_1")
			.withDisplayName("Display 1")
			.withMunicipalityId(MUNICIPALITY_ID)
			.withNamespace(NAMESPACE)
			.build();
		final var entity2 = CaseTypeEntity.builder()
			.withType("TYPE_2")
			.withDisplayName("Display 2")
			.withMunicipalityId(MUNICIPALITY_ID)
			.withNamespace(NAMESPACE)
			.build();

		when(caseTypeRepositoryMock.findAllByMunicipalityIdAndNamespace(MUNICIPALITY_ID, NAMESPACE))
			.thenReturn(List.of(entity1, entity2));

		// Act
		final var result = metadataService.getCaseTypes(MUNICIPALITY_ID, NAMESPACE);

		// Assert
		assertThat(result).hasSize(2);
		assertThat(result.getFirst().getType()).isEqualTo("TYPE_1");
		assertThat(result.getFirst().getDisplayName()).isEqualTo("Display 1");
		assertThat(result.get(1).getType()).isEqualTo("TYPE_2");
		assertThat(result.get(1).getDisplayName()).isEqualTo("Display 2");
		verify(caseTypeRepositoryMock).findAllByMunicipalityIdAndNamespace(MUNICIPALITY_ID, NAMESPACE);
		verifyNoMoreInteractions(caseTypeRepositoryMock);
	}

	@Test
	void getCaseType() {
		// Arrange
		final var type = "PARATRANSIT";
		final var entity = CaseTypeEntity.builder()
			.withType(type)
			.withDisplayName("Färdtjänst")
			.withMunicipalityId(MUNICIPALITY_ID)
			.withNamespace(NAMESPACE)
			.build();

		when(caseTypeRepositoryMock.findByMunicipalityIdAndNamespaceAndType(MUNICIPALITY_ID, NAMESPACE, type))
			.thenReturn(Optional.of(entity));

		// Act
		final var result = metadataService.getCaseType(MUNICIPALITY_ID, NAMESPACE, type);

		// Assert
		assertThat(result).isNotNull();
		assertThat(result.getType()).isEqualTo(type);
		assertThat(result.getDisplayName()).isEqualTo("Färdtjänst");
		verify(caseTypeRepositoryMock).findByMunicipalityIdAndNamespaceAndType(MUNICIPALITY_ID, NAMESPACE, type);
		verifyNoMoreInteractions(caseTypeRepositoryMock);
	}

	@Test
	void getCaseTypeNotFound() {
		// Arrange
		final var type = "MISSING";
		when(caseTypeRepositoryMock.findByMunicipalityIdAndNamespaceAndType(MUNICIPALITY_ID, NAMESPACE, type))
			.thenReturn(Optional.empty());

		// Act & Assert
		assertThatThrownBy(() -> metadataService.getCaseType(MUNICIPALITY_ID, NAMESPACE, type))
			.isInstanceOf(ThrowableProblem.class)
			.hasMessage("Not Found: CaseType not found in database")
			.satisfies(e -> assertThat(((ThrowableProblem) e).getStatus()).isEqualTo(Status.NOT_FOUND));

		verify(caseTypeRepositoryMock).findByMunicipalityIdAndNamespaceAndType(MUNICIPALITY_ID, NAMESPACE, type);
		verifyNoMoreInteractions(caseTypeRepositoryMock);
	}

	@Test
	void createCaseType() {
		// Arrange
		final var request = CaseType.builder()
			.withType("NEW_TYPE")
			.withDisplayName("New Display")
			.build();
		final var savedEntity = CaseTypeEntity.builder()
			.withType("NEW_TYPE")
			.withDisplayName("New Display")
			.withMunicipalityId(MUNICIPALITY_ID)
			.withNamespace(NAMESPACE)
			.build();

		when(caseTypeRepositoryMock.save(any(CaseTypeEntity.class))).thenReturn(savedEntity);

		// Act
		final var result = metadataService.createCaseType(MUNICIPALITY_ID, NAMESPACE, request);

		// Assert
		assertThat(result).isEqualTo("NEW_TYPE");
		final var captor = ArgumentCaptor.forClass(CaseTypeEntity.class);
		verify(caseTypeRepositoryMock).save(captor.capture());
		assertThat(captor.getValue()).satisfies(entity -> {
			assertThat(entity.getMunicipalityId()).isEqualTo(MUNICIPALITY_ID);
			assertThat(entity.getNamespace()).isEqualTo(NAMESPACE);
			assertThat(entity.getType()).isEqualTo("NEW_TYPE");
			assertThat(entity.getDisplayName()).isEqualTo("New Display");
		});
		verifyNoMoreInteractions(caseTypeRepositoryMock);
	}

	@Test
	void deleteCaseType() {
		// Arrange
		final var type = "DELETE_ME";
		final var entity = CaseTypeEntity.builder()
			.withType(type)
			.withDisplayName("To be deleted")
			.withMunicipalityId(MUNICIPALITY_ID)
			.withNamespace(NAMESPACE)
			.build();

		when(caseTypeRepositoryMock.findByMunicipalityIdAndNamespaceAndType(MUNICIPALITY_ID, NAMESPACE, type))
			.thenReturn(Optional.of(entity));

		// Act
		metadataService.deleteCaseType(MUNICIPALITY_ID, NAMESPACE, type);

		// Assert
		verify(caseTypeRepositoryMock).findByMunicipalityIdAndNamespaceAndType(MUNICIPALITY_ID, NAMESPACE, type);
		verify(caseTypeRepositoryMock).delete(entity);
		verifyNoMoreInteractions(caseTypeRepositoryMock);
	}

	@Test
	void deleteCaseTypeNotFound() {
		// Arrange
		final var type = "MISSING";
		when(caseTypeRepositoryMock.findByMunicipalityIdAndNamespaceAndType(MUNICIPALITY_ID, NAMESPACE, type))
			.thenReturn(Optional.empty());

		// Act & Assert
		assertThatThrownBy(() -> metadataService.deleteCaseType(MUNICIPALITY_ID, NAMESPACE, type))
			.isInstanceOf(ThrowableProblem.class)
			.hasMessage("Not Found: CaseType not found in database")
			.satisfies(e -> assertThat(((ThrowableProblem) e).getStatus()).isEqualTo(Status.NOT_FOUND));

		verify(caseTypeRepositoryMock).findByMunicipalityIdAndNamespaceAndType(MUNICIPALITY_ID, NAMESPACE, type);
		verify(caseTypeRepositoryMock, never()).delete(any());
		verifyNoMoreInteractions(caseTypeRepositoryMock);
	}
}
