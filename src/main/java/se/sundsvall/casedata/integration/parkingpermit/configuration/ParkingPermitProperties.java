package se.sundsvall.casedata.integration.parkingpermit.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("integration.parkingpermit")
public record ParkingPermitProperties(int connectTimeout, int readTimeout) {
}
