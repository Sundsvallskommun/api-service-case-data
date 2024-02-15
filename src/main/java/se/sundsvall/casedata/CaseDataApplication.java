package se.sundsvall.casedata;

import org.springframework.boot.SpringApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

import se.sundsvall.dept44.ServiceApplication;

@ServiceApplication
@EnableFeignClients
@EnableScheduling
public class CaseDataApplication {

	public static void main(final String[] args) {
		SpringApplication.run(CaseDataApplication.class, args);
	}

}
