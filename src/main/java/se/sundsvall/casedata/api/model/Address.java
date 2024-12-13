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

	@Schema(description = "Category of the address", example = "RESIDENTIAL")
	private AddressCategory addressCategory;

	@Schema(description = "Street name", example = "Testvägen")
	@Size(max = 255)
	private String street;

	@Schema(description = "House number", example = "18")
	@Size(max = 255)
	private String houseNumber;

	@Schema(description = "Postal code", example = "123 45")
	@Size(max = 255)
	private String postalCode;

	@Schema(description = "City name", example = "Sundsvall")
	@Size(max = 255)
	private String city;

	@Schema(description = "Country name", example = "Sverige")
	@Size(max = 255)
	private String country;

	@Schema(description = "Care of (c/o)", example = "Test Testorsson")
	@Size(max = 255)
	private String careOf;

	@Schema(description = "Attention to", example = "Test Testorsson")
	@Size(max = 255)
	private String attention;

	@Schema(description = "Property designation", example = "SUNDSVALL LJUSTA 7:2")
	@Size(max = 255)
	private String propertyDesignation;

	@Schema(description = "Apartment number", example = "LGH 1001")
	@Size(max = 255)
	private String apartmentNumber;

	@Schema(description = "Indicates if the address is within a zoning plan area", example = "true")
	private Boolean isZoningPlanArea;

	@Schema(description = "Invoice marking, only in combination with addressCategory: INVOICE_ADDRESS", example = "1234567890")
	@Size(max = 255)
	private String invoiceMarking;

	@Schema(description = "The location of the address", example = "{\"latitude\": 62.3908, \"longitude\": 17.3069}")
	private Coordinates location;

}
