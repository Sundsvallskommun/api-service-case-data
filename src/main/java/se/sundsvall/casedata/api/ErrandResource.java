package se.sundsvall.casedata.api;

import com.turkraft.springfilter.boot.Filter;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zalando.problem.Problem;
import org.zalando.problem.violations.ConstraintViolationProblem;
import se.sundsvall.casedata.api.model.AppealDTO;
import se.sundsvall.casedata.api.model.DecisionDTO;
import se.sundsvall.casedata.api.model.ErrandDTO;
import se.sundsvall.casedata.api.model.ExtraParameterDTO;
import se.sundsvall.casedata.api.model.FacilityDTO;
import se.sundsvall.casedata.api.model.NoteDTO;
import se.sundsvall.casedata.api.model.PatchErrandDTO;
import se.sundsvall.casedata.api.model.StakeholderDTO;
import se.sundsvall.casedata.api.model.StatusDTO;
import se.sundsvall.casedata.integration.db.model.Errand;
import se.sundsvall.casedata.service.ErrandService;

import java.util.List;
import java.util.Optional;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.http.MediaType.ALL_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;
import static org.springframework.http.ResponseEntity.created;
import static org.springframework.http.ResponseEntity.noContent;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.web.util.UriComponentsBuilder.fromPath;

@RestController
@Validated
@RequestMapping("/errands")
@Tag(name = "Errands", description = "Errand operations")
@ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(oneOf = { Problem.class, ConstraintViolationProblem.class })))
@ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
@ApiResponse(responseCode = "500", description = "Internal Server error", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
class ErrandResource {

	private final ErrandService errandService;

	ErrandResource(final ErrandService errandService) {
		this.errandService = errandService;
	}

	/***
	 * Errand operations
	 */

	@Operation(description = "Create errand (without attachments). Add attachments to errand with PATCH /errands/{id}/attachments afterwards.",
		responses = {
			@ApiResponse(responseCode = "201", description = "Created - Successful operation", headers = @Header(name = LOCATION, schema = @Schema(type = "string")), useReturnTypeSchema = true)
		})
	@PostMapping(consumes = APPLICATION_JSON_VALUE, produces = { ALL_VALUE, APPLICATION_PROBLEM_JSON_VALUE })
	ResponseEntity<Void> postErrands(@RequestBody @Valid final ErrandDTO errandDTO) {
		final ErrandDTO result = errandService.createErrand(errandDTO);
		return created(
			fromPath("/errands/{id}")
				.buildAndExpand(result.getId())
				.toUri())
			.header(CONTENT_TYPE, ALL_VALUE)
			.build();
	}

	@Operation(description = "Get errand by ID.")
	@GetMapping(path = "/{id}", produces = { APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE })
	@ApiResponse(responseCode = "200", description = "OK - Successful operation", useReturnTypeSchema = true)
	ResponseEntity<ErrandDTO> getErrandById(@PathVariable final Long id) {
		return ok(errandService.findById(id));
	}

	@Operation(description = "Update errand.")
	@PatchMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = { APPLICATION_PROBLEM_JSON_VALUE })
	@ApiResponse(responseCode = "204", description = "No content - Successful operation", useReturnTypeSchema = true)
	ResponseEntity<Void> patchErrand(@PathVariable final Long id, @RequestBody @Valid final PatchErrandDTO patchErrandDTO) {
		errandService.updateErrand(id, patchErrandDTO);
		return noContent().build();
	}

	@Hidden // Should be a hidden operation in the API.
	@Operation(description = "Delete errand by ID.")
	@DeleteMapping(path = "/{id}", produces = { APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE })
	@ApiResponse(responseCode = "204", description = "No content - Successful operation", useReturnTypeSchema = true)
	ResponseEntity<Void> deleteErrandById(@PathVariable final Long id) {
		errandService.deleteById(id);
		return noContent().build();
	}

	@GetMapping(produces = { APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE })
	@Operation(description = "Get errands with or without query. The query is very flexible and allows you as a client to control a lot yourself.")
	@ApiResponse(responseCode = "200", description = "OK - Successful operation", useReturnTypeSchema = true)
	ResponseEntity<Page<ErrandDTO>> getErrands(
		@Parameter(
			description = "Syntax description: [spring-filter](https://github.com/turkraft/spring-filter/blob/85730f950a5f8623159cc0eb4d737555f9382bb7/README.md#syntax)",
			example = "caseType:'PARKING_PERMIT' and stakeholders.firstName~'*mar*' and applicationReceived>'2022-09-08T12:18:03.747+02:00'",
			schema = @Schema(implementation = String.class)) @Filter final Specification<Errand> filter,
		@Parameter(description = "extraParameters on errand. Use like this: extraParameters[artefact.permit.number]=12345&extraParameters[disability.aid]=Rullstol") final Optional<ExtraParameterDTO> extraParameterDTO,
		@ParameterObject final Pageable pageable) {
		return ok(errandService.findAll(filter, extraParameterDTO.orElse(new ExtraParameterDTO()).getExtraParameters(), pageable));
	}

	/***
	 * Errand decision operations
	 */

	@Operation(description = "Create and add decision to errand.",
		responses = {
			@ApiResponse(responseCode = "201", description = "Created - Successful operation", headers = @Header(name = LOCATION, description = "Location of the created resource."), useReturnTypeSchema = true)
		})
	@PatchMapping(path = "/{id}/decisions", consumes = APPLICATION_JSON_VALUE, produces = { ALL_VALUE, APPLICATION_PROBLEM_JSON_VALUE })
	ResponseEntity<Void> patchErrandWithDecision(
		@PathVariable final Long id,
		@RequestBody @Valid final DecisionDTO decisionDTO) {

		final var dto = errandService.addDecisionToErrand(id, decisionDTO);
		return created(fromPath("/decisions/{id}").buildAndExpand(dto.getId()).toUri())
			.header(CONTENT_TYPE, ALL_VALUE)
			.build();
	}

	@Operation(description = "Get decisions on errand.")
	@GetMapping(path = "/{id}/decisions", produces = { APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE })
	@ApiResponse(responseCode = "200", description = "OK - Successful operation", useReturnTypeSchema = true)
	ResponseEntity<List<DecisionDTO>> getDecision(@PathVariable final Long id) {
		return ok(errandService.findDecisionsOnErrand(id));
	}

	@Operation(description = "Delete decision on errand.")
	@DeleteMapping(path = "/{id}/decisions/{decisionId}", produces = { APPLICATION_PROBLEM_JSON_VALUE })
	@ApiResponse(responseCode = "204", description = "No content - Successful operation", useReturnTypeSchema = true)
	ResponseEntity<Void> deleteDecision(@PathVariable final Long id, @PathVariable final Long decisionId) {
		errandService.deleteDecisionOnErrand(id, decisionId);
		return noContent().build();
	}

	/***
	 * Errand facilities operations
	 */

	@Operation(description = "Create errand facility.")
	@PostMapping(path = "/{id}/facilities", consumes = APPLICATION_JSON_VALUE, produces = { ALL_VALUE, APPLICATION_PROBLEM_JSON_VALUE })
	@ApiResponse(responseCode = "201", description = "Created - Successful operation", headers = @Header(name = LOCATION, schema = @Schema(type = "string")), useReturnTypeSchema = true)
	ResponseEntity<Void> postErrandFacility(@PathVariable final Long id, @RequestBody @Valid final FacilityDTO facilityDTO) {
		final FacilityDTO result = errandService.createFacility(id, facilityDTO);
		return created(
			fromPath("/errands/{id}/facilities/{facilityId}")
				.buildAndExpand(id, result.getId())
				.toUri())
			.header(CONTENT_TYPE, ALL_VALUE)
			.build();
	}

	@Operation(description = "Get all facilities on errand.")
	@GetMapping(path = "/{id}/facilities", produces = { APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE })
	@ApiResponse(responseCode = "200", description = "OK - Successful operation", useReturnTypeSchema = true)
	ResponseEntity<List<FacilityDTO>> getFacilities(@PathVariable final Long id) {
		return ok(errandService.findFacilitiesOnErrand(id));
	}

	@Operation(description = "Get a specific facility on errand.")
	@GetMapping(path = "/{id}/facilities/{facilityId}", produces = { APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE })
	@ApiResponse(responseCode = "200", description = "OK - Successful operation", useReturnTypeSchema = true)
	ResponseEntity<FacilityDTO> getFacility(@PathVariable final Long id, @PathVariable final Long facilityId) {
		return ok(errandService.findFacilityOnErrand(id, facilityId));
	}

	@Operation(description = "Update errand facility")
	@PatchMapping(path = "/{id}/facilities/{facilityId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = { APPLICATION_PROBLEM_JSON_VALUE })
	@ApiResponse(responseCode = "204", description = "No content - Successful operation", useReturnTypeSchema = true)
	ResponseEntity<Void> patchErrandFacility(@PathVariable final Long id, @PathVariable final Long facilityId, @RequestBody @Valid final FacilityDTO facilityDTO) {
		errandService.updateFacilityOnErrand(id, facilityId, facilityDTO);
		return noContent().build();
	}

	@Operation(description = "Add/replace facility on errand.")
	@PutMapping(path = "/{id}/facilities", consumes = MediaType.APPLICATION_JSON_VALUE, produces = { APPLICATION_PROBLEM_JSON_VALUE })
	@ApiResponse(responseCode = "204", description = "No content - Successful operation", useReturnTypeSchema = true)
	ResponseEntity<Void> putFacilitiesOnErrand(@PathVariable final Long id, @RequestBody @Valid final List<FacilityDTO> facilityDTOs) {
		//TODO Add call to service layer when implemented
		return noContent().build();
	}

	@Operation(description = "Delete facility on errand.")
	@DeleteMapping(path = "/{id}/facilities/{facilityId}", produces = { APPLICATION_PROBLEM_JSON_VALUE })
	@ApiResponse(responseCode = "204", description = "No content - Successful operation", useReturnTypeSchema = true)
	ResponseEntity<Void> deleteFacility(@PathVariable final Long id, @PathVariable Long facilityId) {
		errandService.deleteFacilityOnErrand(id, facilityId);
		return noContent().build();
	}

	/***
	 * Errand status operations
	 */

	@Operation(description = "Add status to errand.")
	@PatchMapping(path = "/{id}/statuses", consumes = MediaType.APPLICATION_JSON_VALUE, produces = { APPLICATION_PROBLEM_JSON_VALUE })
	@ApiResponse(responseCode = "204", description = "No content - Successful operation", useReturnTypeSchema = true)
	ResponseEntity<Void> patchErrandWithStatus(@PathVariable final Long id, @RequestBody @Valid final StatusDTO statusDTO) {
		errandService.addStatusToErrand(id, statusDTO);
		return noContent().build();
	}

	@Operation(description = "Add/replace status on errand.")
	@PutMapping(path = "/{id}/statuses", consumes = MediaType.APPLICATION_JSON_VALUE, produces = { APPLICATION_PROBLEM_JSON_VALUE })
	@ApiResponse(responseCode = "204", description = "No content - Successful operation", useReturnTypeSchema = true)
	ResponseEntity<Void> putStatusOnErrand(@PathVariable final Long id, @RequestBody @Valid final List<StatusDTO> statusDTOList) {
		errandService.replaceStatusesOnErrand(id, statusDTOList);
		return noContent().build();
	}

	/***
	 * Errand note operations
	 */

	@Operation(description = "Create and add note to errand.",
		responses = {
			@ApiResponse(responseCode = "201", description = "Created - Successful operation", headers = @Header(name = LOCATION, description = "Location of the created resource."), useReturnTypeSchema = true)
		})
	@PatchMapping(path = "/{id}/notes", consumes = APPLICATION_JSON_VALUE, produces = { ALL_VALUE, APPLICATION_PROBLEM_JSON_VALUE })
	ResponseEntity<Void> patchErrandWithNote(
		@PathVariable final Long id,
		@RequestBody @Valid final NoteDTO noteDTO) {

		final var dto = errandService.addNoteToErrand(id, noteDTO);
		return created(fromPath("/notes/{id}").buildAndExpand(dto.getId()).toUri())
			.header(CONTENT_TYPE, ALL_VALUE)
			.build();
	}

	@Operation(description = "Delete note on errand.")
	@DeleteMapping(path = "/{id}/notes/{noteId}", produces = { APPLICATION_PROBLEM_JSON_VALUE })
	@ApiResponse(responseCode = "204", description = "No content - Successful operation", useReturnTypeSchema = true)
	ResponseEntity<Void> deleteNote(@PathVariable final Long id, @PathVariable final Long noteId) {
		errandService.deleteNoteOnErrand(id, noteId);
		return noContent().build();
	}

	/***
	 * Errand appeal operations
	 */

	@Operation(description = "Create and add appeal to errand.",
		responses = {
			@ApiResponse(responseCode = "201", description = "Created - Successful operation", headers = @Header(name = LOCATION, description = "Location of the created resource."), useReturnTypeSchema = true)
		})
	@PatchMapping(path = "/{id}/appeals", consumes = APPLICATION_JSON_VALUE, produces = { ALL_VALUE, APPLICATION_PROBLEM_JSON_VALUE })
	ResponseEntity<Void> patchErrandWithAppeal(
		@PathVariable final Long id,
		@RequestBody @Valid final AppealDTO appealDTO) {

		final var dto = errandService.addAppealToErrand(id, appealDTO);
		return created(fromPath("/appeals/{id}").buildAndExpand(dto.getId()).toUri())
			.header(CONTENT_TYPE, ALL_VALUE)
			.build();
	}

	@Operation(description = "Delete appeal on errand.")
	@DeleteMapping(path = "/{id}/appeals/{appealId}", produces = { APPLICATION_PROBLEM_JSON_VALUE })
	@ApiResponse(responseCode = "204", description = "No content - Successful operation", useReturnTypeSchema = true)
	ResponseEntity<Void> deleteAppeal(@PathVariable final Long id, @PathVariable final Long appealId) {
		errandService.deleteAppealOnErrand(id, appealId);
		return noContent().build();
	}

	/***
	 * Errand stakeholder operations
	 */

	@Operation(description = "Create and add stakeholder to errand.",
		responses = { @ApiResponse(responseCode = "201", description = "Created - Successful operation", headers = @Header(name = LOCATION, description = "Location of the created resource."), useReturnTypeSchema = true)
		})
	@PatchMapping(path = "/{id}/stakeholders", consumes = APPLICATION_JSON_VALUE, produces = { ALL_VALUE, APPLICATION_PROBLEM_JSON_VALUE })
	ResponseEntity<Void> patchErrandWithStakeholder(
		@PathVariable final Long id,
		@RequestBody @Valid final StakeholderDTO stakeholderDTO) {

		final var dto = errandService.addStakeholderToErrand(id, stakeholderDTO);
		return created(fromPath("/stakeholders/{id}").buildAndExpand(dto.getId()).toUri())
			.header(CONTENT_TYPE, ALL_VALUE)
			.build();
	}

	@Operation(description = "Replace stakeholders on errand.")
	@PutMapping(path = "/{id}/stakeholders", consumes = MediaType.APPLICATION_JSON_VALUE, produces = { APPLICATION_PROBLEM_JSON_VALUE })
	@ApiResponse(responseCode = "204", description = "No content - Successful operation", useReturnTypeSchema = true)
	ResponseEntity<Void> putStakeholdersOnErrand(@PathVariable final Long id, @RequestBody @Valid final List<StakeholderDTO> stakeholderDTOList) {
		errandService.replaceStakeholdersOnErrand(id, stakeholderDTOList);
		return noContent().build();
	}

	@Operation(description = "Delete stakeholder on errand.")
	@DeleteMapping(path = "/{id}/stakeholders/{stakeholderId}", produces = { APPLICATION_PROBLEM_JSON_VALUE })
	@ApiResponse(responseCode = "204", description = "No content - Successful operation", useReturnTypeSchema = true)
	ResponseEntity<Void> deleteStakeholder(@PathVariable final Long id, @PathVariable final Long stakeholderId) {
		errandService.deleteStakeholderOnErrand(id, stakeholderId);
		return noContent().build();
	}
}
