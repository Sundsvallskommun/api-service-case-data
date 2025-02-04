package se.sundsvall.casedata.api.model.validation.enums;

import java.util.Arrays;

// This should be updated to be incorporated into namespace config like in support management
public enum Shortcode {

	MEX("SBK_MEX"),
	PRH("SBK_PARKING_PERMIT");

	final String namespace;

	Shortcode(final String namespace) {
		this.namespace = namespace;
	}

	public static Shortcode getByNamespace(final String namespace) {
		return Arrays.stream(Shortcode.values())
			.filter(shortcode -> shortcode.namespace.equals(namespace))
			.findFirst()
			.orElseThrow(() -> new IllegalArgumentException("No Shortcode found for namespace: " + namespace));
	}
}
