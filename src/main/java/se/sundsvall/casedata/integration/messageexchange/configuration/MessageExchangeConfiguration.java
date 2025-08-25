package se.sundsvall.casedata.integration.messageexchange.configuration;

import static java.util.Optional.ofNullable;

import feign.form.spring.SpringFormEncoder;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.FeignBuilderCustomizer;
import org.springframework.cloud.openfeign.support.JsonFormWriter;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import se.sundsvall.dept44.configuration.feign.FeignConfiguration;
import se.sundsvall.dept44.configuration.feign.FeignMultiCustomizer;
import se.sundsvall.dept44.configuration.feign.decoder.ProblemErrorDecoder;
import se.sundsvall.dept44.support.Identifier;

@Import(FeignConfiguration.class)
public class MessageExchangeConfiguration {

	public static final String CLIENT_ID = "message-exchange";

	@Bean
	JsonFormWriter jsonFormWriter() {
		// Needed for Feign to handle json objects sent as requestpart correctly
		return new JsonFormWriter();
	}

	@Bean
	FeignBuilderCustomizer feignBuilderCustomizer(final ClientRegistrationRepository clientRepository, final MessageExchangeProperties messageExchangeProperties, ObjectProvider<HttpMessageConverters> messageConverters) {
		return FeignMultiCustomizer.create()
			.withErrorDecoder(new ProblemErrorDecoder(CLIENT_ID))
			.withEncoder(new SpringFormEncoder(new SpringEncoder(messageConverters)))
			.withRequestInterceptor(builder -> builder.header(Identifier.HEADER_NAME, createSentByHeaderValue(Identifier.get())))
			.withRequestTimeoutsInSeconds(messageExchangeProperties.connectTimeout(), messageExchangeProperties.readTimeout())
			.withRetryableOAuth2InterceptorForClientRegistration(clientRepository.findByRegistrationId(CLIENT_ID))
			.composeCustomizersToOne();
	}

	String createSentByHeaderValue(final Identifier identifier) {
		return ofNullable(identifier)
			.map(Identifier::toHeaderValue)
			.orElse(null);
	}
}
