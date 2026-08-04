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
import static se.sundsvall.casedata.apptest.util.TestConstants.MUNICIPALITY_ID;
import static se.sundsvall.casedata.apptest.util.TestConstants.NAMESPACE;
import static se.sundsvall.casedata.apptest.util.TestConstants.REQUEST_FILE;
import static se.sundsvall.casedata.apptest.util.TestConstants.RESPONSE_FILE;
import static se.sundsvall.dept44.support.Identifier.HEADER_NAME;

import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;
import se.sundsvall.casedata.Application;
import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;

/**
 * Attachments owned by a decision. Decision 1 belongs to errand 1 and owns attachments 1 (blob) and 2 (legacy base64);
 * attachment 3 belongs directly to errand 1. Decision 2 belongs to errand 2.
 */
@WireMockAppTestSuite(files = "classpath:/DecisionAttachmentIT", classes = Application.class)
@Sql({
	"/db/scripts/truncate.sql",
	"/db/scripts/decisionAttachmentIT-testdata.sql"
})
class DecisionAttachmentIT extends AbstractAppTest {

	private static final String ERRAND_ID = "1";
	private static final String DECISION_ID = "1";
	private static final String ATTACHMENTS_PATH = "/{0}/{1}/errands/{2}/decisions/{3}/attachments";
	private static final String ATTACHMENT_BY_ID_PATH = "/{0}/{1}/errands/{2}/decisions/{3}/attachments/{4}";
	private static final String ERRAND_ATTACHMENTS_PATH = "/{0}/{1}/errands/{2}/attachments";
	private static final String DECISION_PATH = "/{0}/{1}/errands/{2}/decisions/{3}";
	private static final String ADMIN_IDENTIFIER = "type=adAccount; user123";
	private static final String OTHER_NAMESPACE = "OTHER_NAMESPACE";
	private static final String OTHER_MUNICIPALITY_ID = "2262";

	@Test
	void test01_getAttachment() throws IOException {
		// Attachment 1 is stored as a binary blob (attachment.content); it should be streamed back verbatim.
		setupCall()
			.withHttpMethod(GET)
			.withServicePath(format(ATTACHMENT_BY_ID_PATH, MUNICIPALITY_ID, NAMESPACE, ERRAND_ID, DECISION_ID, "1"))
			.withExpectedResponseStatus(OK)
			.withExpectedResponseHeader(CONTENT_TYPE, List.of(IMAGE_PNG_VALUE))
			.withExpectedBinaryResponse("test_image.png")
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test02_getAttachmentWithLegacyBase64Content() throws IOException {
		// Attachment 2 is an un-migrated row: the content lives base64-encoded in the legacy 'file' column and must be
		// decoded on the fly, yielding the same bytes as a blob-stored attachment.
		setupCall()
			.withHttpMethod(GET)
			.withServicePath(format(ATTACHMENT_BY_ID_PATH, MUNICIPALITY_ID, NAMESPACE, ERRAND_ID, DECISION_ID, "2"))
			.withExpectedResponseStatus(OK)
			.withExpectedResponseHeader(CONTENT_TYPE, List.of(IMAGE_PNG_VALUE))
			.withExpectedBinaryResponse("test_image.png")
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test03_getAttachmentBelongingToErrand() {
		// Attachment 3 belongs to the errand itself, so it must not be reachable through the decision.
		setupCall()
			.withHttpMethod(GET)
			.withServicePath(format(ATTACHMENT_BY_ID_PATH, MUNICIPALITY_ID, NAMESPACE, ERRAND_ID, DECISION_ID, "3"))
			.withExpectedResponseStatus(NOT_FOUND)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test04_getAttachmentsByDecisionId() {
		// Only the decision's own attachments, each carrying decisionId and no errandId.
		setupCall()
			.withHttpMethod(GET)
			.withServicePath(format(ATTACHMENTS_PATH, MUNICIPALITY_ID, NAMESPACE, ERRAND_ID, DECISION_ID))
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test05_getErrandAttachmentsExcludesDecisionAttachments() {
		// The counterpart of the test above: the errand attachment endpoint only returns attachment 3.
		setupCall()
			.withHttpMethod(GET)
			.withServicePath(format(ERRAND_ATTACHMENTS_PATH, MUNICIPALITY_ID, NAMESPACE, ERRAND_ID))
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test06_createAttachment() throws IOException {
		// POST is multipart/form-data: JSON metadata part 'attachment' + binary part 'file'.
		final var location = setupCall()
			.withHttpMethod(POST)
			.withServicePath(format(ATTACHMENTS_PATH, MUNICIPALITY_ID, NAMESPACE, ERRAND_ID, DECISION_ID))
			.withContentType(MULTIPART_FORM_DATA)
			.withRequestFile("attachment", "attachment.json")
			.withRequestFile("file", "test_image.png")
			.withHeader(HEADER_NAME, ADMIN_IDENTIFIER)
			.withExpectedResponseStatus(CREATED)
			.withExpectedResponseBodyIsNull()
			.sendRequestAndVerifyResponse()
			.getResponseHeaders().get(LOCATION).getFirst();

		// The stored content is streamed back binary-identical to the uploaded file.
		setupCall()
			.withHttpMethod(GET)
			.withServicePath(location)
			.withExpectedResponseStatus(OK)
			.withExpectedResponseHeader(CONTENT_TYPE, List.of(IMAGE_PNG_VALUE))
			.withExpectedBinaryResponse("test_image.png")
			.sendRequest();

		// The new attachment belongs to the decision and is not part of the errand's own attachments.
		setupCall()
			.withHttpMethod(GET)
			.withServicePath(format(ATTACHMENTS_PATH, MUNICIPALITY_ID, NAMESPACE, ERRAND_ID, DECISION_ID))
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequest();

		setupCall()
			.withHttpMethod(GET)
			.withServicePath(format(ERRAND_ATTACHMENTS_PATH, MUNICIPALITY_ID, NAMESPACE, ERRAND_ID))
			.withExpectedResponseStatus(OK)
			.withExpectedResponse("errand-attachments.json")
			.sendRequest();
	}

	@Test
	void test07_patchAttachment() {
		setupCall()
			.withHttpMethod(PATCH)
			.withServicePath(format(ATTACHMENT_BY_ID_PATH, MUNICIPALITY_ID, NAMESPACE, ERRAND_ID, DECISION_ID, "1"))
			.withHeader(HEADER_NAME, ADMIN_IDENTIFIER)
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(NO_CONTENT)
			.sendRequestAndVerifyResponse();

		// Metadata changes are reflected while content/hash are untouched.
		setupCall()
			.withHttpMethod(GET)
			.withServicePath(format(ATTACHMENTS_PATH, MUNICIPALITY_ID, NAMESPACE, ERRAND_ID, DECISION_ID))
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequest();
	}

	@Test
	void test08_deleteAttachment() {
		setupCall()
			.withHttpMethod(DELETE)
			.withServicePath(format(ATTACHMENT_BY_ID_PATH, MUNICIPALITY_ID, NAMESPACE, ERRAND_ID, DECISION_ID, "1"))
			.withHeader(HEADER_NAME, ADMIN_IDENTIFIER)
			.withExpectedResponseStatus(NO_CONTENT)
			.sendRequestAndVerifyResponse();

		setupCall()
			.withHttpMethod(GET)
			.withServicePath(format(ATTACHMENT_BY_ID_PATH, MUNICIPALITY_ID, NAMESPACE, ERRAND_ID, DECISION_ID, "1"))
			.withExpectedResponseStatus(NOT_FOUND)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequest();
	}

	@Test
	void test09_getAttachmentsOnDecisionBelongingToOtherErrand() {
		// Decision 2 exists, but on errand 2. Addressing it through errand 1 must not find it.
		setupCall()
			.withHttpMethod(GET)
			.withServicePath(format(ATTACHMENTS_PATH, MUNICIPALITY_ID, NAMESPACE, ERRAND_ID, "2"))
			.withExpectedResponseStatus(NOT_FOUND)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test10_createAttachmentOnMissingDecision() throws IOException {
		setupCall()
			.withHttpMethod(POST)
			.withServicePath(format(ATTACHMENTS_PATH, MUNICIPALITY_ID, NAMESPACE, ERRAND_ID, "666"))
			.withContentType(MULTIPART_FORM_DATA)
			.withRequestFile("attachment", "attachment.json")
			.withRequestFile("file", "content.bin")
			.withHeader(HEADER_NAME, ADMIN_IDENTIFIER)
			.withExpectedResponseStatus(NOT_FOUND)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test11_createAttachmentInOtherNamespace() throws IOException {
		// The decision exists, but not in this namespace - an upload must not leak across the tenant boundary.
		setupCall()
			.withHttpMethod(POST)
			.withServicePath(format(ATTACHMENTS_PATH, MUNICIPALITY_ID, OTHER_NAMESPACE, ERRAND_ID, DECISION_ID))
			.withContentType(MULTIPART_FORM_DATA)
			.withRequestFile("attachment", "attachment.json")
			.withRequestFile("file", "content.bin")
			.withHeader(HEADER_NAME, ADMIN_IDENTIFIER)
			.withExpectedResponseStatus(NOT_FOUND)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test12_createAttachmentInOtherMunicipality() throws IOException {
		// Same as above for the municipality dimension of the tenant key.
		setupCall()
			.withHttpMethod(POST)
			.withServicePath(format(ATTACHMENTS_PATH, OTHER_MUNICIPALITY_ID, NAMESPACE, ERRAND_ID, DECISION_ID))
			.withContentType(MULTIPART_FORM_DATA)
			.withRequestFile("attachment", "attachment.json")
			.withRequestFile("file", "content.bin")
			.withHeader(HEADER_NAME, ADMIN_IDENTIFIER)
			.withExpectedResponseStatus(NOT_FOUND)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test13_patchAttachmentBelongingToErrand() {
		// Attachment 3 is owned by the errand, so it cannot be patched through the decision.
		setupCall()
			.withHttpMethod(PATCH)
			.withServicePath(format(ATTACHMENT_BY_ID_PATH, MUNICIPALITY_ID, NAMESPACE, ERRAND_ID, DECISION_ID, "3"))
			.withHeader(HEADER_NAME, ADMIN_IDENTIFIER)
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(NOT_FOUND)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test14_getAttachmentNotFound() {
		setupCall()
			.withHttpMethod(GET)
			.withServicePath(format(ATTACHMENT_BY_ID_PATH, MUNICIPALITY_ID, NAMESPACE, ERRAND_ID, DECISION_ID, "1000"))
			.withExpectedResponseStatus(NOT_FOUND)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test15_patchAttachmentNotFound() {
		setupCall()
			.withHttpMethod(PATCH)
			.withServicePath(format(ATTACHMENT_BY_ID_PATH, MUNICIPALITY_ID, NAMESPACE, ERRAND_ID, DECISION_ID, "1000"))
			.withHeader(HEADER_NAME, ADMIN_IDENTIFIER)
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(NOT_FOUND)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test16_deleteAttachmentNotFound() {
		setupCall()
			.withHttpMethod(DELETE)
			.withServicePath(format(ATTACHMENT_BY_ID_PATH, MUNICIPALITY_ID, NAMESPACE, ERRAND_ID, DECISION_ID, "1000"))
			.withHeader(HEADER_NAME, ADMIN_IDENTIFIER)
			.withExpectedResponseStatus(NOT_FOUND)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test17_patchAttachmentIgnoresReadOnlyFields() {
		// The request deliberately carries id, errandId, decisionId, municipalityId, namespace, version and hash. None of
		// them are patchable: the content hash in particular must keep describing the stored bytes, and decisionId must not
		// become a way of moving an attachment to another decision.
		setupCall()
			.withHttpMethod(PATCH)
			.withServicePath(format(ATTACHMENT_BY_ID_PATH, MUNICIPALITY_ID, NAMESPACE, ERRAND_ID, DECISION_ID, "1"))
			.withHeader(HEADER_NAME, ADMIN_IDENTIFIER)
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(NO_CONTENT)
			.sendRequestAndVerifyResponse();

		setupCall()
			.withHttpMethod(GET)
			.withServicePath(format(ATTACHMENTS_PATH, MUNICIPALITY_ID, NAMESPACE, ERRAND_ID, DECISION_ID))
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequest();
	}

	@Test
	void test18_patchAttachmentMergesExtraParameters() {
		// extraParameters are merged into the existing map, not replaced - two consecutive patches must leave both keys in
		// place.
		setupCall()
			.withHttpMethod(PATCH)
			.withServicePath(format(ATTACHMENT_BY_ID_PATH, MUNICIPALITY_ID, NAMESPACE, ERRAND_ID, DECISION_ID, "1"))
			.withHeader(HEADER_NAME, ADMIN_IDENTIFIER)
			.withRequest("request-first.json")
			.withExpectedResponseStatus(NO_CONTENT)
			.sendRequestAndVerifyResponse();

		setupCall()
			.withHttpMethod(PATCH)
			.withServicePath(format(ATTACHMENT_BY_ID_PATH, MUNICIPALITY_ID, NAMESPACE, ERRAND_ID, DECISION_ID, "1"))
			.withHeader(HEADER_NAME, ADMIN_IDENTIFIER)
			.withRequest("request-second.json")
			.withExpectedResponseStatus(NO_CONTENT)
			.sendRequest();

		setupCall()
			.withHttpMethod(GET)
			.withServicePath(format(ATTACHMENTS_PATH, MUNICIPALITY_ID, NAMESPACE, ERRAND_ID, DECISION_ID))
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequest();
	}

	@Test
	void test19_getAttachmentsInOtherNamespace() {
		setupCall()
			.withHttpMethod(GET)
			.withServicePath(format(ATTACHMENTS_PATH, MUNICIPALITY_ID, OTHER_NAMESPACE, ERRAND_ID, DECISION_ID))
			.withExpectedResponseStatus(NOT_FOUND)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test20_getAttachmentInOtherMunicipality() {
		setupCall()
			.withHttpMethod(GET)
			.withServicePath(format(ATTACHMENT_BY_ID_PATH, OTHER_MUNICIPALITY_ID, NAMESPACE, ERRAND_ID, DECISION_ID, "1"))
			.withExpectedResponseStatus(NOT_FOUND)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test21_patchAttachmentInOtherNamespace() {
		setupCall()
			.withHttpMethod(PATCH)
			.withServicePath(format(ATTACHMENT_BY_ID_PATH, MUNICIPALITY_ID, OTHER_NAMESPACE, ERRAND_ID, DECISION_ID, "1"))
			.withHeader(HEADER_NAME, ADMIN_IDENTIFIER)
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(NOT_FOUND)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test22_deleteAttachmentInOtherMunicipality() {
		setupCall()
			.withHttpMethod(DELETE)
			.withServicePath(format(ATTACHMENT_BY_ID_PATH, OTHER_MUNICIPALITY_ID, NAMESPACE, ERRAND_ID, DECISION_ID, "1"))
			.withHeader(HEADER_NAME, ADMIN_IDENTIFIER)
			.withExpectedResponseStatus(NOT_FOUND)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test23_getAttachmentsOnDecisionWithoutAttachments() {
		// Decision 2 (on errand 2) owns no attachments, which is an empty list rather than a 404.
		setupCall()
			.withHttpMethod(GET)
			.withServicePath(format(ATTACHMENTS_PATH, MUNICIPALITY_ID, NAMESPACE, "2", "2"))
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test24_streamDecisionAttachmentThroughErrandEndpoint() {
		// The errand download endpoint must not reach a decision attachment either, not just its listing.
		setupCall()
			.withHttpMethod(GET)
			.withServicePath(format(ERRAND_ATTACHMENTS_PATH + "/{3}", MUNICIPALITY_ID, NAMESPACE, ERRAND_ID, "1"))
			.withExpectedResponseStatus(NOT_FOUND)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test25_deleteDecisionRemovesItsAttachments() {
		// attachment.decision_id is a real foreign key, so the attachments have to be deleted before the decision itself.
		// A broken deletion order surfaces here as a constraint violation rather than as 204.
		setupCall()
			.withHttpMethod(DELETE)
			.withServicePath(format(DECISION_PATH, MUNICIPALITY_ID, NAMESPACE, ERRAND_ID, DECISION_ID))
			.withHeader(HEADER_NAME, ADMIN_IDENTIFIER)
			.withExpectedResponseStatus(NO_CONTENT)
			.sendRequest();

		// The decision is gone, and with it the way to reach its attachments.
		setupCall()
			.withHttpMethod(GET)
			.withServicePath(format(ATTACHMENTS_PATH, MUNICIPALITY_ID, NAMESPACE, ERRAND_ID, DECISION_ID))
			.withExpectedResponseStatus(NOT_FOUND)
			.sendRequest();

		// The errand's own attachment is untouched by the decision being deleted.
		setupCall()
			.withHttpMethod(GET)
			.withServicePath(format(ERRAND_ATTACHMENTS_PATH, MUNICIPALITY_ID, NAMESPACE, ERRAND_ID))
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequest();
	}

	@Test
	void test26_deleteErrandRemovesAllItsAttachments() {
		// Deleting the errand cascades to its decisions and from there to their attachments - a broken deletion order
		// would fail on the decision_id foreign key instead of returning 204. The errand's own attachments have no such
		// foreign key and no mapped relation, so they are deleted explicitly by the service; without that they would
		// survive the errand as rows still listed by the (errand id scoped) attachment endpoint.
		setupCall()
			.withHttpMethod(DELETE)
			.withServicePath(format("/{0}/{1}/errands/{2}", MUNICIPALITY_ID, NAMESPACE, ERRAND_ID))
			.withHeader(HEADER_NAME, ADMIN_IDENTIFIER)
			.withExpectedResponseStatus(NO_CONTENT)
			.sendRequest();

		setupCall()
			.withHttpMethod(GET)
			.withServicePath(format(ATTACHMENTS_PATH, MUNICIPALITY_ID, NAMESPACE, ERRAND_ID, DECISION_ID))
			.withExpectedResponseStatus(NOT_FOUND)
			.sendRequest();

		setupCall()
			.withHttpMethod(GET)
			.withServicePath(format(ERRAND_ATTACHMENTS_PATH, MUNICIPALITY_ID, NAMESPACE, ERRAND_ID))
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequest();
	}
}
