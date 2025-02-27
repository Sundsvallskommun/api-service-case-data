package se.sundsvall.casedata.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.ALL_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.TEXT_PLAIN;
import static se.sundsvall.casedata.TestUtil.MUNICIPALITY_ID;
import static se.sundsvall.casedata.TestUtil.NAMESPACE;
import static se.sundsvall.casedata.TestUtil.createAttachment;
import static se.sundsvall.casedata.TestUtil.createAttachmentEntity;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import se.sundsvall.casedata.Application;
import se.sundsvall.casedata.api.model.Attachment;
import se.sundsvall.casedata.api.model.validation.enums.AttachmentCategory;
import se.sundsvall.casedata.service.AttachmentService;

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
		final var attachment = createAttachment(AttachmentCategory.SIGNATURE);
		when(attachmentServiceMock.findAttachment(errandId, attachmentId, MUNICIPALITY_ID, NAMESPACE)).thenReturn(attachment);

		// Act
		final var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(BASE_URL + "/{attachmentId}").build(MUNICIPALITY_ID, NAMESPACE, errandId, attachmentId))
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody(Attachment.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response).isNotNull();
		verify(attachmentServiceMock).findAttachment(errandId, attachmentId, MUNICIPALITY_ID, NAMESPACE);
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
		final var request = createAttachment(AttachmentCategory.MEDICAL_CONFIRMATION);
		final var attachment = createAttachmentEntity();
		attachment.setId(attachmentId);
		request.setId(attachmentId);
		final var file = "file";

		final var multipartBodyBuilder = new MultipartBodyBuilder();
		multipartBodyBuilder.part("file", file).filename("file.txt").contentType(TEXT_PLAIN);

		multipartBodyBuilder.part("attachment", request, APPLICATION_JSON);
		final var body = multipartBodyBuilder.build();

		when(attachmentServiceMock.create(eq(errandId), eq(request), eq(MUNICIPALITY_ID), eq(NAMESPACE), any())).thenReturn(attachment);

		// Act
		webTestClient.post()
			.uri(uriBuilder -> uriBuilder.path(BASE_URL).build(MUNICIPALITY_ID, NAMESPACE, errandId))
			.contentType(MediaType.MULTIPART_FORM_DATA)
			.body(BodyInserters.fromMultipartData(body))
			.exchange()
			.expectStatus().isCreated()
			.expectHeader().contentType(ALL_VALUE)
			.expectHeader().location("/2281/MY_NAMESPACE/errands/" + errandId + "/attachments/" + attachmentId);

		// Assert
		verify(attachmentServiceMock).create(eq(errandId), eq(request), eq(MUNICIPALITY_ID), eq(NAMESPACE), any());
	}

	@Test
	void putAttachmentOnErrand() {
		// Arrange
		final var errandId = 123L;
		final var attachmentId = 456L;
		final var body = createAttachment(AttachmentCategory.OTHER);
		body.setId(attachmentId);

		// Act
		webTestClient.put()
			.uri(uriBuilder -> uriBuilder.path(BASE_URL + "/{attachmentId}").build(MUNICIPALITY_ID, NAMESPACE, errandId, attachmentId))
			.contentType(APPLICATION_JSON)
			.bodyValue(body)
			.exchange()
			.expectStatus().isNoContent()
			.expectHeader().contentType(ALL_VALUE);

		// Assert
		verify(attachmentServiceMock).replace(errandId, attachmentId, MUNICIPALITY_ID, NAMESPACE, body);
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
		final var attachmentEntity = createAttachment(AttachmentCategory.CORPORATE_TAX_CARD);
		final var errandId = 123L;
		final var attachmentId = 456L;

		when(attachmentServiceMock.findAttachment(errandId, attachmentId, MUNICIPALITY_ID, NAMESPACE)).thenReturn(attachmentEntity);

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
