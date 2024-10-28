package se.sundsvall.casedata.apptest;

import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.web.util.UriComponentsBuilder.fromPath;
import static se.sundsvall.casedata.TestUtil.MUNICIPALITY_ID;
import static se.sundsvall.casedata.TestUtil.NAMESPACE;
import static se.sundsvall.casedata.apptest.util.TestConstants.REQUEST_FILE;
import static se.sundsvall.casedata.apptest.util.TestConstants.RESPONSE_FILE;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;

import se.sundsvall.casedata.Application;
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
	private static final String NOTIFICATION_PATH = "/{municipalityId}/{namespace}/notifications/{notificationId}";

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
				.build(Map.of("municipalityId", MUNICIPALITY_ID, "namespace", NAMESPACE, "notificationId", NOTIFICATION_ID)))
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test03_createNotification() {
		final var location = setupCall()
			.withServicePath(builder -> fromPath(NOTIFICATIONS_PATH)
				.build(Map.of("municipalityId", MUNICIPALITY_ID, "namespace", NAMESPACE)))
			.withHttpMethod(POST)
			.withHeader("sentbyuser", "creator123")
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
				.build(Map.of("municipalityId", MUNICIPALITY_ID, "namespace", NAMESPACE, "notificationId", NOTIFICATION_ID)))
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test05_deleteNotification() {

		setupCall()
			.withServicePath(builder -> fromPath(NOTIFICATION_PATH)
				.build(Map.of("municipalityId", MUNICIPALITY_ID, "namespace", NAMESPACE, "notificationId", NOTIFICATION_ID)))
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.sendRequestAndVerifyResponse();

		setupCall()
			.withServicePath(builder -> fromPath(NOTIFICATION_PATH)
				.build(Map.of("municipalityId", MUNICIPALITY_ID, "namespace", NAMESPACE, "notificationId", NOTIFICATION_ID)))
			.withHttpMethod(DELETE)
			.withExpectedResponseStatus(NO_CONTENT)
			.sendRequestAndVerifyResponse();

		setupCall()
			.withServicePath(builder -> fromPath(NOTIFICATION_PATH)
				.build(Map.of("municipalityId", MUNICIPALITY_ID, "namespace", NAMESPACE, "notificationId", NOTIFICATION_ID)))
			.withHttpMethod(GET)
			.withExpectedResponseStatus(NOT_FOUND)
			.sendRequestAndVerifyResponse();
	}
}
