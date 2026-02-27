package se.sundsvall.casedata.configuration;

import java.util.ArrayList;
import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SwaggerBeanConfig implements WebMvcConfigurer {

	@Override
	public void extendMessageConverters(final List<HttpMessageConverter<?>> converters) {
		converters.stream()
			.filter(JacksonJsonHttpMessageConverter.class::isInstance)
			.map(JacksonJsonHttpMessageConverter.class::cast)
			.findFirst()
			.ifPresent(converter -> {
				final var supportedMediaTypes = new ArrayList<>(converter.getSupportedMediaTypes());
				supportedMediaTypes.add(new MediaType("application", "octet-stream"));
				converter.setSupportedMediaTypes(supportedMediaTypes);
			});
	}
}
