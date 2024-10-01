package se.sundsvall.casedata.integration.db.model;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static com.google.code.beanmatchers.BeanMatchers.registerValueGenerator;
import static java.time.OffsetDateTime.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import se.sundsvall.casedata.integration.db.model.enums.Channel;
import se.sundsvall.casedata.integration.db.model.enums.Priority;

class ErrandTest {

	@BeforeAll
	static void setup() {
		registerValueGenerator(() -> now().plusDays(new Random().nextInt()), OffsetDateTime.class);
		registerValueGenerator(() -> LocalDate.now().plusDays(new Random().nextInt()), LocalDate.class);
	}

	@Test
	void testBean() {
		MatcherAssert.assertThat(Errand.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void builder() {
		// Arrange

		var id = 1L;
		var errandNumber = "errandNumber";
		var municipalityId = "municipalityId";
		var namespace = "namespace";
		var externalCaseId = "externalCaseId";
		var caseType = "caseType";
		var channel = Channel.EMAIL;
		var priority = Priority.HIGH;
		var description = "description";
		var caseTitleAddition = "caseTitleAddition";
		var diaryNumber = "diaryNumber";
		var phase = "phase";
		var statuses = List.of(new Status());
		var startDate = LocalDate.now();
		var endDate = LocalDate.now();
		var applicationReceived = now();
		var processId = "processId";
		var stakeholders = List.of(new Stakeholder());
		var facilities = List.of(new Facility());
		var decisions = List.of(new Decision());
		var appeals = List.of(new Appeal());
		var notes = List.of(new Note());
		var createdByClient = "createdByClient";
		var updatedByClient = "updatedByClient";
		var createdBy = "createdBy";
		var updatedBy = "updatedBy";
		var extraParameters = Map.of("key", "value");
		var created = now();
		var updated = now();

		// Act
		var bean = Errand.builder()
			.withId(id)
			.withErrandNumber(errandNumber)
			.withMunicipalityId(municipalityId)
			.withNamespace(namespace)
			.withExternalCaseId(externalCaseId)
			.withCaseType(caseType)
			.withChannel(channel)
			.withPriority(priority)
			.withDescription(description)
			.withCaseTitleAddition(caseTitleAddition)
			.withDiaryNumber(diaryNumber)
			.withPhase(phase)
			.withStatuses(statuses)
			.withStartDate(startDate)
			.withEndDate(endDate)
			.withApplicationReceived(applicationReceived)
			.withProcessId(processId)
			.withStakeholders(stakeholders)
			.withFacilities(facilities)
			.withDecisions(decisions)
			.withAppeals(appeals)
			.withNotes(notes)
			.withCreatedByClient(createdByClient)
			.withUpdatedByClient(updatedByClient)
			.withCreatedBy(createdBy)
			.withUpdatedBy(updatedBy)
			.withExtraParameters(extraParameters)
			.withCreated(created)
			.withUpdated(updated)
			.build();

		// Assert
		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertBasicFields(bean, id, errandNumber, municipalityId, namespace, externalCaseId, caseType, channel, priority, description, caseTitleAddition, diaryNumber, phase);
		assertDates(bean, startDate, endDate, applicationReceived, created, updated);
		assertCollections(bean, statuses, stakeholders, facilities, decisions, appeals, notes, extraParameters);
		assertClients(bean, createdByClient, updatedByClient, createdBy, updatedBy);
	}

	private void assertBasicFields(Errand bean, Long id, String errandNumber, String municipalityId, String namespace, String externalCaseId, String caseType, Channel channel, Priority priority, String description, String caseTitleAddition, String diaryNumber, String phase) {
		assertThat(bean.getId()).isEqualTo(id);
		assertThat(bean.getErrandNumber()).isEqualTo(errandNumber);
		assertThat(bean.getMunicipalityId()).isEqualTo(municipalityId);
		assertThat(bean.getNamespace()).isEqualTo(namespace);
		assertThat(bean.getExternalCaseId()).isEqualTo(externalCaseId);
		assertThat(bean.getCaseType()).isEqualTo(caseType);
		assertThat(bean.getChannel()).isEqualTo(channel);
		assertThat(bean.getPriority()).isEqualTo(priority);
		assertThat(bean.getDescription()).isEqualTo(description);
		assertThat(bean.getCaseTitleAddition()).isEqualTo(caseTitleAddition);
		assertThat(bean.getDiaryNumber()).isEqualTo(diaryNumber);
		assertThat(bean.getPhase()).isEqualTo(phase);
	}

	private void assertDates(Errand bean, LocalDate startDate, LocalDate endDate, OffsetDateTime applicationReceived, OffsetDateTime created, OffsetDateTime updated) {
		assertThat(bean.getStartDate()).isEqualTo(startDate);
		assertThat(bean.getEndDate()).isEqualTo(endDate);
		assertThat(bean.getApplicationReceived()).isEqualTo(applicationReceived);
		assertThat(bean.getCreated()).isEqualTo(created);
		assertThat(bean.getUpdated()).isEqualTo(updated);
	}

	private void assertCollections(Errand bean, List<Status> statuses, List<Stakeholder> stakeholders, List<Facility> facilities, List<Decision> decisions, List<Appeal> appeals, List<Note> notes, Map<String, String> extraParameters) {
		assertThat(bean.getStatuses()).isEqualTo(statuses);
		assertThat(bean.getStakeholders()).isEqualTo(stakeholders);
		assertThat(bean.getFacilities()).isEqualTo(facilities);
		assertThat(bean.getDecisions()).isEqualTo(decisions);
		assertThat(bean.getAppeals()).isEqualTo(appeals);
		assertThat(bean.getNotes()).isEqualTo(notes);
		assertThat(bean.getExtraParameters()).isEqualTo(extraParameters);
	}

	private void assertClients(Errand bean, String createdByClient, String updatedByClient, String createdBy, String updatedBy) {
		assertThat(bean.getCreatedByClient()).isEqualTo(createdByClient);
		assertThat(bean.getUpdatedByClient()).isEqualTo(updatedByClient);
		assertThat(bean.getCreatedBy()).isEqualTo(createdBy);
		assertThat(bean.getUpdatedBy()).isEqualTo(updatedBy);
	}


	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(Errand.builder().build()).hasAllNullFieldsOrPropertiesExcept("statuses", "stakeholders", "facilities", "decisions", "appeals", "notes", "extraParameters", "version")
			.satisfies(bean -> {
					assertThat(bean.getStatuses()).isEmpty();
					assertThat(bean.getStakeholders()).isEmpty();
					assertThat(bean.getFacilities()).isEmpty();
					assertThat(bean.getDecisions()).isEmpty();
					assertThat(bean.getAppeals()).isEmpty();
					assertThat(bean.getNotes()).isEmpty();
					assertThat(bean.getExtraParameters()).isEmpty();
					assertThat(bean.getVersion()).isZero();
				}
			);
		assertThat(new Errand()).hasAllNullFieldsOrPropertiesExcept("statuses", "stakeholders", "facilities", "decisions", "appeals", "notes", "extraParameters", "version")
			.satisfies(bean -> {
					assertThat(bean.getStatuses()).isEmpty();
					assertThat(bean.getStakeholders()).isEmpty();
					assertThat(bean.getFacilities()).isEmpty();
					assertThat(bean.getDecisions()).isEmpty();
					assertThat(bean.getAppeals()).isEmpty();
					assertThat(bean.getNotes()).isEmpty();
					assertThat(bean.getExtraParameters()).isEmpty();
					assertThat(bean.getVersion()).isZero();
				}
			);
	}


}
