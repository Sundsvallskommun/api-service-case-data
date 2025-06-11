package se.sundsvall.casedata.integration.messagingsettings.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("integration.messaging-settings")
public record MessagingSettingsProperties(int connectTimeout, int readTimeout) {
}
