package se.sundsvall.casedata.apptest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static se.sundsvall.casedata.TestUtil.OBJECT_MAPPER;
import static se.sundsvall.casedata.TestUtil.createNoteDTO;
import static se.sundsvall.casedata.apptest.util.TestConstants.AD_USER;
import static se.sundsvall.casedata.apptest.util.TestConstants.JWT_HEADER_VALUE;
import static se.sundsvall.casedata.apptest.util.TestConstants.SUBSCRIBER;
import static se.sundsvall.casedata.service.util.Constants.AD_USER_HEADER_KEY;
import static se.sundsvall.casedata.service.util.Constants.UNKNOWN;
import static se.sundsvall.casedata.service.util.Constants.X_JWT_ASSERTION_HEADER_KEY;

import java.text.MessageFormat;
import java.util.Comparator;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.jdbc.Sql;

import se.sundsvall.casedata.CaseDataApplication;
import se.sundsvall.casedata.api.model.ErrandDTO;
import se.sundsvall.casedata.api.model.NoteDTO;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;

@WireMockAppTestSuite(files = "classpath:/NoteIT/", classes = CaseDataApplication.class)
@Sql({
	"/db/scripts/truncate.sql",
	"/db/scripts/testdata-it.sql"
})
class NoteIT extends CustomAbstractAppTest {

	private static final String AD_USER_HEADER_VALUE = "test";

	private static final String[] EXCLUDE_FIELDS = {
		"id",
		"version",
		"created",
		"updated",
		"createdBy",
		"updatedBy"};

	@Test
	void test1_getNote() throws JsonProcessingException, ClassNotFoundException {
		final var result = setupCall()
			.withHttpMethod(HttpMethod.GET)
			.withServicePath("/notes/1")
			.withExpectedResponseStatus(HttpStatus.OK)
			.sendRequestAndVerifyResponse()
			.andReturnBody(NoteDTO.class);

		assertThat(result).isNotNull();
	}

	@Test
	void test2_patchErrandWithNote() throws JsonProcessingException, ClassNotFoundException {
		final var errandBeforePatch = setupCall()
			.withHttpMethod(HttpMethod.GET)
			.withServicePath("/errands/1")
			.withExpectedResponseStatus(HttpStatus.OK)
			.sendRequestAndVerifyResponse()
			.andReturnBody(ErrandDTO.class);

		final var noteDTO = NoteDTO.builder().withText("This is a patch").build();

		setupCall()
			.withHttpMethod(HttpMethod.PATCH)
			.withServicePath("/errands/1/notes")
			.withHeader(AD_USER_HEADER_KEY, AD_USER_HEADER_VALUE)
			.withRequest(OBJECT_MAPPER.writeValueAsString(noteDTO))
			.withExpectedResponseStatus(HttpStatus.CREATED)
			.sendRequestAndVerifyResponse();

		final var errandAfterPatch = setupCall()
			.withHttpMethod(HttpMethod.GET)
			.withServicePath("/errands/1")
			.withExpectedResponseStatus(HttpStatus.OK)
			.sendRequestAndVerifyResponse()
			.andReturnBody(ErrandDTO.class);

		// Errand assertions
		assertThat(errandAfterPatch.getNotes()).isNotEqualTo(errandBeforePatch.getNotes());
		assertTrue(errandAfterPatch.getUpdated().isAfter(errandBeforePatch.getUpdated()));
		assertThat(errandAfterPatch.getCreatedBy()).isEqualTo(UNKNOWN);
		assertThat(errandAfterPatch.getUpdatedBy()).isEqualTo(AD_USER_HEADER_VALUE);
		assertThat(errandAfterPatch.getNotes()).hasSizeGreaterThan(errandBeforePatch.getNotes().size());

		// Note assertions
		final var patchedNote = errandAfterPatch.getNotes().stream().max(Comparator.comparing(NoteDTO::getUpdated)).orElseThrow();
		errandBeforePatch.getNotes().forEach(note -> assertThat(note.getCreatedBy()).isEqualTo(UNKNOWN));
		assertThat(noteDTO).isNotEqualTo(patchedNote);
		assertThat(patchedNote.getVersion()).isGreaterThan(noteDTO.getVersion());
		assertThat(patchedNote.getCreatedBy()).isEqualTo(AD_USER_HEADER_VALUE);
		assertThat(patchedNote.getUpdatedBy()).isEqualTo(AD_USER_HEADER_VALUE);

		assertThat(noteDTO)
			.usingRecursiveComparison()
			.ignoringFieldsMatchingRegexes(
				EXCLUDE_FIELDS)
			.isEqualTo(patchedNote);
	}

	@Test
	void test3_patchErrandWithNoteUnknownUser() throws JsonProcessingException, ClassNotFoundException {
		setupCall()
			.withHttpMethod(HttpMethod.GET)
			.withServicePath("/errands/1")
			.withExpectedResponseStatus(HttpStatus.OK)
			.sendRequestAndVerifyResponse()
			.andReturnBody(ErrandDTO.class);

		final var noteDTO = NoteDTO.builder().withText("This is a patch").build();

		setupCall()
			.withHttpMethod(HttpMethod.PATCH)
			.withServicePath("/errands/1/notes")
			.withRequest(OBJECT_MAPPER.writeValueAsString(noteDTO))
			.withExpectedResponseStatus(HttpStatus.CREATED)
			.sendRequestAndVerifyResponse();

		final ErrandDTO resultErrand = setupCall()
			.withHttpMethod(HttpMethod.GET)
			.withServicePath("/errands/1")
			.withExpectedResponseStatus(HttpStatus.OK)
			.sendRequestAndVerifyResponse()
			.andReturnBody(ErrandDTO.class);

		final var patchedNote = resultErrand.getNotes().stream()
			.max(Comparator.comparing(NoteDTO::getUpdated))
			.orElseThrow();

		assertThat(noteDTO).isNotEqualTo(patchedNote);
		assertThat(patchedNote.getVersion()).isGreaterThan(noteDTO.getVersion());
		assertThat(patchedNote.getCreatedBy()).isEqualTo(UNKNOWN);
		assertThat(patchedNote.getUpdatedBy()).isEqualTo(UNKNOWN);

		assertThat(noteDTO)
			.usingRecursiveComparison()
			.ignoringFieldsMatchingRegexes(
				EXCLUDE_FIELDS)
			.isEqualTo(resultErrand.getNotes().stream().max(Comparator.comparing(NoteDTO::getUpdated)).orElseThrow());
	}

	@Test
	void test4_patchNoteOnErrand() throws JsonProcessingException, ClassNotFoundException {
		final var errandDTO = setupCall()
			.withHttpMethod(HttpMethod.GET)
			.withServicePath("/errands/1")
			.withExpectedResponseStatus(HttpStatus.OK)
			.sendRequestAndVerifyResponse()
			.andReturnBody(ErrandDTO.class);

		setupCall()
			.withHttpMethod(HttpMethod.PATCH)
			.withServicePath(MessageFormat.format("/errands/{0}/notes", errandDTO.getId()))
			.withHeader(X_JWT_ASSERTION_HEADER_KEY, JWT_HEADER_VALUE)
			.withHeader(AD_USER_HEADER_KEY, AD_USER)
			.withRequest(OBJECT_MAPPER.writeValueAsString(createNoteDTO()))
			.withExpectedResponseStatus(HttpStatus.CREATED)
			.sendRequestAndVerifyResponse();

		assertErrandWasUpdatedAfterChange(errandDTO, SUBSCRIBER, AD_USER);

		final var resultErrandBeforePatch = setupCall()
			.withHttpMethod(HttpMethod.GET)
			.withServicePath(MessageFormat.format("/errands/{0}", errandDTO.getId()))
			.withExpectedResponseStatus(HttpStatus.OK)
			.sendRequestAndVerifyResponse()
			.andReturnBody(ErrandDTO.class);

		resultErrandBeforePatch.getNotes().sort(Comparator.comparing(NoteDTO::getCreated).reversed());
		final var resultNoteBeforePatch = resultErrandBeforePatch.getNotes().getFirst();

		final var patch = new NoteDTO();
		patch.setText("This is a patch");

		setupCall()
			.withHttpMethod(HttpMethod.PATCH)
			.withServicePath(MessageFormat.format("/notes/{0}", resultNoteBeforePatch.getId()))
			.withHeader(X_JWT_ASSERTION_HEADER_KEY, null)
			.withHeader(AD_USER_HEADER_KEY, null)
			.withRequest(OBJECT_MAPPER.writeValueAsString(patch))
			.withExpectedResponseStatus(HttpStatus.NO_CONTENT)
			.sendRequestAndVerifyResponse();

		assertErrandWasUpdatedAfterChange(resultErrandBeforePatch, UNKNOWN, UNKNOWN);

		final var resultErrand = setupCall()
			.withHttpMethod(HttpMethod.GET)
			.withServicePath(MessageFormat.format("/errands/{0}", errandDTO.getId()))
			.withExpectedResponseStatus(HttpStatus.OK)
			.sendRequestAndVerifyResponse()
			.andReturnBody(ErrandDTO.class);

		// Verify the errand contains this note
		assertThat(resultErrand.getNotes())
			.usingRecursiveFieldByFieldElementComparatorIgnoringFields(ArrayUtils.addAll(new String[]{"updatedBy", "text"}, EXCLUDE_FIELDS))
			.contains(resultNoteBeforePatch);
	}

	private void assertErrandWasUpdatedAfterChange(final ErrandDTO errandDTO, final String subscriber, final String adUser) throws JsonProcessingException, ClassNotFoundException {
		final var errandAfter = setupCall()
			.withHttpMethod(HttpMethod.GET)
			.withServicePath(MessageFormat.format("/errands/{0}", errandDTO.getId()))
			.withExpectedResponseStatus(HttpStatus.OK)
			.sendRequestAndVerifyResponse()
			.andReturnBody(ErrandDTO.class);
		assertThat(errandAfter.getUpdated()).isAfter(errandDTO.getUpdated());
		assertThat(errandDTO.getUpdatedByClient()).isNotEqualTo(errandAfter.getUpdatedByClient());
		assertThat(subscriber).isEqualTo(errandAfter.getUpdatedByClient());
		assertThat(errandDTO.getUpdatedBy()).isNotEqualTo(errandAfter.getUpdatedBy());
		assertThat(adUser).isEqualTo(errandAfter.getUpdatedBy());
	}
}
