package se.sundsvall.casedata.integration.messageexchange.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("integration.message-exchange")
public record MessageExchangeProperties(int connectTimeout, int readTimeout) {
}
