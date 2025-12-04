package se.sundsvall.casedata.integration.employee;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static se.sundsvall.casedata.integration.employee.configuration.EmployeeConfiguration.CLIENT_ID;

import generated.se.sundsvall.employee.PortalPersonData;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.util.Optional;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import se.sundsvall.casedata.integration.employee.configuration.EmployeeConfiguration;

@FeignClient(
	name = CLIENT_ID,
	url = "${integration.employee.base-url}",
	configuration = EmployeeConfiguration.class,
	dismiss404 = true)
@CircuitBreaker(name = CLIENT_ID)
public interface EmployeeClient {

	/**
	 * Get Userdata from the employee service by domain and loginName.
	 *
	 * @param  municipalityId the municipalityId.
	 * @param  domain         domain of the employee.
	 * @param  loginName      login name of the employee.
	 * @return                PortalPersonData with information about the employee
	 */
	@GetMapping(path = "/{municipalityId}/portalpersondata/{domain}/{loginName}", produces = APPLICATION_JSON_VALUE)
	Optional<PortalPersonData> getEmployeeByDomainAndLoginName(
		@PathVariable final String municipalityId,
		@PathVariable final String domain,
		@PathVariable final String loginName);
}
