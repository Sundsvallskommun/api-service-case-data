package se.sundsvall.casedata.integration.employee;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;
import static se.sundsvall.casedata.integration.employee.configuration.EmployeeConfiguration.CLIENT_ID;

import java.util.Optional;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import generated.se.sundsvall.employee.PortalPersonData;
import se.sundsvall.casedata.integration.employee.configuration.EmployeeConfiguration;

@FeignClient(
	name = CLIENT_ID,
	url = "${integration.employee.base-url}",
	configuration = EmployeeConfiguration.class,
	dismiss404 = true)
public interface EmployeeClient {

	/**
	 * Get Userdata from the employee service by domain and loginName.
	 *
	 * @param  domain    domain of the employee
	 * @param  loginName login name of the employee
	 * @return           PortalPersonData with information about the employee
	 */
	@GetMapping(path = "/portalpersondata/{domain}/{loginName}", produces = {
		APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE
	})
	Optional<PortalPersonData> getEmployeeByDomainAndLoginName(@PathVariable("domain") String domain, @PathVariable("loginName") String loginName);
}
