package se.sundsvall.casedata.apptest;

import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static se.sundsvall.casedata.TestUtil.OBJECT_MAPPER;
import static se.sundsvall.casedata.TestUtil.createAttachmentDTO;
import static se.sundsvall.casedata.api.model.validation.enums.AttachmentCategory.NOTIFICATION_WITHOUT_PERSONAL_NUMBER;
import static se.sundsvall.casedata.api.model.validation.enums.AttachmentCategory.PASSPORT_PHOTO;
import static se.sundsvall.casedata.api.model.validation.enums.AttachmentCategory.POLICE_REPORT;
import static se.sundsvall.casedata.apptest.util.TestConstants.AD_USER;
import static se.sundsvall.casedata.apptest.util.TestConstants.JWT_HEADER_VALUE;
import static se.sundsvall.casedata.service.util.Constants.X_JWT_ASSERTION_HEADER_KEY;

import java.text.MessageFormat;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;

import se.sundsvall.casedata.Application;
import se.sundsvall.casedata.api.model.AttachmentDTO;
import se.sundsvall.casedata.api.model.validation.enums.AttachmentCategory;
import se.sundsvall.casedata.integration.db.AttachmentRepository;
import se.sundsvall.casedata.integration.db.model.Attachment;
import se.sundsvall.casedata.service.util.Constants;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;

@WireMockAppTestSuite(files = "classpath:/AttachmentResourceIT", classes = Application.class)
@Sql({
	"/db/scripts/truncate.sql",
	"/db/scripts/testdata-it.sql"
})
class AttachmentResourceIT extends CustomAbstractAppTest {

	@Autowired
	private AttachmentRepository attachmentRepository;

	@Test
	void test1_GetAttachment() throws JsonProcessingException, ClassNotFoundException {
		final AttachmentDTO attachment = setupCall()
			.withHttpMethod(HttpMethod.GET)
			.withServicePath("/attachments/1")
			.withExpectedResponseStatus(HttpStatus.OK)
			.sendRequestAndVerifyResponse()
			.andReturnBody(AttachmentDTO.class);

		assertNotNull(attachment);
	}

	@Test
	void test2_GetAttachmentNotFound() {

		setupCall()
			.withHttpMethod(HttpMethod.GET)
			.withServicePath("/attachments/1000")
			.withExpectedResponseStatus(HttpStatus.NOT_FOUND)
			.sendRequestAndVerifyResponse();

		assertThat(attachmentRepository.findById(1000L)).isEmpty();
	}

	@Test
	void test3_PutAttachment() throws JsonProcessingException, ClassNotFoundException {

		final AttachmentDTO inputAttachmentDTO = createAttachmentDTO(POLICE_REPORT);

		setupCall()
			.withHttpMethod(HttpMethod.PUT)
			.withServicePath("/attachments/1")
			.withRequest(OBJECT_MAPPER.writeValueAsString(inputAttachmentDTO))
			.withHeader(X_JWT_ASSERTION_HEADER_KEY, JWT_HEADER_VALUE)
			.withHeader(Constants.AD_USER_HEADER_KEY, AD_USER)
			.withExpectedResponseStatus(HttpStatus.NO_CONTENT)
			.sendRequestAndVerifyResponse();

		final AttachmentDTO attachmentAfter = setupCall()
			.withHttpMethod(HttpMethod.GET)
			.withServicePath("/attachments/1")
			.withExpectedResponseStatus(HttpStatus.OK)
			.sendRequestAndVerifyResponse()
			.andReturnBody(AttachmentDTO.class);

		assertEquals(inputAttachmentDTO.getCategory(), attachmentAfter.getCategory());
		assertEquals(inputAttachmentDTO.getExtension(), attachmentAfter.getExtension());
		assertEquals(inputAttachmentDTO.getFile(), attachmentAfter.getFile());
		assertEquals(inputAttachmentDTO.getMimeType(), attachmentAfter.getMimeType());
		assertEquals(inputAttachmentDTO.getName(), attachmentAfter.getName());
		assertEquals(inputAttachmentDTO.getNote(), attachmentAfter.getNote());
		assertEquals(inputAttachmentDTO.getExtraParameters(), attachmentAfter.getExtraParameters());
	}

	@Test
	void test4_PutAttachmentNotFound() throws JsonProcessingException {
		final var inputAttachmentDTO = createAttachmentDTO(NOTIFICATION_WITHOUT_PERSONAL_NUMBER);
		final var randomId = new Random().nextLong();

		setupCall()
			.withHttpMethod(HttpMethod.PUT)
			.withServicePath(MessageFormat.format("/attachments/{0}", String.valueOf(randomId)))
			.withRequest(OBJECT_MAPPER.writeValueAsString(inputAttachmentDTO))
			.withHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
			.withExpectedResponseStatus(HttpStatus.NOT_FOUND)
			.sendRequestAndVerifyResponse();

		assertThat(attachmentRepository.findById(randomId)).isEmpty();
	}

	@Test
	void test5_DeleteAttachmentOnErrand() {
		final var result = attachmentRepository.save(Attachment.builder().build());
		Assertions.assertTrue(attachmentRepository.findById(result.getId()).isPresent());

		setupCall()
			.withHttpMethod(HttpMethod.DELETE)
			.withServicePath(MessageFormat.format("/attachments/{0}", result.getId()))
			.withHeader(X_JWT_ASSERTION_HEADER_KEY, JWT_HEADER_VALUE)
			.withHeader(Constants.AD_USER_HEADER_KEY, AD_USER)
			.withExpectedResponseStatus(HttpStatus.NO_CONTENT)
			.sendRequestAndVerifyResponse();

		assertThat(attachmentRepository.findById(result.getId())).isEmpty();
	}

	@Test
	void test6_DeleteAttachmentOnErrandNotFound_1() {

		final var id = new Random().nextLong(100, 10000);

		setupCall()
			.withHttpMethod(HttpMethod.DELETE)
			.withServicePath(MessageFormat.format("/attachments/{0}", String.valueOf(id)))
			.withExpectedResponseStatus(HttpStatus.NOT_FOUND)
			.sendRequestAndVerifyResponse();

		assertThat(attachmentRepository.findById(id)).isEmpty();
	}

	@Test
	void test8_createAttachment() throws JsonProcessingException {

		final var initialAttachments = attachmentRepository.findAll();
		final var dto = createAttachmentDTO(AttachmentCategory.SIGNATURE);
		dto.setErrandNumber("1234567890");
		setupCall()
			.withHttpMethod(HttpMethod.POST)
			.withServicePath("/attachments")
			.withRequest(OBJECT_MAPPER.writeValueAsString(dto))
			.withExpectedResponseStatus(HttpStatus.CREATED)
			.sendRequestAndVerifyResponse();

		final var result = attachmentRepository.findAll();

		assertThat(result).hasSize(initialAttachments.size() + 1).element(result.size() - 1).satisfies(attachment -> {
			assertThat(attachment.getId()).isNotNull();
			assertThat(attachment.getCategory()).isEqualTo(dto.getCategory());
			assertThat(attachment.getExtension()).isEqualTo(dto.getExtension());
			assertThat(attachment.getFile()).isEqualTo(dto.getFile());
			assertThat(attachment.getMimeType()).isEqualTo(dto.getMimeType());
			assertThat(attachment.getName()).isEqualTo(dto.getName());
			assertThat(attachment.getNote()).isEqualTo(dto.getNote());
			assertThat(attachment.getErrandNumber()).isEqualTo(dto.getErrandNumber());
			assertThat(attachment.getExtraParameters()).isEqualTo(dto.getExtraParameters());
		});
	}

	@Test
	void test9_patchAttachmentNotFound() throws JsonProcessingException {

		final var dto = AttachmentDTO.builder().withCategory(AttachmentCategory.NOTIFICATION.toString()).build();
		setupCall()
			.withHttpMethod(HttpMethod.PATCH)
			.withServicePath("/attachments/1000")
			.withRequest(OBJECT_MAPPER.writeValueAsString(dto))
			.withExpectedResponseStatus(HttpStatus.NOT_FOUND)
			.sendRequestAndVerifyResponse();

		assertThat(attachmentRepository.findById(1000L)).isEmpty();
	}

	@Test
	void test10_getAttachmentsByErrandNumber() throws JsonProcessingException {

		final var result = setupCall()
			.withHttpMethod(HttpMethod.GET)
			.withServicePath("/attachments/errand/ERRAND-NUMBER-2")
			.withExpectedResponseStatus(HttpStatus.OK)
			.sendRequestAndVerifyResponse()
			.andReturnBody(new TypeReference<List<AttachmentDTO>>() {

			});

		assertThat(result).hasSize(3)
			.extracting(AttachmentDTO::getErrandNumber,
				AttachmentDTO::getCategory,
				AttachmentDTO::getExtension,
				AttachmentDTO::getFile,
				AttachmentDTO::getMimeType,
				AttachmentDTO::getName,
				AttachmentDTO::getNote,
				AttachmentDTO::getExtraParameters)
			.containsExactly(
				tuple("ERRAND-NUMBER-2", PASSPORT_PHOTO.toString(), ".pdf", "FILE-2", "application/pdf", "test2.pdf", "NOTE-2", emptyMap()),
				tuple("ERRAND-NUMBER-2", POLICE_REPORT.toString(), ".pdf", "FILE-3", "application/pdf", "test3.pdf", "NOTE-3", emptyMap()),
				tuple("ERRAND-NUMBER-2", NOTIFICATION_WITHOUT_PERSONAL_NUMBER.toString(), ".pdf", "FILE-4", "application/pdf", "test4.pdf", "NOTE-4", emptyMap()));
	}
}
