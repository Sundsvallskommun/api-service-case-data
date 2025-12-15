package se.sundsvall.casedata.apptest;

import static java.text.MessageFormat.format;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_EXTRA_ARRAY_ITEMS;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;
import static org.springframework.util.MimeTypeUtils.IMAGE_JPEG_VALUE;
import static se.sundsvall.casedata.apptest.util.TestConstants.JWT_HEADER_VALUE;
import static se.sundsvall.casedata.apptest.util.TestConstants.MUNICIPALITY_ID;
import static se.sundsvall.casedata.apptest.util.TestConstants.NAMESPACE;
import static se.sundsvall.casedata.apptest.util.TestConstants.REQUEST_FILE;
import static se.sundsvall.casedata.apptest.util.TestConstants.RESPONSE_FILE;
import static se.sundsvall.casedata.service.util.Constants.X_JWT_ASSERTION_HEADER_KEY;
import static se.sundsvall.dept44.support.Identifier.HEADER_NAME;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;

import se.sundsvall.casedata.Application;
import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;

@WireMockAppTestSuite(files = "classpath:/ConversationIT", classes = Application.class)
@Sql({
	"/db/scripts/truncate.sql",
	"/db/scripts/conversationIT-testdata.sql"
})
class ConversationIT extends AbstractAppTest {

	private static final String CONVERSATION_ID = "896a44d8-724b-11ed-a840-0242ac110002";
	private static final Long ERRAND_ID = 1L;
	private static final String PATH = "/{0}/{1}/errands/{2}/communication/conversations";
	private static final String MESSAGE_ID = "d82bd8ac-1507-4d9a-958d-369261eecc15";
	private static final String ATTACHMENT_ID = "a1a1b2c3-d4e5-f6a7-b8c9-d0e1f2a3b4c5";

	@Test
	void test01_getConversation() {
		setupCall()
			.withHttpMethod(GET)
			.withServicePath(format(PATH + "/{3}", MUNICIPALITY_ID, NAMESPACE, ERRAND_ID, CONVERSATION_ID))
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test02_getConversations() {
		setupCall()
			.withHttpMethod(GET)
			.withServicePath(format(PATH, MUNICIPALITY_ID, NAMESPACE, ERRAND_ID))
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test03_updateConversation() {
		setupCall()
			.withHttpMethod(PATCH)
			.withServicePath(format(PATH + "/{3}", MUNICIPALITY_ID, NAMESPACE, ERRAND_ID, CONVERSATION_ID))
			.withRequest(REQUEST_FILE)
			.withHeader(X_JWT_ASSERTION_HEADER_KEY, JWT_HEADER_VALUE)
			.withHeader(HEADER_NAME, "type=adAccount; someUser123")
			.withExpectedResponseStatus(OK)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test04_createConversation() {
		final var location = setupCall()
			.withHttpMethod(POST)
			.withServicePath(format(PATH, MUNICIPALITY_ID, NAMESPACE, ERRAND_ID))
			.withRequest(REQUEST_FILE)
			.withHeader(X_JWT_ASSERTION_HEADER_KEY, JWT_HEADER_VALUE)
			.withHeader(HEADER_NAME, "type=adAccount; someUser123")
			.withExpectedResponseStatus(CREATED)
			.withExpectedResponseHeader("Location", List.of(format(PATH + "/[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}", MUNICIPALITY_ID, NAMESPACE, ERRAND_ID)))
			.sendRequest()
			.getResponseHeaders().get(LOCATION).getFirst();

		setupCall()
			.withServicePath(location)
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test05_createMessage() throws FileNotFoundException {
		setupCall()
			.withHttpMethod(POST)
			.withServicePath(format(PATH + "/{3}/messages", MUNICIPALITY_ID, NAMESPACE, ERRAND_ID, CONVERSATION_ID))
			.withContentType(MULTIPART_FORM_DATA)
			.withRequestFile("message", REQUEST_FILE)
			.withHeader(X_JWT_ASSERTION_HEADER_KEY, JWT_HEADER_VALUE)
			.withHeader(HEADER_NAME, "type=adAccount; someUser123")
			.withExpectedResponseStatus(NO_CONTENT)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test06_getMessages() {
		setupCall()
			.withHttpMethod(GET)
			.withServicePath(format(PATH + "/{3}/messages", MUNICIPALITY_ID, NAMESPACE, ERRAND_ID, CONVERSATION_ID))
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test07_getAttachment() throws IOException {
		setupCall()
			.withServicePath(format(PATH + "/{3}/messages/{4}/attachments/{5}", MUNICIPALITY_ID, NAMESPACE, ERRAND_ID, CONVERSATION_ID, MESSAGE_ID, ATTACHMENT_ID))
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponseHeader(CONTENT_TYPE, List.of(IMAGE_JPEG_VALUE))
			.withExpectedBinaryResponse("Test_image.jpg")
			.sendRequestAndVerifyResponse();
	}

	/**
	 * Test to verify email is sent and notifications are created when creating an message with type internal for an errand
	 * where stakeholder with reporter role is present, i.e. test of scenario when administrator creates an internal message
	 * in a case created by a reporter (for example in paratransit))
	 */
	@Test
	void test08_createMessageWithTypeInternalToReporter() throws FileNotFoundException {
		final var errandId = 3;
		final var internalConversationId = "896a44d8-724b-11ed-a840-0242ac110004";
		final var notificationsResponseFile = "notificationResponse.json";

		setupCall()
			.withHttpMethod(POST)
			.withServicePath(format(PATH + "/{3}/messages", MUNICIPALITY_ID, NAMESPACE, errandId, internalConversationId))
			.withContentType(MULTIPART_FORM_DATA)
			.withRequestFile("message", REQUEST_FILE)
			.withHeader(X_JWT_ASSERTION_HEADER_KEY, JWT_HEADER_VALUE)
			.withHeader(HEADER_NAME, "type=adAccount; adm01adm")
			.withExpectedResponseStatus(NO_CONTENT)
			.sendRequest();

		setupCall()
			.withServicePath(format("/{0}/{1}/errands/{2}/notifications", MUNICIPALITY_ID, NAMESPACE, errandId))
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(notificationsResponseFile)
			.withJsonAssertOptions(List.of(IGNORING_ARRAY_ORDER, IGNORING_EXTRA_ARRAY_ITEMS))
			.sendRequestAndVerifyResponse();
	}

	/**
	 * Test to verify email is NOT sent but notifications are created when creating an message with type internal for an
	 * errand where stakeholder with reporter role is present, i.e. test of scenario when reporter creates an internal
	 * message to the administrator of the errand in a case created by the reporter (for example in paratransit)
	 */
	@Test
	void test09_createMessageWithTypeInternalToAdministrator() throws FileNotFoundException {
		final var errandId = 3;
		final var internalConversationId = "896a44d8-724b-11ed-a840-0242ac110004";
		final var notificationsResponseFile = "notificationResponse.json";

		setupCall()
			.withHttpMethod(POST)
			.withServicePath(format(PATH + "/{3}/messages", MUNICIPALITY_ID, NAMESPACE, errandId, internalConversationId))
			.withContentType(MULTIPART_FORM_DATA)
			.withRequestFile("message", REQUEST_FILE)
			.withHeader(X_JWT_ASSERTION_HEADER_KEY, JWT_HEADER_VALUE)
			.withHeader(HEADER_NAME, "type=adAccount; tes02rep")
			.withExpectedResponseStatus(NO_CONTENT)
			.sendRequest();

		setupCall()
			.withServicePath(format("/{0}/{1}/errands/{2}/notifications", MUNICIPALITY_ID, NAMESPACE, errandId))
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(notificationsResponseFile)
			.withJsonAssertOptions(List.of(IGNORING_ARRAY_ORDER, IGNORING_EXTRA_ARRAY_ITEMS))
			.sendRequestAndVerifyResponse();
	}
}
