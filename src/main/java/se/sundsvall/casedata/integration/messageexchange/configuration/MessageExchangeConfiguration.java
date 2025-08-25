package se.sundsvall.casedata.integration.messageexchange.configuration;

import static java.util.Optional.ofNullable;

import feign.codec.Encoder;
import feign.form.FormEncoder;
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
	public Encoder feignFormEncoder(ObjectProvider<HttpMessageConverters> messageConverters) {

		// 1. This is the delegate encoder. It's an encoder that uses Spring's
		// HttpMessageConverters, allowing it to handle various data types like JSON.
		// This encoder handles everything that isn't a form.
		Encoder delegate = new SpringEncoder(messageConverters);

		// 2. This encoder wraps the delegate to specifically handle Spring's
		// MultipartFile. It is implemented to stream the file content.
		SpringFormEncoder springFormEncoder = new SpringFormEncoder(delegate);

		// 3. This is the top-level encoder. It's the one that knows how to process
		// form data (both form-urlencoded and multipart).
		return new FormEncoder(springFormEncoder);
	}

	@Bean
	FeignBuilderCustomizer feignBuilderCustomizer(final ClientRegistrationRepository clientRepository, final MessageExchangeProperties messageExchangeProperties, ObjectProvider<HttpMessageConverters> messageConverters) {
		return FeignMultiCustomizer.create()
			.withErrorDecoder(new ProblemErrorDecoder(CLIENT_ID))
			.withEncoder(new FormEncoder(new SpringFormEncoder(new SpringEncoder(messageConverters))))
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
