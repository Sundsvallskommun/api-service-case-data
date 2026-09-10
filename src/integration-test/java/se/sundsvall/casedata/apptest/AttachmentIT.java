package se.sundsvall.casedata.apptest;

import static java.text.MessageFormat.format;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.IMAGE_PNG_VALUE;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;
import static se.sundsvall.casedata.apptest.util.TestConstants.JWT_HEADER_VALUE;
import static se.sundsvall.casedata.apptest.util.TestConstants.MUNICIPALITY_ID;
import static se.sundsvall.casedata.apptest.util.TestConstants.NAMESPACE;
import static se.sundsvall.casedata.apptest.util.TestConstants.REQUEST_FILE;
import static se.sundsvall.casedata.apptest.util.TestConstants.RESPONSE_FILE;
import static se.sundsvall.casedata.service.util.Constants.X_JWT_ASSERTION_HEADER_KEY;
import static se.sundsvall.dept44.support.Identifier.HEADER_NAME;

import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;
import se.sundsvall.casedata.Application;
import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;

@WireMockAppTestSuite(files = "classpath:/AttachmentIT", classes = Application.class)
@Sql({
	"/db/scripts/truncate.sql",
	"/db/scripts/attachmentIT-testdata.sql"
})
class AttachmentIT extends AbstractAppTest {

	private static final Long ERRAND_ID = 1L;
	private static final String ATTACHMENTS_PATH = "/{0}/{1}/errands/{2}/attachments";
	private static final String ATTACHMENT_BY_ID_PATH = "/{0}/{1}/errands/{2}/attachments/{3}";
	private static final String ADMIN_IDENTIFIER = "type=adAccount; user123";
	private static final String OTHER_NAMESPACE = "OTHER_NAMESPACE";
	private static final String OTHER_MUNICIPALITY_ID = "2262";

	@Test
	void test01_getAttachment() throws IOException {
		// Attachment 1 is stored as a binary blob (attachment.content); it should be streamed back verbatim.
		setupCall()
			.withHttpMethod(GET)
			.withServicePath(format(ATTACHMENT_BY_ID_PATH, MUNICIPALITY_ID, NAMESPACE, ERRAND_ID, "1"))
			.withExpectedResponseStatus(OK)
			.withExpectedResponseHeader(CONTENT_TYPE, List.of(IMAGE_PNG_VALUE))
			.withExpectedBinaryResponse("test_image.png")
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test02_getAttachmentNotFound() {
		setupCall()
			.withHttpMethod(GET)
			.withServicePath(format(ATTACHMENT_BY_ID_PATH, MUNICIPALITY_ID, NAMESPACE, ERRAND_ID, "1000"))
			.withExpectedResponseStatus(NOT_FOUND)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test05_deleteAttachmentOnErrand() {
		setupCall()
			.withHttpMethod(GET)
			.withServicePath(format(ATTACHMENT_BY_ID_PATH, MUNICIPALITY_ID, NAMESPACE, ERRAND_ID, "1"))
			.withExpectedResponseStatus(OK)
			.sendRequest();

		setupCall()
			.withHttpMethod(DELETE)
			.withServicePath(format(ATTACHMENT_BY_ID_PATH, MUNICIPALITY_ID, NAMESPACE, ERRAND_ID, "1"))
			.withHeader(X_JWT_ASSERTION_HEADER_KEY, JWT_HEADER_VALUE)
			.withHeader(HEADER_NAME, ADMIN_IDENTIFIER)
			.withExpectedResponseStatus(NO_CONTENT)
			.sendRequest();

		setupCall()
			.withHttpMethod(GET)
			.withServicePath(format(ATTACHMENT_BY_ID_PATH, MUNICIPALITY_ID, NAMESPACE, ERRAND_ID, "1"))
			.withExpectedResponseStatus(NOT_FOUND)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test06_deleteAttachmentOnErrandNotFound() {
		setupCall()
			.withHttpMethod(DELETE)
			.withServicePath(format(ATTACHMENT_BY_ID_PATH, MUNICIPALITY_ID, NAMESPACE, ERRAND_ID, "10"))
			.withExpectedResponseStatus(NOT_FOUND)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test07_createAttachment() throws IOException {
		// POST is multipart/form-data: JSON metadata part 'attachment' + binary part 'file'.
		final var location = setupCall()
			.withHttpMethod(POST)
			.withServicePath(format(ATTACHMENTS_PATH, MUNICIPALITY_ID, NAMESPACE, 4))
			.withContentType(MULTIPART_FORM_DATA)
			.withRequestFile("attachment", "attachment.json")
			.withRequestFile("file", "test_image.png")
			.withHeader(HEADER_NAME, ADMIN_IDENTIFIER)
			.withExpectedResponseStatus(CREATED)
			.withExpectedResponseBodyIsNull()
			.sendRequest()
			.getResponseHeaders().get(LOCATION).getFirst();

		// The stored content is streamed back binary-identical to the uploaded file.
		setupCall()
			.withHttpMethod(GET)
			.withServicePath(location)
			.withExpectedResponseStatus(OK)
			.withExpectedResponseHeader(CONTENT_TYPE, List.of(IMAGE_PNG_VALUE))
			.withExpectedBinaryResponse("test_image.png")
			.sendRequest();

		// The metadata exposes the application-computed SHA-256 hash and no longer exposes base64 content.
		setupCall()
			.withHttpMethod(GET)
			.withServicePath(format(ATTACHMENTS_PATH, MUNICIPALITY_ID, NAMESPACE, 4))
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test08_patchAttachmentNotFound() {
		setupCall()
			.withHttpMethod(PATCH)
			.withServicePath(format(ATTACHMENT_BY_ID_PATH, MUNICIPALITY_ID, NAMESPACE, ERRAND_ID, "1000"))
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(NOT_FOUND)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test09_getAttachmentsByErrandId() {
		setupCall()
			.withHttpMethod(GET)
			.withServicePath(format(ATTACHMENTS_PATH, MUNICIPALITY_ID, NAMESPACE, 2))
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test10_patchAttachment() {
		setupCall()
			.withHttpMethod(PATCH)
			.withServicePath(format(ATTACHMENT_BY_ID_PATH, MUNICIPALITY_ID, NAMESPACE, ERRAND_ID, "1"))
			.withHeader(HEADER_NAME, ADMIN_IDENTIFIER)
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(NO_CONTENT)
			.sendRequest();

		// Metadata changes are reflected while content/hash are untouched.
		setupCall()
			.withHttpMethod(GET)
			.withServicePath(format(ATTACHMENTS_PATH, MUNICIPALITY_ID, NAMESPACE, ERRAND_ID))
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	// The tenant-scoping tests below send a throwaway 'file' part (content.bin); the binary content is irrelevant to
	// them, the request is rejected on the errand lookup before the upload is ever read. Pure request validation
	// (empty/missing file part, unparsable metadata, invalid category) needs no database and lives in
	// AttachmentResourceTest instead.

	@Test
	void test12_createAttachmentOnMissingErrand() throws IOException {
		setupCall()
			.withHttpMethod(POST)
			.withServicePath(format(ATTACHMENTS_PATH, MUNICIPALITY_ID, NAMESPACE, 666))
			.withContentType(MULTIPART_FORM_DATA)
			.withRequestFile("attachment", "attachment.json")
			.withRequestFile("file", "content.bin")
			.withHeader(HEADER_NAME, ADMIN_IDENTIFIER)
			.withExpectedResponseStatus(NOT_FOUND)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test13_createAttachmentInOtherNamespace() throws IOException {
		// Errand 1 exists, but not in this namespace - an upload must not leak across the tenant boundary.
		setupCall()
			.withHttpMethod(POST)
			.withServicePath(format(ATTACHMENTS_PATH, MUNICIPALITY_ID, OTHER_NAMESPACE, ERRAND_ID))
			.withContentType(MULTIPART_FORM_DATA)
			.withRequestFile("attachment", "attachment.json")
			.withRequestFile("file", "content.bin")
			.withHeader(HEADER_NAME, ADMIN_IDENTIFIER)
			.withExpectedResponseStatus(NOT_FOUND)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test14_createAttachmentInOtherMunicipality() throws IOException {
		// Same as above for the municipality dimension of the tenant key.
		setupCall()
			.withHttpMethod(POST)
			.withServicePath(format(ATTACHMENTS_PATH, OTHER_MUNICIPALITY_ID, NAMESPACE, ERRAND_ID))
			.withContentType(MULTIPART_FORM_DATA)
			.withRequestFile("attachment", "attachment.json")
			.withRequestFile("file", "content.bin")
			.withHeader(HEADER_NAME, ADMIN_IDENTIFIER)
			.withExpectedResponseStatus(NOT_FOUND)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test15_patchAttachmentBelongingToOtherErrand() {
		// Attachment 2 exists, but on errand 2. Addressing it through errand 1 must not find it.
		setupCall()
			.withHttpMethod(PATCH)
			.withServicePath(format(ATTACHMENT_BY_ID_PATH, MUNICIPALITY_ID, NAMESPACE, ERRAND_ID, "2"))
			.withHeader(HEADER_NAME, ADMIN_IDENTIFIER)
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(NOT_FOUND)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test16_patchAttachmentIgnoresReadOnlyFields() {
		// The request deliberately carries id, errandId, municipalityId, namespace, version and hash. None of them are
		// patchable: the content hash in particular must keep describing the stored bytes.
		setupCall()
			.withHttpMethod(PATCH)
			.withServicePath(format(ATTACHMENT_BY_ID_PATH, MUNICIPALITY_ID, NAMESPACE, ERRAND_ID, "1"))
			.withHeader(HEADER_NAME, ADMIN_IDENTIFIER)
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(NO_CONTENT)
			.sendRequest();

		setupCall()
			.withHttpMethod(GET)
			.withServicePath(format(ATTACHMENTS_PATH, MUNICIPALITY_ID, NAMESPACE, ERRAND_ID))
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test17_patchAttachmentMergesExtraParameters() {
		// extraParameters are merged into the existing map, not replaced - two consecutive patches must leave both
		// keys in place.
		setupCall()
			.withHttpMethod(PATCH)
			.withServicePath(format(ATTACHMENT_BY_ID_PATH, MUNICIPALITY_ID, NAMESPACE, ERRAND_ID, "1"))
			.withHeader(HEADER_NAME, ADMIN_IDENTIFIER)
			.withRequest("request-first.json")
			.withExpectedResponseStatus(NO_CONTENT)
			.sendRequest();

		setupCall()
			.withHttpMethod(PATCH)
			.withServicePath(format(ATTACHMENT_BY_ID_PATH, MUNICIPALITY_ID, NAMESPACE, ERRAND_ID, "1"))
			.withHeader(HEADER_NAME, ADMIN_IDENTIFIER)
			.withRequest("request-second.json")
			.withExpectedResponseStatus(NO_CONTENT)
			.sendRequest();

		setupCall()
			.withHttpMethod(GET)
			.withServicePath(format(ATTACHMENTS_PATH, MUNICIPALITY_ID, NAMESPACE, ERRAND_ID))
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}
}
