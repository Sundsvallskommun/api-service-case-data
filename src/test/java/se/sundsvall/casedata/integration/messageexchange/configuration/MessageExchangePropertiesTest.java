package se.sundsvall.casedata.integration.messageexchange.configuration;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import se.sundsvall.casedata.Application;

@SpringBootTest(classes = Application.class)
@ActiveProfiles("junit")
class MessageExchangePropertiesTest {

	@Autowired
	private MessageExchangeProperties properties;

	@Test
	void testProperties() {
		assertThat(properties.connectTimeout()).isEqualTo(5);
		assertThat(properties.readTimeout()).isEqualTo(20);
	}

}
