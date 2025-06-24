package se.sundsvall.casedata.apptest;

import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.web.util.UriComponentsBuilder.fromPath;
import static se.sundsvall.casedata.TestUtil.MUNICIPALITY_ID;
import static se.sundsvall.casedata.TestUtil.NAMESPACE;
import static se.sundsvall.casedata.apptest.util.TestConstants.REQUEST_FILE;
import static se.sundsvall.casedata.apptest.util.TestConstants.RESPONSE_FILE;
import static se.sundsvall.dept44.support.Identifier.HEADER_NAME;
import static se.sundsvall.dept44.support.Identifier.Type.AD_ACCOUNT;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;
import se.sundsvall.casedata.Application;
import se.sundsvall.dept44.support.Identifier;
import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;

@WireMockAppTestSuite(
	files = "classpath:/NotificationIT/",
	classes = Application.class)
@Sql({
	"/db/scripts/truncate.sql",
	"/db/scripts/notificationIT-testdata.sql"
})
class NotificationIT extends AbstractAppTest {

	private static final String NOTIFICATION_ID = "25d818b7-763e-4b77-9fce-1c7dfc42deb2";
	private static final String NOTIFICATIONS_PATH = "/{municipalityId}/{namespace}/notifications";
	private static final String ERRAND_NOTIFICATIONS_PATH = "/{municipalityId}/{namespace}/errands/{errandId}/notifications";
	private static final String NOTIFICATION_PATH = "/{municipalityId}/{namespace}/errands/{errandId}/notifications/{notificationId}";

	@Test
	void test01_getNotificationsByOwnerId() {
		setupCall()
			.withServicePath(builder -> fromPath(NOTIFICATIONS_PATH)
				.queryParam("ownerId", "testUser2")
				.build(Map.of("municipalityId", MUNICIPALITY_ID, "namespace", NAMESPACE)))
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test02_getNotificationById() {
		setupCall()
			.withServicePath(builder -> fromPath(NOTIFICATION_PATH)
				.build(Map.of("municipalityId", MUNICIPALITY_ID, "namespace", NAMESPACE, "notificationId", NOTIFICATION_ID, "errandId", 1)))
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test03_createNotification() {
		final var location = setupCall()
			.withServicePath(builder -> fromPath(ERRAND_NOTIFICATIONS_PATH)
				.build(Map.of("municipalityId", MUNICIPALITY_ID, "namespace", NAMESPACE, "errandId", 1)))
			.withHttpMethod(POST)
			.withHeader(HEADER_NAME, Identifier.create().withType(AD_ACCOUNT).withValue("creator123").toHeaderValue())
			.withExpectedResponseStatus(CREATED)
			.withRequest(REQUEST_FILE)
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
	void test04_updateNotification() {
		setupCall()
			.withServicePath(builder -> fromPath(NOTIFICATIONS_PATH)
				.build(Map.of("municipalityId", MUNICIPALITY_ID, "namespace", NAMESPACE)))
			.withHttpMethod(PATCH)
			.withHeader("sentbyuser", "modifier123")
			.withExpectedResponseStatus(NO_CONTENT)
			.withRequest(REQUEST_FILE)
			.sendRequest();

		setupCall()
			.withServicePath(builder -> fromPath(NOTIFICATION_PATH)
				.build(Map.of("municipalityId", MUNICIPALITY_ID, "namespace", NAMESPACE, "notificationId", NOTIFICATION_ID, "errandId", 1)))
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test05_deleteNotification() {

		setupCall()
			.withServicePath(builder -> fromPath(NOTIFICATION_PATH)
				.build(Map.of("municipalityId", MUNICIPALITY_ID, "namespace", NAMESPACE, "notificationId", NOTIFICATION_ID, "errandId", 1)))
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.sendRequestAndVerifyResponse();

		setupCall()
			.withServicePath(builder -> fromPath(NOTIFICATION_PATH)
				.build(Map.of("municipalityId", MUNICIPALITY_ID, "namespace", NAMESPACE, "notificationId", NOTIFICATION_ID, "errandId", 1)))
			.withHttpMethod(DELETE)
			.withExpectedResponseStatus(NO_CONTENT)
			.sendRequestAndVerifyResponse();

		setupCall()
			.withServicePath(builder -> fromPath(NOTIFICATION_PATH)
				.build(Map.of("municipalityId", MUNICIPALITY_ID, "namespace", NAMESPACE, "notificationId", NOTIFICATION_ID, "errandId", 1)))
			.withHttpMethod(GET)
			.withExpectedResponseStatus(NOT_FOUND)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test06_getNotificationsByErrandId() {
		setupCall()
			.withServicePath(builder -> fromPath(ERRAND_NOTIFICATIONS_PATH)
				.build(Map.of("municipalityId", MUNICIPALITY_ID, "namespace", NAMESPACE, "errandId", 1)))
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test07_globalAcknowledgeNotifications() {

		setupCall()
			.withServicePath(builder -> fromPath(ERRAND_NOTIFICATIONS_PATH + "/global-acknowledged")
				.build(Map.of("municipalityId", MUNICIPALITY_ID, "namespace", NAMESPACE, "errandId", 1)))
			.withHttpMethod(PUT)
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(NO_CONTENT)
			.withExpectedResponseBodyIsNull()
			.sendRequest();

		setupCall()
			.withServicePath(builder -> fromPath(ERRAND_NOTIFICATIONS_PATH)
				.build(Map.of("municipalityId", MUNICIPALITY_ID, "namespace", NAMESPACE, "errandId", 1)))
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}
}
