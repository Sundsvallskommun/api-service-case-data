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

@Getter
@Setter
@Builder(setterPrefix = "with")
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class AddressEntity {

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
	private CoordinatesEntity location;

	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		final AddressEntity that = (AddressEntity) o;
		return addressCategory == that.addressCategory && Objects.equals(street, that.street) && Objects.equals(houseNumber, that.houseNumber) && Objects.equals(postalCode, that.postalCode) && Objects.equals(city, that.city) && Objects.equals(country, that.country) && Objects.equals(careOf, that.careOf) && Objects.equals(attention, that.attention) && Objects.equals(propertyDesignation, that.propertyDesignation) && Objects.equals(apartmentNumber, that.apartmentNumber) && Objects.equals(isZoningPlanArea, that.isZoningPlanArea) && Objects.equals(invoiceMarking, that.invoiceMarking) && Objects.equals(location, that.location);
	}

	@Override
	public int hashCode() {
		return Objects.hash(addressCategory, street, houseNumber, postalCode, city, country, careOf, attention, propertyDesignation, apartmentNumber, isZoningPlanArea, invoiceMarking, location);
	}

	@Override
	public String toString() {
		return "AddressEntity{" +
			"addressCategory=" + addressCategory +
			", street='" + street + '\'' +
			", houseNumber='" + houseNumber + '\'' +
			", postalCode='" + postalCode + '\'' +
			", city='" + city + '\'' +
			", country='" + country + '\'' +
			", careOf='" + careOf + '\'' +
			", attention='" + attention + '\'' +
			", propertyDesignation='" + propertyDesignation + '\'' +
			", apartmentNumber='" + apartmentNumber + '\'' +
			", isZoningPlanArea=" + isZoningPlanArea +
			", invoiceMarking='" + invoiceMarking + '\'' +
			", location=" + location +
			'}';
	}

}
