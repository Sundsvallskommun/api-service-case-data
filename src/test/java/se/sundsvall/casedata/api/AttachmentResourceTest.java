package se.sundsvall.casedata.api;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.multipart.MultipartFile;
import se.sundsvall.casedata.Application;
import se.sundsvall.casedata.api.model.Attachment;
import se.sundsvall.casedata.api.model.validation.enums.AttachmentCategory;
import se.sundsvall.casedata.service.AttachmentService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.ALL_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;
import static org.springframework.http.MediaType.TEXT_PLAIN;
import static org.springframework.web.reactive.function.BodyInserters.fromMultipartData;
import static se.sundsvall.casedata.TestUtil.MUNICIPALITY_ID;
import static se.sundsvall.casedata.TestUtil.NAMESPACE;
import static se.sundsvall.casedata.TestUtil.createAttachment;
import static se.sundsvall.casedata.TestUtil.createAttachmentEntity;

@AutoConfigureWebTestClient
@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class AttachmentResourceTest {

	private static final String BASE_URL = "/{municipalityId}/{namespace}/errands/{errandId}/attachments";

	@MockitoBean
	private AttachmentService attachmentServiceMock;

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void getAttachmentByAttachmentId() {
		// Arrange
		final var errandId = 123L;
		final var attachmentId = 456L;

		// Act
		webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(BASE_URL + "/{attachmentId}").build(MUNICIPALITY_ID, NAMESPACE, errandId, attachmentId))
			.exchange()
			.expectStatus().isOk();

		// Assert
		verify(attachmentServiceMock).findAttachmentAsStreamedResponse(eq(errandId), eq(attachmentId), eq(MUNICIPALITY_ID), eq(NAMESPACE), any());
		verifyNoMoreInteractions(attachmentServiceMock);
	}

	@Test
	void getAttachmentsByErrandId() {
		// Arrange
		final var errandId = 12345L;
		final var attachment = createAttachment(AttachmentCategory.OTHER_ATTACHMENT);
		when(attachmentServiceMock.findAttachments(errandId, MUNICIPALITY_ID, NAMESPACE)).thenReturn(List.of(attachment));

		// Act
		final var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(BASE_URL).build(MUNICIPALITY_ID, NAMESPACE, errandId))
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBodyList(Attachment.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response).hasSize(1);
		verify(attachmentServiceMock).findAttachments(errandId, MUNICIPALITY_ID, NAMESPACE);
		verifyNoMoreInteractions(attachmentServiceMock);
	}

	@Test
	void postAttachment() {
		// Arrange
		final var errandId = 123L;
		final var attachmentId = 456L;
		final var attachment = createAttachmentEntity();
		attachment.setId(attachmentId);

		when(attachmentServiceMock.create(eq(errandId), any(Attachment.class), any(MultipartFile.class), eq(MUNICIPALITY_ID), eq(NAMESPACE))).thenReturn(attachment);

		final var multipartBodyBuilder = new MultipartBodyBuilder();
		multipartBodyBuilder.part("attachment", "{\"category\":\"MEDICAL_CONFIRMATION\",\"name\":\"document.pdf\"}");
		multipartBodyBuilder.part("file", "file-content").filename("document.pdf").contentType(TEXT_PLAIN);

		// Act
		webTestClient.post()
			.uri(uriBuilder -> uriBuilder.path(BASE_URL).build(MUNICIPALITY_ID, NAMESPACE, errandId))
			.contentType(MULTIPART_FORM_DATA)
			.body(fromMultipartData(multipartBodyBuilder.build()))
			.exchange()
			.expectStatus().isCreated()
			.expectHeader().contentType(ALL_VALUE)
			.expectHeader().location("/2281/MY_NAMESPACE/errands/" + errandId + "/attachments/" + attachmentId);

		// Assert
		verify(attachmentServiceMock).create(eq(errandId), any(Attachment.class), any(MultipartFile.class), eq(MUNICIPALITY_ID), eq(NAMESPACE));
		verifyNoMoreInteractions(attachmentServiceMock);
	}

	@Test
	void postAttachmentWithEmptyFile() {
		// Arrange
		final var errandId = 123L;
		final var multipartBodyBuilder = new MultipartBodyBuilder();
		multipartBodyBuilder.part("attachment", "{\"category\":\"MEDICAL_CONFIRMATION\",\"name\":\"document.pdf\"}");
		multipartBodyBuilder.part("file", new byte[0]).filename("empty.pdf").contentType(TEXT_PLAIN);

		// Act
		webTestClient.post()
			.uri(uriBuilder -> uriBuilder.path(BASE_URL).build(MUNICIPALITY_ID, NAMESPACE, errandId))
			.contentType(MULTIPART_FORM_DATA)
			.body(fromMultipartData(multipartBodyBuilder.build()))
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody()
			.jsonPath("$.title").isEqualTo("Bad Request")
			.jsonPath("$.status").isEqualTo(400)
			.jsonPath("$.detail").isEqualTo("The 'file' part must not be empty");

		// Assert
		verifyNoInteractions(attachmentServiceMock);
	}

	@Test
	void postAttachmentWithoutFilePart() {
		// Arrange - the 'file' part is mandatory; omitting it must fail as a client error, not a server error.
		final var errandId = 123L;
		final var multipartBodyBuilder = new MultipartBodyBuilder();
		multipartBodyBuilder.part("attachment", "{\"category\":\"MEDICAL_CONFIRMATION\",\"name\":\"document.pdf\"}");

		// Act
		webTestClient.post()
			.uri(uriBuilder -> uriBuilder.path(BASE_URL).build(MUNICIPALITY_ID, NAMESPACE, errandId))
			.contentType(MULTIPART_FORM_DATA)
			.body(fromMultipartData(multipartBodyBuilder.build()))
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody()
			.jsonPath("$.status").isEqualTo(400);

		// Assert
		verifyNoInteractions(attachmentServiceMock);
	}

	@Test
	void postAttachmentWithMalformedMetadata() {
		// Arrange - the 'attachment' part is parsed by hand (it is a string part, not a @RequestBody), so unparsable
		// JSON must surface as a client error rather than escaping as an unhandled exception.
		final var errandId = 123L;
		final var multipartBodyBuilder = new MultipartBodyBuilder();
		multipartBodyBuilder.part("attachment", "{\"category\":\"MEDICAL_CONFIRMATION\",");
		multipartBodyBuilder.part("file", "file-content").filename("document.pdf").contentType(TEXT_PLAIN);

		// Act
		webTestClient.post()
			.uri(uriBuilder -> uriBuilder.path(BASE_URL).build(MUNICIPALITY_ID, NAMESPACE, errandId))
			.contentType(MULTIPART_FORM_DATA)
			.body(fromMultipartData(multipartBodyBuilder.build()))
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody()
			.jsonPath("$.title").isEqualTo("Bad Request")
			.jsonPath("$.status").isEqualTo(400)
			.jsonPath("$.detail").isEqualTo("The 'attachment' part must be valid JSON");

		// Assert
		verifyNoInteractions(attachmentServiceMock);
	}

	@Test
	void postAttachmentWithInvalidCategory() {
		// Arrange - metadata in the multipart part is validated by hand in the resource; the violation must be
		// reported in the same shape as a @Valid @RequestBody violation.
		final var errandId = 123L;
		final var multipartBodyBuilder = new MultipartBodyBuilder();
		multipartBodyBuilder.part("attachment", "{\"category\":\"NOT_A_VALID_CATEGORY\",\"name\":\"document.pdf\"}");
		multipartBodyBuilder.part("file", "file-content").filename("document.pdf").contentType(TEXT_PLAIN);

		// Act
		webTestClient.post()
			.uri(uriBuilder -> uriBuilder.path(BASE_URL).build(MUNICIPALITY_ID, NAMESPACE, errandId))
			.contentType(MULTIPART_FORM_DATA)
			.body(fromMultipartData(multipartBodyBuilder.build()))
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody()
			.jsonPath("$.title").isEqualTo("Constraint Violation")
			.jsonPath("$.violations[0].field").isEqualTo("category")
			.jsonPath("$.violations[0].message").<String>value(message -> assertThat(message).startsWith("Invalid attachment category."));

		// Assert
		verifyNoInteractions(attachmentServiceMock);
	}

	@Test
	void patchAttachmentWithInvalidCategory() {
		// Arrange
		final var errandId = 123L;
		final var attachmentId = 456L;

		// Act
		webTestClient.patch()
			.uri(uriBuilder -> uriBuilder.path(BASE_URL + "/{attachmentId}").build(MUNICIPALITY_ID, NAMESPACE, errandId, attachmentId))
			.contentType(APPLICATION_JSON)
			.bodyValue(Map.of("category", "NOT_A_VALID_CATEGORY"))
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody()
			.jsonPath("$.title").isEqualTo("Constraint Violation")
			.jsonPath("$.violations[0].field").isEqualTo("category");

		// Assert
		verifyNoInteractions(attachmentServiceMock);
	}

	@Test
	void patchAttachment() {
		// Arrange
		final var errandId = 123L;
		final var attachmentId = 456L;
		final var body = createAttachment(AttachmentCategory.CORPORATE_TAX_CARD);
		body.setId(attachmentId);

		// Act
		webTestClient.patch()
			.uri(uriBuilder -> uriBuilder.path(BASE_URL + "/{attachmentId}").build(MUNICIPALITY_ID, NAMESPACE, errandId, attachmentId))
			.contentType(APPLICATION_JSON)
			.bodyValue(body)
			.exchange()
			.expectStatus().isNoContent()
			.expectHeader().contentType(ALL_VALUE);

		// Assert
		verify(attachmentServiceMock).update(errandId, attachmentId, MUNICIPALITY_ID, NAMESPACE, body);
	}

	@Test
	void deleteAttachment() {
		// Arrange
		final var errandId = 123L;
		final var attachmentId = 456L;

		// Act
		webTestClient.delete()
			.uri(uriBuilder -> uriBuilder.path(BASE_URL + "/{attachmentId}").build(MUNICIPALITY_ID, NAMESPACE, errandId, attachmentId))
			.exchange()
			.expectStatus().isNoContent()
			.expectHeader().contentType(ALL_VALUE);

		// Assert
		verify(attachmentServiceMock).delete(errandId, attachmentId, MUNICIPALITY_ID, NAMESPACE);
	}
}
