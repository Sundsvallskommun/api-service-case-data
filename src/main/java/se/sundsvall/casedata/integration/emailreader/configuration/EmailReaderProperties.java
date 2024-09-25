package se.sundsvall.casedata.integration.emailreader.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "integration.email-reader")
public record EmailReaderProperties(int connectTimeout, int readTimeout, String namespace, String municipalityId) {}
