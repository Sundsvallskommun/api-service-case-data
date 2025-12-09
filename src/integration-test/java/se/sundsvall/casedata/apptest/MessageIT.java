package se.sundsvall.casedata.apptest;

import static net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.IMAGE_PNG_VALUE;
import static org.springframework.web.util.UriComponentsBuilder.fromPath;
import static se.sundsvall.casedata.apptest.util.TestConstants.MUNICIPALITY_ID;
import static se.sundsvall.casedata.apptest.util.TestConstants.NAMESPACE;
import static se.sundsvall.casedata.apptest.util.TestConstants.REQUEST_FILE;
import static se.sundsvall.casedata.apptest.util.TestConstants.RESPONSE_FILE;
import static se.sundsvall.dept44.support.Identifier.HEADER_NAME;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import se.sundsvall.casedata.Application;
import se.sundsvall.casedata.integration.db.MessageRepository;
import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;

@WireMockAppTestSuite(
	files = "classpath:/MessageIT/",
	classes = Application.class)
@Sql({
	"/db/scripts/truncate.sql",
	"/db/scripts/messageIT-testdata.sql"
})
class MessageIT extends AbstractAppTest {

	private static final String BASE_PATH = "/{municipalityId}/{namespace}/errands/{errandId}";
	private static final String MESSAGE_ID = "a8883fb9-60b4-4f38-9f48-642070ff49ee";
	private static final String MESSAGE_ATTACHMENT_ID = "05b29c30-4512-46c0-9d82-d0f11cb04bae";
	private static final Long ERRAND_ID = 1L;
	private static final String NOTIFICATIONS_RESPONSE_FILE = "notificationResponse.json";

	@Autowired
	private MessageRepository messageRepository;

	@Test
	void test01_getMessages() {
		setupCall()
			.withHttpMethod(GET)
			.withServicePath(builder -> fromPath(BASE_PATH + "/messages")
				.build(Map.of("municipalityId", MUNICIPALITY_ID, "namespace", NAMESPACE, "errandId", ERRAND_ID)))
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test02_getMessage() {
		setupCall()
			.withHttpMethod(GET)
			.withServicePath(builder -> fromPath(BASE_PATH + "/messages/{messageId}")
				.build(Map.of("municipalityId", MUNICIPALITY_ID, "namespace", NAMESPACE, "errandId", ERRAND_ID, "messageId", MESSAGE_ID)))
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test03_createMessage() {
		final var location = setupCall()
			.withServicePath(builder -> fromPath(BASE_PATH + "/messages")
				.build(Map.of("municipalityId", MUNICIPALITY_ID, "namespace", NAMESPACE, "errandId", ERRAND_ID)))
			.withHttpMethod(POST)
			.withHeader(HEADER_NAME, "type=adAccount; tes01adm")
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(CREATED)
			.withExpectedResponseBodyIsNull()
			.sendRequest()
			.getResponseHeaders().get(LOCATION).getFirst();

		setupCall()
			.withHttpMethod(GET)
			.withServicePath(location)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequest();

		setupCall()
			.withServicePath(builder -> fromPath(BASE_PATH + "/notifications")
				.build(Map.of("municipalityId", MUNICIPALITY_ID, "namespace", NAMESPACE, "errandId", ERRAND_ID)))
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(NOTIFICATIONS_RESPONSE_FILE)
			.withJsonAssertOptions(List.of(IGNORING_ARRAY_ORDER))
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test04_updateViewedStatus() {
		assertThat(messageRepository.findById(MESSAGE_ID).orElseThrow().isViewed()).isTrue();

		setupCall()
			.withServicePath(builder -> fromPath(BASE_PATH + "/messages/{messageId}/viewed/{isViewed}")
				.build(Map.of("municipalityId", MUNICIPALITY_ID, "namespace", NAMESPACE, "errandId", 1, "messageId", MESSAGE_ID, "isViewed", false)))
			.withHttpMethod(PUT)
			.withExpectedResponseStatus(NO_CONTENT)
			.withExpectedResponseBodyIsNull()
			.sendRequestAndVerifyResponse();

		assertThat(messageRepository.findById(MESSAGE_ID).orElseThrow().isViewed()).isFalse();
	}

	@Test
	void test05_getMessageAttachment() throws Exception {
		setupCall()
			.withHttpMethod(GET)
			.withServicePath(builder -> fromPath(BASE_PATH + "/messages/{messageId}/attachments/{attachmentId}")
				.build(Map.of("municipalityId", MUNICIPALITY_ID, "namespace", NAMESPACE, "errandId", ERRAND_ID, "messageId", MESSAGE_ID, "attachmentId", MESSAGE_ATTACHMENT_ID)))
			.withExpectedResponseStatus(OK)
			.withExpectedResponseHeader(CONTENT_TYPE, List.of(IMAGE_PNG_VALUE))
			.withExpectedBinaryResponse("test_image.png")
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test06_getMessageAttachmentNotFound() {
		setupCall()
			.withHttpMethod(GET)
			.withServicePath(builder -> fromPath(BASE_PATH + "/messages/{messageId}/attachments/{attachmentId}")
				.build(Map.of("municipalityId", MUNICIPALITY_ID, "namespace", NAMESPACE, "errandId", ERRAND_ID, "messageId", MESSAGE_ID, "attachmentId", "nonexistingid"))) // Non existing attachment id
			.withExpectedResponseStatus(NOT_FOUND)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test07_getExternalMessages() {
		setupCall()
			.withHttpMethod(GET)
			.withServicePath(builder -> fromPath(BASE_PATH + "/messages/external")
				.build(Map.of("municipalityId", MUNICIPALITY_ID, "namespace", NAMESPACE, "errandId", 1)))
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test08_createMessageForErrandWithReporterStakeholder() {
		final var location = setupCall()
			.withServicePath(builder -> fromPath(BASE_PATH + "/messages")
				.build(Map.of("municipalityId", MUNICIPALITY_ID, "namespace", NAMESPACE, "errandId", 3)))
			.withHttpMethod(POST)
			.withHeader(HEADER_NAME, "type=adAccount; tes01adm")
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(CREATED)
			.withExpectedResponseBodyIsNull()
			.sendRequest()
			.getResponseHeaders().get(LOCATION).getFirst();

		setupCall()
			.withHttpMethod(GET)
			.withServicePath(location)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequest();

		setupCall()
			.withServicePath(builder -> fromPath(BASE_PATH + "/notifications")
				.build(Map.of("municipalityId", MUNICIPALITY_ID, "namespace", NAMESPACE, "errandId", 3)))
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(NOTIFICATIONS_RESPONSE_FILE)
			.withJsonAssertOptions(List.of(IGNORING_ARRAY_ORDER))
			.sendRequestAndVerifyResponse();
	}
}
