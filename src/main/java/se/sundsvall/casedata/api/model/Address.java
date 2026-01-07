package se.sundsvall.casedata.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import se.sundsvall.casedata.integration.db.model.enums.AddressCategory;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(setterPrefix = "with")
public class Address {

	@Schema(description = "Category of the address", examples = "RESIDENTIAL")
	private AddressCategory addressCategory;

	@Schema(description = "Street name", examples = "Testvägen", maxLength = 255)
	@Size(max = 255)
	private String street;

	@Schema(description = "House number", examples = "18", maxLength = 255)
	@Size(max = 255)
	private String houseNumber;

	@Schema(description = "Postal code", examples = "123 45", maxLength = 255)
	@Size(max = 255)
	private String postalCode;

	@Schema(description = "City name", examples = "Sundsvall", maxLength = 255)
	@Size(max = 255)
	private String city;

	@Schema(description = "Country name", examples = "Sverige", maxLength = 255)
	@Size(max = 255)
	private String country;

	@Schema(description = "Care of (c/o)", examples = "Test Testorsson", maxLength = 255)
	@Size(max = 255)
	private String careOf;

	@Schema(description = "Attention to", examples = "Test Testorsson", maxLength = 255)
	@Size(max = 255)
	private String attention;

	@Schema(description = "Property designation", examples = "SUNDSVALL LJUSTA 7:2", maxLength = 255)
	@Size(max = 255)
	private String propertyDesignation;

	@Schema(description = "Apartment number", examples = "LGH 1001", maxLength = 255)
	@Size(max = 255)
	private String apartmentNumber;

	@Schema(description = "Indicates if the address is within a zoning plan area", examples = "true")
	private Boolean isZoningPlanArea;

	@Schema(description = "Invoice marking, only in combination with addressCategory: INVOICE_ADDRESS", examples = "1234567890", maxLength = 255)
	@Size(max = 255)
	private String invoiceMarking;

	@Schema(description = "The location of the address", examples = "{\"latitude\": 62.3908, \"longitude\": 17.3069}")
	private Coordinates location;
}
