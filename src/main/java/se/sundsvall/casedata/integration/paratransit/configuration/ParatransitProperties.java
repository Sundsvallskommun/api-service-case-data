package se.sundsvall.casedata.integration.paratransit.configuration;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("integration.paratransit")
public record ParatransitProperties(int connectTimeout, int readTimeout, List<String> supportedNamespaces) {
}
