package se.sundsvall.casedata.service.scheduler.supensions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import se.sundsvall.casedata.api.model.PatchErrand;
import se.sundsvall.casedata.integration.db.ErrandRepository;
import se.sundsvall.casedata.integration.db.model.ErrandEntity;
import se.sundsvall.casedata.service.ErrandService;


@ExtendWith(MockitoExtension.class)
class SuspensionWorkerTest {

	@Mock
	private ErrandRepository errandsRepository;

	@Mock
	private ErrandService errandService;

	@InjectMocks
	private SuspensionWorker suspensionWorker;

	@Captor
	private ArgumentCaptor<PatchErrand> errandCaptor;

	@Test
	void cleanUpSuspensions() {

		// Arrange
		final var namespace = "namespace";
		final var municipalityId = "municipalityId";

		final var errandEntity = ErrandEntity.builder()
			.withNamespace(namespace)
			.withId(1L)
			.withMunicipalityId(municipalityId)
			.withSuspendedFrom(OffsetDateTime.now().minusDays(1))
			.withSuspendedTo(OffsetDateTime.now().minusHours(1))
			.withMunicipalityId(municipalityId)
			.build();

		when(errandsRepository.findAllBySuspendedToBefore(any(OffsetDateTime.class))).thenReturn(List.of(errandEntity));

		// Act
		suspensionWorker.cleanUpSuspensions();

		// Assert
		verify(errandsRepository).findAllBySuspendedToBefore(any(OffsetDateTime.class));
		verify(errandService).updateErrand(eq(errandEntity.getId()), eq(municipalityId), eq(namespace), errandCaptor.capture());
		final var errand = errandCaptor.getValue();
		assertThat(errand).isNotNull();
		assertThat(errand.getSuspension()).isNotNull();

		verifyNoMoreInteractions(errandsRepository, errandService);
	}

	@Test
	void cleanUpSuspensionsNoSuspensions() {

		// Arrange
		when(errandsRepository.findAllBySuspendedToBefore(any(OffsetDateTime.class))).thenReturn(List.of());

		// Act
		suspensionWorker.cleanUpSuspensions();

		// Assert
		verify(errandsRepository).findAllBySuspendedToBefore(any(OffsetDateTime.class));
		verifyNoMoreInteractions(errandsRepository, errandService);
	}

}
