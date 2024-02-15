package se.sundsvall.casedata.integration.db.model;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import se.sundsvall.casedata.integration.db.model.enums.AddressCategory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Builder(setterPrefix = "with")
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class Address {

	@Enumerated(EnumType.STRING)
	@Column(name = "address_category")
	private AddressCategory addressCategory;

	@Column(name = "street")
	private String street;

	@Column(name = "house_number")
	private String houseNumber;

	@Column(name = "postal_code")
	private String postalCode;

	@Column(name = "city")
	private String city;

	@Column(name = "country")
	private String country;

	@Column(name = "care_of")
	private String careOf;

	@Column(name = "attention")
	private String attention;

	@Column(name = "property_designation")
	private String propertyDesignation;

	@Column(name = "apartment_number")
	private String apartmentNumber;

	@Column(name = "is_zoning_plan_area")
	private Boolean isZoningPlanArea;

	@Column(name = "invoice_marking")
	private String invoiceMarking;

	@Embedded
	private Coordinates location;

	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (!(o instanceof final Address address)) return false;
		return addressCategory == address.addressCategory && Objects.equals(street, address.street) && Objects.equals(houseNumber, address.houseNumber) && Objects.equals(postalCode, address.postalCode) && Objects.equals(city, address.city) && Objects.equals(country, address.country) && Objects.equals(careOf, address.careOf) && Objects.equals(attention, address.attention) && Objects.equals(propertyDesignation, address.propertyDesignation) && Objects.equals(apartmentNumber, address.apartmentNumber) && Objects.equals(isZoningPlanArea, address.isZoningPlanArea) && Objects.equals(invoiceMarking, address.invoiceMarking) && Objects.equals(location, address.location);
	}

	@Override
	public int hashCode() {
		return Objects.hash(addressCategory, street, houseNumber, postalCode, city, country, careOf, attention, propertyDesignation, apartmentNumber, isZoningPlanArea, invoiceMarking, location);
	}

}
