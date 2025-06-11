package se.sundsvall.casedata.integration.messageexchange.configuration;

import org.springframework.cloud.openfeign.FeignBuilderCustomizer;
import org.springframework.cloud.openfeign.support.JsonFormWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import se.sundsvall.dept44.configuration.feign.FeignConfiguration;
import se.sundsvall.dept44.configuration.feign.FeignMultiCustomizer;
import se.sundsvall.dept44.configuration.feign.decoder.ProblemErrorDecoder;

@Import(FeignConfiguration.class)
public class MessageExchangeConfiguration {

	public static final String CLIENT_ID = "message-exchange";

	@Bean
	JsonFormWriter jsonFormWriter() {
		// Needed for Feign to handle json objects sent as requestpart correctly
		return new JsonFormWriter();
	}

	@Bean
	FeignBuilderCustomizer feignBuilderCustomizer(final ClientRegistrationRepository clientRepository, final MessageExchangeProperties messageExchangeProperties) {
		return FeignMultiCustomizer.create()
			.withErrorDecoder(new ProblemErrorDecoder(CLIENT_ID))
			.withRequestTimeoutsInSeconds(messageExchangeProperties.connectTimeout(), messageExchangeProperties.readTimeout())
			.withRetryableOAuth2InterceptorForClientRegistration(clientRepository.findByRegistrationId(CLIENT_ID))
			.composeCustomizersToOne();
	}

}
