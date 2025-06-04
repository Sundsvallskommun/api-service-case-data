package se.sundsvall.casedata.apptest;

import static java.text.MessageFormat.format;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;
import static se.sundsvall.casedata.apptest.util.TestConstants.AD_USER_HEADER_KEY;
import static se.sundsvall.casedata.apptest.util.TestConstants.JWT_HEADER_VALUE;
import static se.sundsvall.casedata.apptest.util.TestConstants.MUNICIPALITY_ID;
import static se.sundsvall.casedata.apptest.util.TestConstants.NAMESPACE;
import static se.sundsvall.casedata.apptest.util.TestConstants.REQUEST_FILE;
import static se.sundsvall.casedata.apptest.util.TestConstants.RESPONSE_FILE;
import static se.sundsvall.casedata.service.util.Constants.X_JWT_ASSERTION_HEADER_KEY;

import java.io.FileNotFoundException;
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
			.withHeader(AD_USER_HEADER_KEY, "someUser123")
			.withExpectedResponseStatus(OK)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test04_createConversation() {
		setupCall()
			.withHttpMethod(POST)
			.withServicePath(format(PATH, MUNICIPALITY_ID, NAMESPACE, ERRAND_ID))
			.withRequest(REQUEST_FILE)
			.withHeader(X_JWT_ASSERTION_HEADER_KEY, JWT_HEADER_VALUE)
			.withHeader(AD_USER_HEADER_KEY, "someUser123")
			.withExpectedResponseStatus(CREATED)
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
			.withHeader(AD_USER_HEADER_KEY, "someUser123")
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

}
