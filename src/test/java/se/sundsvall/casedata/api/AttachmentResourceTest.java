package se.sundsvall.casedata.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.ALL_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static se.sundsvall.casedata.TestUtil.MUNICIPALITY_ID;
import static se.sundsvall.casedata.TestUtil.NAMESPACE;
import static se.sundsvall.casedata.TestUtil.createAttachment;
import static se.sundsvall.casedata.TestUtil.createAttachmentEntity;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import se.sundsvall.casedata.Application;
import se.sundsvall.casedata.api.model.Attachment;
import se.sundsvall.casedata.api.model.validation.enums.AttachmentCategory;
import se.sundsvall.casedata.service.AttachmentService;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class AttachmentResourceTest {

	private static final String BASE_URL = "/{municipalityId}/{namespace}/errands";

	@MockBean
	private AttachmentService attachmentServiceMock;

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void getAttachments() {
		// Arrange
		final var errandId = 123L;
		final var attachmentId = 456L;
		final var attachment = createAttachment(AttachmentCategory.SIGNATURE);
		when(attachmentServiceMock.findByIdAndMunicipalityIdAndNamespace(errandId, attachmentId, MUNICIPALITY_ID, NAMESPACE)).thenReturn(attachment);

		// Act
		var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(BASE_URL + "/{errandId}/attachments/{attachmentId}").build(MUNICIPALITY_ID, NAMESPACE, errandId, attachmentId))
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody(Attachment.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response).isNotNull();
		verify(attachmentServiceMock).findByIdAndMunicipalityIdAndNamespace(errandId, attachmentId, MUNICIPALITY_ID, NAMESPACE);
		verifyNoMoreInteractions(attachmentServiceMock);
	}

	@Test
	void getAttachmentsByErrandNumber() {
		// Arrange
		final var errandNumber = "12345";
		final var attachment = createAttachment(AttachmentCategory.NOTIFICATION);
		when(attachmentServiceMock.findByErrandNumberAndMunicipalityIdAndNamespace(errandNumber, MUNICIPALITY_ID, NAMESPACE)).thenReturn(List.of(attachment));

		// Act
		var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path("/{municipalityId}/{namespace}/attachments/errand/{errandNumber}").build(MUNICIPALITY_ID, NAMESPACE, errandNumber))
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBodyList(Attachment.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response).hasSize(1);
		verify(attachmentServiceMock).findByErrandNumberAndMunicipalityIdAndNamespace(errandNumber, MUNICIPALITY_ID, NAMESPACE);
		verifyNoMoreInteractions(attachmentServiceMock);
	}

	@Test
	void postAttachment() {
		// Arrange
		final var errandId = 123L;
		final var attachmentId = 456L;
		final var body = createAttachment(AttachmentCategory.PO_IT);
		final var attachment = createAttachmentEntity();
		attachment.setId(attachmentId);
		body.setId(attachmentId);

		when(attachmentServiceMock.createAttachment(body, MUNICIPALITY_ID, NAMESPACE)).thenReturn(attachment);

		// Act
		webTestClient.post()
			.uri(uriBuilder -> uriBuilder.path(BASE_URL + "/{errandId}/attachments").build(MUNICIPALITY_ID, NAMESPACE, errandId))
			.contentType(APPLICATION_JSON)
			.bodyValue(body)
			.exchange()
			.expectStatus().isCreated()
			.expectHeader().contentType(ALL_VALUE)
			.expectHeader().location("/2281/my.namespace/errands/" + errandId + "/attachments/" + attachmentId);

		// Assert
		verify(attachmentServiceMock).createAttachment(body, MUNICIPALITY_ID, NAMESPACE);
	}

	@Test
	void putAttachmentOnErrand() {
		// Arrange
		final var errandId = 123L;
		final var attachmentId = 456L;
		final var body = createAttachment(AttachmentCategory.ADDRESS_SHEET);
		body.setId(attachmentId);

		// Act
		webTestClient.put()
			.uri(uriBuilder -> uriBuilder.path(BASE_URL + "/{errandId}/attachments/{attachmentId}").build(MUNICIPALITY_ID, NAMESPACE, errandId, attachmentId))
			.contentType(APPLICATION_JSON)
			.bodyValue(body)
			.exchange()
			.expectStatus().isNoContent()
			.expectHeader().contentType(ALL_VALUE);

		// Assert
		verify(attachmentServiceMock).replaceAttachment(errandId, attachmentId, MUNICIPALITY_ID, NAMESPACE, body);
	}

	@Test
	void patchAttachment() {
		// Arrange
		final var errandId = 123L;
		final var attachmentId = 456L;
		final var body = createAttachment(AttachmentCategory.AIRFLOW_PROTOCOL);
		body.setId(attachmentId);

		// Act
		webTestClient.patch()
			.uri(uriBuilder -> uriBuilder.path(BASE_URL + "/{errandId}/attachments/{attachmentId}").build(MUNICIPALITY_ID, NAMESPACE, errandId, attachmentId))
			.contentType(APPLICATION_JSON)
			.bodyValue(body)
			.exchange()
			.expectStatus().isNoContent()
			.expectHeader().contentType(ALL_VALUE);

		// Assert
		verify(attachmentServiceMock).updateAttachment(errandId, attachmentId, MUNICIPALITY_ID, NAMESPACE, body);
	}

	@Test
	void deleteAttachment() {
		// Arrange
		final var errandId = 123L;
		final var attachmentId = 456L;

		when(attachmentServiceMock.deleteAttachment(errandId, attachmentId, MUNICIPALITY_ID, NAMESPACE)).thenReturn(true);

		// Act
		webTestClient.delete()
			.uri(uriBuilder -> uriBuilder.path(BASE_URL + "/{errandId}/attachments/{attachmentId}").build(MUNICIPALITY_ID, NAMESPACE, errandId, attachmentId))
			.exchange()
			.expectStatus().isNoContent()
			.expectHeader().contentType(ALL_VALUE);

		// Assert
		verify(attachmentServiceMock).deleteAttachment(errandId, attachmentId, MUNICIPALITY_ID, NAMESPACE);
	}

}
