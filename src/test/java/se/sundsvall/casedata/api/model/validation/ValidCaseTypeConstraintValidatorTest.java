package se.sundsvall.casedata.api.model.validation;

import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.ConstraintValidatorContext.ConstraintViolationBuilder;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import se.sundsvall.casedata.api.model.CaseType;
import se.sundsvall.casedata.api.model.validation.impl.ValidCaseTypeConstraintValidator;
import se.sundsvall.casedata.service.MetadataService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.web.context.request.RequestAttributes.SCOPE_REQUEST;
import static org.springframework.web.servlet.HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE;

@ExtendWith(MockitoExtension.class)
class ValidCaseTypeConstraintValidatorTest {

	@InjectMocks
	private ValidCaseTypeConstraintValidator validator;

	@Mock
	private MetadataService metadataService;

	@Mock
	private ConstraintValidatorContext constraintValidatorContextMock;

	@Mock
	private ConstraintViolationBuilder constraintViolationBuilderMock;

	@Mock
	private RequestAttributes requestAttributesMock;

	@ParameterizedTest
	@ValueSource(strings = {
		"PARATRANSIT", "TYPE_1"
	})
	void isValidWithValidCaseType(final String validCaseType) {
		final var namespace = "namespace";
		final var municipalityId = "municipalityId";
		final var attributes = Map.of("namespace", namespace, "municipalityId", municipalityId);

		try (final var requestContextHolderMock = mockStatic(RequestContextHolder.class)) {
			requestContextHolderMock.when(RequestContextHolder::getRequestAttributes).thenReturn(requestAttributesMock);
			when(requestAttributesMock.getAttribute(URI_TEMPLATE_VARIABLES_ATTRIBUTE, SCOPE_REQUEST)).thenReturn(attributes);
			when(metadataService.getCaseTypes(municipalityId, namespace)).thenReturn(List.of(
				CaseType.builder().withType("PARATRANSIT").withDisplayName("Färdtjänst").build(),
				CaseType.builder().withType("TYPE_1").withDisplayName("Type 1").build(),
				CaseType.builder().withType("TYPE_2").withDisplayName("Type 2").build()));

			assertThat(validator.isValid(validCaseType, constraintValidatorContextMock)).isTrue();
		}
	}

	@Test
	void isValidWithInvalidCaseType() {
		final var namespace = "namespace";
		final var municipalityId = "municipalityId";
		final var attributes = Map.of("namespace", namespace, "municipalityId", municipalityId);

		try (final var requestContextHolderMock = mockStatic(RequestContextHolder.class)) {
			requestContextHolderMock.when(RequestContextHolder::getRequestAttributes).thenReturn(requestAttributesMock);
			when(requestAttributesMock.getAttribute(URI_TEMPLATE_VARIABLES_ATTRIBUTE, SCOPE_REQUEST)).thenReturn(attributes);
			when(metadataService.getCaseTypes(municipalityId, namespace)).thenReturn(List.of(
				CaseType.builder().withType("PARATRANSIT").withDisplayName("Färdtjänst").build(),
				CaseType.builder().withType("TYPE_1").withDisplayName("Type 1").build(),
				CaseType.builder().withType("TYPE_2").withDisplayName("Type 2").build()));

			when(constraintValidatorContextMock.buildConstraintViolationWithTemplate(anyString())).thenReturn(constraintViolationBuilderMock);

			final var invalidCaseType = "INVALID_CATEGORY";
			assertThat(validator.isValid(invalidCaseType, constraintValidatorContextMock)).isFalse();
			verify(constraintValidatorContextMock).buildConstraintViolationWithTemplate(any());
			verify(constraintViolationBuilderMock).addConstraintViolation();
		}
	}

	@ParameterizedTest
	@ValueSource(booleans = {
		true, false
	})
	void isValidNullable(final Boolean nullable) {

		try (final var requestContextHolderMock = mockStatic(RequestContextHolder.class)) {
			requestContextHolderMock.when(RequestContextHolder::getRequestAttributes).thenReturn(requestAttributesMock);
			validator.setNullable(nullable);
			assertThat(validator.isValid(null, constraintValidatorContextMock)).isEqualTo(nullable);
		}
	}

	@Test
	void isValidWthEmptyCaseType() {
		final var namespace = "namespace";
		final var municipalityId = "municipalityId";
		final var attributes = Map.of("namespace", namespace, "municipalityId", municipalityId);

		try (final var requestContextHolderMock = mockStatic(RequestContextHolder.class)) {
			requestContextHolderMock.when(RequestContextHolder::getRequestAttributes).thenReturn(requestAttributesMock);
			when(requestAttributesMock.getAttribute(URI_TEMPLATE_VARIABLES_ATTRIBUTE, SCOPE_REQUEST)).thenReturn(attributes);
			when(metadataService.getCaseTypes(municipalityId, namespace)).thenReturn(List.of(
				CaseType.builder().withType("PARATRANSIT").withDisplayName("Färdtjänst").build()));

			when(constraintValidatorContextMock.buildConstraintViolationWithTemplate(anyString())).thenReturn(constraintViolationBuilderMock);

			final var emptyType = "";
			assertThat(validator.isValid(emptyType, constraintValidatorContextMock)).isFalse();
			verify(constraintValidatorContextMock).buildConstraintViolationWithTemplate(any());
			verify(constraintViolationBuilderMock).addConstraintViolation();
		}
	}

}
