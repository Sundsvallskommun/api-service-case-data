package se.sundsvall.casedata.api.model.validation.impl;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.Setter;
import org.springframework.web.context.request.RequestContextHolder;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import se.sundsvall.casedata.api.model.CaseType;
import se.sundsvall.casedata.api.model.validation.ValidCaseType;
import se.sundsvall.casedata.service.MetadataService;

import static java.util.Collections.emptyList;
import static org.springframework.web.context.request.RequestAttributes.SCOPE_REQUEST;
import static org.springframework.web.servlet.HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE;

@Setter
public class ValidCaseTypeConstraintValidator implements ConstraintValidator<ValidCaseType, String> {

	private static final String PATH_VARIABLE_NAMESPACE = "namespace";
	private static final String PATH_VARIABLE_MUNICIPALITY_ID = "municipalityId";
	private final MetadataService metadataService;
	private boolean nullable;

	public ValidCaseTypeConstraintValidator(final MetadataService metadataService) {
		this.metadataService = metadataService;
	}

	@Override
	public void initialize(final ValidCaseType constraintAnnotation) {
		this.nullable = constraintAnnotation.nullable();
		ConstraintValidator.super.initialize(constraintAnnotation);
	}

	@Override
	public boolean isValid(final String value, final ConstraintValidatorContext context) {

		if (value == null) {
			return nullable;
		}

		final var namespace = getPathVariable(PATH_VARIABLE_NAMESPACE);
		final var municipalityId = getPathVariable(PATH_VARIABLE_MUNICIPALITY_ID);
		final var casetypes = getCasetypes(municipalityId, namespace);

		if (value.isEmpty()) {
			context.disableDefaultConstraintViolation();
			context.buildConstraintViolationWithTemplate("Case type cannot be blank. Valid types are: " + casetypes)
				.addConstraintViolation();
			return false;
		}

		final boolean isValid = casetypes.stream().anyMatch(caseType -> caseType.equals(value));
		if (!isValid) {
			context.disableDefaultConstraintViolation();
			context.buildConstraintViolationWithTemplate("Invalid case type. Valid types are: " + casetypes)
				.addConstraintViolation();
		}
		return isValid;
	}

	public List<String> getCasetypes(final String municipalityId, final String namespace) {
		return Optional.ofNullable(metadataService.getCaseTypes(municipalityId, namespace))
			.orElse(emptyList()).stream()
			.map(CaseType::getType)
			.toList();
	}

	String getPathVariable(final String variableName) {
		return Stream.ofNullable(RequestContextHolder.getRequestAttributes())
			.map(req -> req.getAttribute(URI_TEMPLATE_VARIABLES_ATTRIBUTE, SCOPE_REQUEST))
			.filter(Objects::nonNull)
			.filter(Map.class::isInstance)
			.map(Map.class::cast)
			.map(map -> map.get(variableName))
			.filter(Objects::nonNull)
			.filter(String.class::isInstance)
			.map(String.class::cast)
			.findAny()
			.orElseThrow(() -> Problem.valueOf(Status.INTERNAL_SERVER_ERROR, String.format("Path variable '%s' is not readable from request", variableName)));
	}

}
