package se.sundsvall.casedata.integration.db.config;

import static se.sundsvall.casedata.service.util.ServiceUtil.getAdUser;

import org.javers.spring.auditable.AuthorProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JaversConfiguration {

	@Bean
	AuthorProvider provideJaversAuthor() {
		return new SimpleAuthorProvider();
	}

	private record SimpleAuthorProvider()
		implements
		AuthorProvider {

		@Override
		public String provide() {
			return getAdUser();
		}
	}
}
