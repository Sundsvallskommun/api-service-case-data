# CaseData

_Manages cases primarily related to citizen-related subjects. Currently handles cases for parking permits and cases
related to land and exploitation subjects._

## Getting Started

### Prerequisites

- **Java 21 or higher**
- **Maven**
- **MariaDB**
- **Git**
- **[Dependent Microservices](#dependencies)**

### Installation

1. **Clone the repository:**

   ```bash
   git clone git@github.com:Sundsvallskommun/api-service-case-status.git
   cd api-service-case-status
   ```
2. **Configure the application:**

   Before running the application, you need to set up configuration settings.
   See [Configuration](#Configuration)

   **Note:** Ensure all required configurations are set; otherwise, the application may fail to start.

3. **Ensure dependent services are running:**

   If this microservice depends on other services, make sure they are up and accessible.
   See [Dependencies](#dependencies) for more details.

4. **Build and run the application:**

   ```bash
   mvn spring-boot:run
   ```

## Dependencies

This microservice depends on the following services:

- **EmailReader**
  - **Purpose:** Reads e-mails sent to mailboxes and provides them for processing by CaseData and other systems.
  - **Repository:** [Link to the repository](https://github.com/Sundsvallskommun/api-service-email-reader)
  - **Setup Instructions:** Refer to its documentation for installation and configuration steps.
- **Employee**
  - **Purpose:** Provides information about an employee and its employment.
  - **Repository:** Not available at this moment.
  - **Additional Notes:** Citizen is a API serving data
    from [Metadatakatalogen](https://utveckling.sundsvall.se/digital-infrastruktur/metakatalogen).
- **Land and Exploitation**
  - **Purpose:** Serves as a facade for the Land and Exploitation camunda process
  - **Repository:** [Link to the repository](https://github.com/Sundsvallskommun/pw-land-and-exploitation)
  - **Setup Instructions:** Refer to its documentation for installation and configuration steps.
- **Messaging**
  - **Purpose:** Used to send communications to stakeholders via E-mail or SMS
  - **Repository:** [Link to the repository](https://github.com/Sundsvallskommun/api-service-messaging)
  - **Setup Instructions:** Refer to its documentation for installation and configuration steps.
- **Message-Exchange**
  - **Purpose:** Used to send local messages between microservices.
  - **Repository:** [Link to the repository](https://github.com/Sundsvallskommun/api-service-message-exchange)
  - **Setup Instructions:** Refer to its documentation for installation and configuration steps.
- **Parking Permit**
  - **Purpose:** Serves as a facade for the ParkingPermit camunda process
  - **Repository:** [Link to the repository](https://github.com/Sundsvallskommun/pw-parking-permit)
  - **Setup Instructions:** Refer to its documentation for installation and configuration steps.
- **WebMessageCollector**
  - **Purpose:** Collects messages from the Open-E platform and stores them in a database for subsequent retrieval by
    other systems.
  - **Repository:** [Link to the repository](https://github.com/Sundsvallskommun/api-service-web-message-collector)
  - **Setup Instructions:** Refer to its documentation for installation and configuration steps.

Ensure that these services are running and properly configured before starting this microservice.

## API Documentation

Access the API documentation via Swagger UI:

- **Swagger UI:** [http://localhost:8080/api-docs](http://localhost:8080/api-docs)

Alternatively, refer to the `openapi.yml` file located in `src/main/resources/api` for the OpenAPI specification.

## Usage

### API Endpoints

Refer to the [API Documentation](#api-documentation) for detailed information on available endpoints.

### Example Request

```bash
curl -X GET http://localhost:8080/api/2281/mynamespace/errands
```

## Configuration

Configuration is crucial for the application to run successfully. Ensure all necessary settings are configured in
`application.yml`.

### Key Configuration Parameters

- **Server Port:**

  ```yaml
  server:
    port: 8080
  ```
- **Database Settings:**

  ```yaml
  spring:
    datasource:
      url: jdbc:mysql://localhost:3306/your_database
      username: your_db_username
      password: your_db_password
  ```
- **External service configuration:**

  ```yaml
    integration:
    web-message-collector:
        family-ids:
            123: 
                external:  
                - "123"
                - "456"
    config:
    landandexploitation:
        base-url: https://your_service_url
        token-url: your-token.url
        client-id: your-client-id
        client-secret: your-client-secret
    parkingpermit:
        base-url: https://your_service_url
        token-url: your-token.url
        client-id: your-client-id
        client-secret: your-client-secret
    web-message-collector:
        base-url: https://your_service_url
        token-url: your-token.url
        client-id: your-client-id
        client-secret: your-client-secret
    emailreader:
        base-url: https://your_service_url
        token-url: your-token.url
        client-id: your-client-id
        client-secret: your-client-secret
        municipality-id: 2281
        namespace: caseData
    employee:
        base-url: https://your_service_url
        token-url: your-token.url
        client-id: your-client-id
        client-secret: your-client-secret

    scheduler:
        message-collector:
            fixedRate: PT1M
            initialDelay: PT1M
        emailreader:
            fixedRate: PT1M
            initialDelay: PT1M


  ```

### Database Initialization

The project is set up with [Flyway](https://github.com/flyway/flyway) for database migrations. Flyway is disabled by
default so you will have to enable it to automatically populate the database schema upon application startup.

```yaml
spring:
  flyway:
    enabled: true
```

- **No additional setup is required** for database initialization, as long as the database connection settings are
  correctly configured.

### Additional Notes

- **Application Profiles:**

  Use Spring profiles (`dev`, `prod`, etc.) to manage different configurations for different environments.

- **Logging Configuration:**

  Adjust logging levels if necessary.

## Contributing

Contributions are welcome! Please
see [CONTRIBUTING.md](https://github.com/Sundsvallskommun/.github/blob/main/.github/CONTRIBUTING.md) for guidelines.

## License

This project is licensed under the [MIT License](LICENSE).

## Code status

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_api-service-case-data&metric=alert_status)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_api-service-case-data)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_api-service-case-data&metric=reliability_rating)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_api-service-case-data)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_api-service-case-data&metric=security_rating)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_api-service-case-data)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_api-service-case-data&metric=sqale_rating)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_api-service-case-data)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_api-service-case-data&metric=vulnerabilities)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_api-service-case-data)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_api-service-case-data&metric=bugs)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_api-service-case-data)

---

© 2024 Sundsvalls kommun
