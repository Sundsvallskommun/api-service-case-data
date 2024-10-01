package se.sundsvall.casedata.integration.db.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

import com.fasterxml.jackson.annotation.JsonBackReference;

import se.sundsvall.casedata.integration.db.listeners.StakeholderListener;
import se.sundsvall.casedata.integration.db.model.enums.StakeholderType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "stakeholder",
	indexes = {
		@Index(name = "idx_stakeholder_municipality_id", columnList = "municipality_id"),
		@Index(name = "idx_stakeholder_namespace", columnList = "namespace")
	})
@EntityListeners(StakeholderListener.class)
@Getter
@Setter
@SuperBuilder(setterPrefix = "with")
@NoArgsConstructor
@AllArgsConstructor
public class Stakeholder extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "errand_id", foreignKey = @ForeignKey(name = "FK_stakeholder_errand_id"))
	@JsonBackReference
	private Errand errand;

	@Enumerated(EnumType.STRING)
	@Column(name = "type")
	private StakeholderType type;

	@Column(name = "municipality_id")
	private String municipalityId;

	@Column(name = "namespace")
	private String namespace;

	@Column(name = "first_name")
	private String firstName;

	@Column(name = "last_name")
	private String lastName;

	@Column(name = "person_id")
	private String personId;

	@Column(name = "organization_name")
	private String organizationName;

	@Column(name = "organization_number")
	private String organizationNumber;

	@Column(name = "authorized_signatory")
	private String authorizedSignatory;

	@Column(name = "ad_account")
	private String adAccount;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "stakeholder_roles",
		joinColumns = @JoinColumn(name = "stakeholder_id", foreignKey = @ForeignKey(name = "FK_stakeholder_roles_stakeholder_id")))
	@OrderColumn(name = "role_order")
	@Builder.Default
	private List<String> roles = new ArrayList<>();

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "stakeholder_addresses",
		joinColumns = @JoinColumn(name = "stakeholder_id", foreignKey = @ForeignKey(name = "FK_stakeholder_addresses_stakeholder_id")))
	@OrderColumn(name = "address_order")
	@Builder.Default
	private List<Address> addresses = new ArrayList<>();

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "stakeholder_contact_information",
		joinColumns = @JoinColumn(name = "stakeholder_id", foreignKey = @ForeignKey(name = "FK_stakeholder_contact_information_stakeholder_id")))
	@OrderColumn(name = "contact_information_order")
	@Builder.Default
	private List<ContactInformation> contactInformation = new ArrayList<>();

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "stakeholder_extra_parameters",
		joinColumns = @JoinColumn(name = "stakeholder_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "FK_stakeholder_extra_parameters_stakeholder_id")))
	@MapKeyColumn(name = "extra_parameter_key")
	@Column(name = "extra_parameter_value", length = 8192)
	@Builder.Default
	private Map<String, String> extraParameters = new HashMap<>();

	@Override
	public String toString() {
		final long errandId = errand == null ? 0 : errand.getId();
		return "Stakeholder{" +
			"errand.id=" + errandId +
			", type=" + type +
			", municipalityId='" + municipalityId + '\'' +
			", namespace='" + namespace + '\'' +
			", firstName='" + firstName + '\'' +
			", lastName='" + lastName + '\'' +
			", personId='" + personId + '\'' +
			", organizationName='" + organizationName + '\'' +
			", organizationNumber='" + organizationNumber + '\'' +
			", authorizedSignatory='" + authorizedSignatory + '\'' +
			", adAccount='" + adAccount + '\'' +
			", roles=" + roles +
			", addresses=" + addresses +
			", contactInformation=" + contactInformation +
			", extraParameters=" + extraParameters +
			"} " + super.toString();
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;
		final Stakeholder that = (Stakeholder) o;
		return Objects.equals(errand, that.errand) && type == that.type && Objects.equals(municipalityId, that.municipalityId) && Objects.equals(namespace, that.namespace) && Objects.equals(firstName, that.firstName) && Objects.equals(lastName, that.lastName) && Objects.equals(personId, that.personId) && Objects.equals(organizationName, that.organizationName) && Objects.equals(organizationNumber, that.organizationNumber) && Objects.equals(authorizedSignatory, that.authorizedSignatory) && Objects.equals(adAccount, that.adAccount) && Objects.equals(roles, that.roles) && Objects.equals(addresses, that.addresses) && Objects.equals(contactInformation, that.contactInformation) && Objects.equals(extraParameters, that.extraParameters);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), errand, type, municipalityId, namespace, firstName, lastName, personId, organizationName, organizationNumber, authorizedSignatory, adAccount, roles, addresses, contactInformation, extraParameters);
	}

}
