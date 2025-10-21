package se.sundsvall.casedata.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.ALL_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import se.sundsvall.casedata.Application;
import se.sundsvall.casedata.api.model.ExtraParameter;
import se.sundsvall.casedata.service.ErrandExtraParameterService;

@DirtiesContext
@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class ErrandExtraParameterResourceTest {

	private static final String NAMESPACE = "namespace";

	private static final String MUNICIPALITY_ID = "2281";

	private static final Long ERRAND_ID = 1L;

	private static final String PARAMETER_KEY = "parameterKey";

	private static final String PATH = "/{municipalityId}/{namespace}/errands/{errandId}/extraparameters";

	@Autowired
	private WebTestClient webTestClient;

	@MockitoBean
	private ErrandExtraParameterService errandExtraParameterServiceMock;

	@Test
	void updateErrandExtraParameters() {
		final var requestBody = List.of(ExtraParameter.builder().withKey("key").withValues(List.of("value")).build());

		when(errandExtraParameterServiceMock.updateErrandExtraParameters(NAMESPACE, MUNICIPALITY_ID, ERRAND_ID, requestBody)).thenReturn(requestBody);

		final var response = webTestClient.patch()
			.uri(builder -> builder.path(PATH).build(Map.of("namespace", NAMESPACE, "municipalityId", MUNICIPALITY_ID, "errandId", ERRAND_ID)))
			.contentType(APPLICATION_JSON)
			.accept(APPLICATION_JSON)
			.bodyValue(requestBody)
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody(new ParameterizedTypeReference<List<ExtraParameter>>() {

			})
			.returnResult();

		assertThat(response).isNotNull();
		verify(errandExtraParameterServiceMock).updateErrandExtraParameters(NAMESPACE, MUNICIPALITY_ID, ERRAND_ID, requestBody);
		verifyNoMoreInteractions(errandExtraParameterServiceMock);
	}

	@Test
	void readErrandExtraParameter() {
		final var errandParameter = List.of("value", "value2");
		when(errandExtraParameterServiceMock.readErrandExtraParameter(NAMESPACE, MUNICIPALITY_ID, ERRAND_ID, PARAMETER_KEY)).thenReturn(errandParameter);

		final var response = webTestClient.get()
			.uri(builder -> builder.path(PATH.concat("/{parameterKey}")).build(Map.of("namespace", NAMESPACE, "municipalityId", MUNICIPALITY_ID, "errandId", ERRAND_ID, "parameterKey", PARAMETER_KEY)))
			.accept(APPLICATION_JSON)
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBodyList(String.class)
			.returnResult();

		assertThat(response).isNotNull();
		assertThat(response.getResponseBody()).satisfies(p -> {
			assertThat(p).isNotNull();
			assertThat(p).hasSize(1);
			assertThat(p).isEqualTo(List.of("[ \"value\", \"value2\" ]"));
		});

		verify(errandExtraParameterServiceMock).readErrandExtraParameter(NAMESPACE, MUNICIPALITY_ID, ERRAND_ID, PARAMETER_KEY);
		verifyNoMoreInteractions(errandExtraParameterServiceMock);
	}

	@Test
	void findErrandExtraParameters() {
		final var errandExtraParameters = List.of(
			ExtraParameter.builder().withKey("key1").withValues(List.of("value1", "value2")).build(),
			ExtraParameter.builder().withKey("key2").withValues(List.of("value3")).build());

		when(errandExtraParameterServiceMock.findErrandExtraParameters(NAMESPACE, MUNICIPALITY_ID, ERRAND_ID)).thenReturn(errandExtraParameters);

		final var response = webTestClient.get()
			.uri(builder -> builder.path(PATH).build(Map.of("namespace", NAMESPACE, "municipalityId", MUNICIPALITY_ID, "errandId", ERRAND_ID)))
			.accept(APPLICATION_JSON)
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody(new ParameterizedTypeReference<List<ExtraParameter>>() {
			})
			.returnResult();

		assertThat(response).isNotNull();
		verify(errandExtraParameterServiceMock).findErrandExtraParameters(NAMESPACE, MUNICIPALITY_ID, ERRAND_ID);
		verifyNoMoreInteractions(errandExtraParameterServiceMock);
	}

	@Test
	void updateErrandExtraParameter() {

		final var requestBody = List.of("value");

		when(errandExtraParameterServiceMock.updateErrandExtraParameter(NAMESPACE, MUNICIPALITY_ID, ERRAND_ID, PARAMETER_KEY, requestBody)).thenReturn(ExtraParameter.builder().withKey("key").withValues(List.of("value")).build());

		webTestClient.patch()
			.uri(builder -> builder.path(PATH.concat("/{parameterKey}")).build(Map.of("namespace", NAMESPACE, "municipalityId", MUNICIPALITY_ID, "errandId", ERRAND_ID, "parameterKey", PARAMETER_KEY)))
			.contentType(APPLICATION_JSON)
			.accept(APPLICATION_JSON)
			.bodyValue(requestBody)
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody(ExtraParameter.class)
			.returnResult();

		verify(errandExtraParameterServiceMock).updateErrandExtraParameter(NAMESPACE, MUNICIPALITY_ID, ERRAND_ID, PARAMETER_KEY, requestBody);
		verifyNoMoreInteractions(errandExtraParameterServiceMock);
	}

	@Test
	void deleteErrandExtraParameter() {
		webTestClient.delete()
			.uri(builder -> builder.path(PATH.concat("/{parameterKey}")).build(Map.of("namespace", NAMESPACE, "municipalityId", MUNICIPALITY_ID, "errandId", ERRAND_ID, "parameterKey", PARAMETER_KEY)))
			.exchange()
			.expectStatus().isNoContent()
			.expectHeader().contentType(ALL_VALUE);

		// Verification
		verify(errandExtraParameterServiceMock).deleteErrandExtraParameter(NAMESPACE, MUNICIPALITY_ID, ERRAND_ID, PARAMETER_KEY);
	}

}
