package se.sundsvall.casedata.service.util;

import static se.sundsvall.dept44.support.Identifier.Type.AD_ACCOUNT;

import java.util.Optional;
import se.sundsvall.dept44.support.Identifier;

public final class ServiceUtil {

	private ServiceUtil() {}

	public static String getAdUser() {
		return Optional.ofNullable(Identifier.get())
			.filter(identifier -> AD_ACCOUNT.equals(identifier.getType()))
			.map(Identifier::getValue)
			.orElse("UNKNOWN");
	}
}
