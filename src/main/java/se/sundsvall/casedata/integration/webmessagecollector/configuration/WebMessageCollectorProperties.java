package se.sundsvall.casedata.integration.webmessagecollector.configuration;

import java.util.List;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "integration.web-message-collector")
public record WebMessageCollectorProperties(int connectTimeout, int readTimeout, Map<String, Map<String, List<String>>> familyIds) {}
