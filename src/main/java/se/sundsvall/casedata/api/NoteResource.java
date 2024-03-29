package se.sundsvall.casedata.api;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;
import static org.springframework.http.ResponseEntity.noContent;
import static org.springframework.http.ResponseEntity.ok;

import java.util.List;
import java.util.Optional;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.zalando.problem.Problem;
import org.zalando.problem.violations.ConstraintViolationProblem;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import se.sundsvall.casedata.api.model.NoteDTO;
import se.sundsvall.casedata.integration.db.model.enums.NoteType;
import se.sundsvall.casedata.service.NoteService;

@RestController
@Validated
@RequestMapping("/notes")
@Tag(name = "Notes", description = "Note operations")
@ApiResponses(value = {
	@ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(oneOf = { Problem.class, ConstraintViolationProblem.class }))),
	@ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class))),
	@ApiResponse(responseCode = "500", description = "Internal Server error", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
})
class NoteResource {

	private final NoteService noteService;

	NoteResource(final NoteService noteService) {
		this.noteService = noteService;
	}

	@Operation(description = "Get note by id",
		responses = {
			@ApiResponse(responseCode = "200", description = "OK - Successful operation", useReturnTypeSchema = true)
		})
	@GetMapping(path = "/{id}", produces = { APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE })
	ResponseEntity<NoteDTO> getNoteById(@PathVariable final Long id) {
		return ok(noteService.getNoteById(id));
	}

	@Operation(description = "Get notes for a specific errand, possible to filter by note type",
		responses = {
			@ApiResponse(responseCode = "200", description = "OK - Successful operation", useReturnTypeSchema = true)
		})
	@GetMapping(path = "/errand/{errandId}", produces = { APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE })
	ResponseEntity<List<NoteDTO>> getNotesByErrandId(@PathVariable final Long errandId, @RequestParam final Optional<NoteType> noteType) {
		return ok(noteService.getNotesByErrandIdAndNoteType(errandId, noteType));
	}

	@Operation(description = "Update note",
		responses = {
			@ApiResponse(responseCode = "204", description = "No content - Successful operation", useReturnTypeSchema = true)
		})
	@PatchMapping(path = "/{noteId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = { APPLICATION_PROBLEM_JSON_VALUE })
	ResponseEntity<Void> patchNoteOnErrand(@PathVariable final Long noteId, @RequestBody @Valid final NoteDTO noteDTO) {
		noteService.updateNote(noteId, noteDTO);
		return noContent().build();
	}

	@Operation(description = "Delete a note by note id",
		responses = {
			@ApiResponse(responseCode = "204", description = "No content - Successful operation", useReturnTypeSchema = true),
			@ApiResponse(responseCode = "403", description = "Forbidden")
		})
	@DeleteMapping(path = "/{noteId}", produces = { APPLICATION_PROBLEM_JSON_VALUE })
	ResponseEntity<Void> deleteNoteById(@PathVariable final Long noteId) {
		noteService.deleteNoteById(noteId);
		return noContent().build();
	}
}
