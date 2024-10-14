package se.sundsvall.casedata.service;

import org.springframework.stereotype.Service;

import generated.se.sundsvall.employee.PortalPersonData;
import se.sundsvall.casedata.integration.employee.EmployeeClient;

@Service
public class EmployeeService {

	private static final String DOMAIN_PERSONAL = "personal";

	private final EmployeeClient employeeClient;

	public EmployeeService(final EmployeeClient employeeClient) {
		this.employeeClient = employeeClient;
	}

	public PortalPersonData getEmployeeByLoginName(final String loginName) {
		return employeeClient.getEmployeeByDomainAndLoginName(DOMAIN_PERSONAL, loginName).orElse(null);
	}
}
