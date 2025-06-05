package se.sundsvall.casedata.api;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.http.MediaType.ALL_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;
import static org.springframework.http.ResponseEntity.created;
import static org.springframework.http.ResponseEntity.noContent;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.web.util.UriComponentsBuilder.fromPath;
import static se.sundsvall.casedata.service.util.Constants.NAMESPACE_REGEXP;
import static se.sundsvall.casedata.service.util.Constants.NAMESPACE_VALIDATION_MESSAGE;

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
import jakarta.validation.constraints.Pattern;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zalando.problem.Problem;
import org.zalando.problem.violations.ConstraintViolationProblem;
import se.sundsvall.casedata.api.model.Errand;
import se.sundsvall.casedata.api.model.PatchErrand;
import se.sundsvall.casedata.integration.db.model.ErrandEntity;
import se.sundsvall.casedata.service.ErrandService;
import se.sundsvall.casedata.service.ProcessService;
import se.sundsvall.dept44.common.validators.annotation.ValidMunicipalityId;

@RestController
@Validated
@RequestMapping("/{municipalityId}")
@Tag(name = "Errands", description = "Errand operations")
@ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(oneOf = {
	Problem.class, ConstraintViolationProblem.class
})))
@ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
@ApiResponse(responseCode = "500", description = "Internal Server error", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
class ErrandResource {

	private final ErrandService errandService;

	private final ProcessService processService;

	ErrandResource(final ErrandService errandService, final ProcessService processService) {
		this.errandService = errandService;
		this.processService = processService;
	}

	@PostMapping(path = "/{namespace}/errands", consumes = APPLICATION_JSON_VALUE, produces = ALL_VALUE)
	@Operation(description = "Create errand (without attachments). Add attachments to errand with PATCH /errands/{id}/attachments afterwards.", responses = {
		@ApiResponse(responseCode = "201", description = "Created - Successful operation", headers = @Header(name = LOCATION, schema = @Schema(type = "string")), useReturnTypeSchema = true)
	})
	ResponseEntity<Void> createErrand(
		@Parameter(name = "municipalityId", description = "Municipality ID", example = "2281") @PathVariable(name = "municipalityId") @ValidMunicipalityId final String municipalityId,
		@Parameter(name = "namespace", description = "Namespace", example = "MY_NAMESPACE") @Pattern(regexp = NAMESPACE_REGEXP, message = NAMESPACE_VALIDATION_MESSAGE) @PathVariable final String namespace,
		@RequestBody @Valid final Errand errand) {

		final Errand result = errandService.create(errand, municipalityId, namespace);
		return created(
			fromPath("/{municipalityId}/{namespace}/errands/{id}")
				.buildAndExpand(municipalityId, namespace, result.getId())
				.toUri())
			.header(CONTENT_TYPE, ALL_VALUE)
			.build();
	}

	@GetMapping(path = "/{namespace}/errands/{errandId}", produces = APPLICATION_JSON_VALUE)
	@Operation(description = "Get errand by ID.", responses = {
		@ApiResponse(responseCode = "200", description = "OK - Successful operation", useReturnTypeSchema = true)
	})
	ResponseEntity<Errand> getErrandById(
		@Parameter(name = "municipalityId", description = "Municipality ID", example = "2281") @PathVariable(name = "municipalityId") @ValidMunicipalityId final String municipalityId,
		@Parameter(name = "namespace", description = "Namespace", example = "MY_NAMESPACE") @Pattern(regexp = NAMESPACE_REGEXP, message = NAMESPACE_VALIDATION_MESSAGE) @PathVariable final String namespace,
		@Parameter(name = "errandId", description = "Errand ID", example = "123") @PathVariable(name = "errandId") final Long errandId) {

		return ok(errandService.findByIdAndMunicipalityIdAndNamespace(errandId, municipalityId, namespace));
	}

	@PatchMapping(path = "/{namespace}/errands/{errandId}", consumes = APPLICATION_JSON_VALUE, produces = ALL_VALUE)
	@Operation(description = "Update errand.", responses = {
		@ApiResponse(responseCode = "204", description = "No content - Successful operation", useReturnTypeSchema = true)
	})
	ResponseEntity<Void> patchErrand(
		@Parameter(name = "municipalityId", description = "Municipality ID", example = "2281") @PathVariable(name = "municipalityId") @ValidMunicipalityId final String municipalityId,
		@Parameter(name = "namespace", description = "Namespace", example = "MY_NAMESPACE") @Pattern(regexp = NAMESPACE_REGEXP, message = NAMESPACE_VALIDATION_MESSAGE) @PathVariable final String namespace,
		@Parameter(name = "errandId", description = "Errand ID", example = "123") @PathVariable(name = "errandId") final Long errandId,
		@RequestBody @Valid final PatchErrand patchErrand) {

		final var updatedErrand = errandService.update(errandId, municipalityId, namespace, patchErrand);

		processService.updateProcess(updatedErrand);

		return noContent()
			.header(CONTENT_TYPE, ALL_VALUE)
			.build();
	}

	@Hidden // Should be a hidden operation in the API.
	@DeleteMapping(path = "/{namespace}/errands/{errandId}", produces = ALL_VALUE)
	@Operation(description = "Delete errand by ID.", hidden = true, responses = {
		@ApiResponse(responseCode = "204", description = "No content - Successful operation", useReturnTypeSchema = true)
	})
	ResponseEntity<Void> deleteErrandById(
		@Parameter(name = "municipalityId", description = "Municipality ID", example = "2281") @PathVariable(name = "municipalityId") @ValidMunicipalityId final String municipalityId,
		@Parameter(name = "namespace", description = "Namespace", example = "MY_NAMESPACE") @Pattern(regexp = NAMESPACE_REGEXP, message = NAMESPACE_VALIDATION_MESSAGE) @PathVariable final String namespace,
		@Parameter(name = "errandId", description = "Errand ID", example = "123") @PathVariable(name = "errandId") final Long errandId) {

		errandService.delete(errandId, municipalityId, namespace);
		return noContent()
			.header(CONTENT_TYPE, ALL_VALUE)
			.build();
	}

	@GetMapping(path = "/{namespace}/errands", produces = APPLICATION_JSON_VALUE)
	@Operation(description = "Get errands with or without query. The query is very flexible and allows you as a client to control a lot yourself.", responses = {
		@ApiResponse(responseCode = "200", description = "OK - Successful operation", useReturnTypeSchema = true)
	})
	ResponseEntity<Page<Errand>> getErrands(
		@Parameter(name = "municipalityId", description = "Municipality ID", example = "2281") @PathVariable(name = "municipalityId") @ValidMunicipalityId final String municipalityId,
		@Parameter(name = "namespace", description = "Namespace", example = "MY_NAMESPACE") @Pattern(regexp = NAMESPACE_REGEXP, message = NAMESPACE_VALIDATION_MESSAGE) @PathVariable final String namespace,
		@Parameter(
			description = "Syntax description: [spring-filter](https://github.com/turkraft/spring-filter/blob/85730f950a5f8623159cc0eb4d737555f9382bb7/README.md#syntax)",
			example = "caseType:'PARKING_PERMIT' and stakeholders.firstName~'*mar*' and applicationReceived>'2022-09-08T12:18:03.747+02:00'",
			schema = @Schema(implementation = String.class)) @Filter final Specification<ErrandEntity> filter,
		@ParameterObject final Pageable pageable) {

		return ok(errandService.findAll(filter, municipalityId, namespace, pageable));
	}

	@GetMapping(path = "/errands", produces = APPLICATION_JSON_VALUE)
	@Operation(description = "Get errands with or without query. The query is very flexible and allows you as a client to control a lot yourself.", hidden = true, responses = {
		@ApiResponse(responseCode = "200", description = "OK - Successful operation", useReturnTypeSchema = true)
	})
	ResponseEntity<Page<Errand>> getErrandsWithoutNamespace(
		@Parameter(name = "municipalityId", description = "Municipality ID", example = "2281") @PathVariable(name = "municipalityId") @ValidMunicipalityId final String municipalityId,
		@Parameter(
			description = "Syntax description: [spring-filter](https://github.com/turkraft/spring-filter/blob/85730f950a5f8623159cc0eb4d737555f9382bb7/README.md#syntax)",
			example = "caseType:'PARKING_PERMIT' and stakeholders.firstName~'*mar*' and applicationReceived>'2022-09-08T12:18:03.747+02:00'",
			schema = @Schema(implementation = String.class)) @Filter final Specification<ErrandEntity> filter,
		@ParameterObject final Pageable pageable) {

		return ok(errandService.findAllWithoutNamespace(filter, municipalityId, pageable));
	}
}
