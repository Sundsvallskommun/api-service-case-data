package se.sundsvall.casedata.api.model;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import se.sundsvall.casedata.integration.db.model.enums.Channel;
import se.sundsvall.casedata.integration.db.model.enums.Priority;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Random;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static com.google.code.beanmatchers.BeanMatchers.registerValueGenerator;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;

class ErrandTest {

	@BeforeAll
	static void setup() {
		registerValueGenerator(() -> OffsetDateTime.now().plusDays(new Random().nextInt()), OffsetDateTime.class);
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
	void builderTest() {

		// Arrange
		final var id = 1L;
		final var version = 1;
		final var errandNumber = "PRH-2022-000001";
		final var externalCaseId = "caa230c6-abb4-4592-ad9a-34e263c2787b";
		final var caseType = "BUILDING_PERMIT";
		final var channel = Channel.EMAIL;
		final var priority = Priority.HIGH;
		final var description = "Some description of the case.";
		final var caseTitleAddition = "Eldstad/rökkanal, Skylt";
		final var diaryNumber = "DIA123456";
		final var phase = "Aktualisering";
		final var statuses = List.of(new Status());
		final var startDate = LocalDate.of(2022, 1, 1);
		final var endDate = LocalDate.of(2022, 6, 1);
		final var applicationReceived = OffsetDateTime.parse("2023-10-01T12:00:00Z");
		final var processId = "c3cb9123-4ed2-11ed-ac7c-0242ac110003";
		final var stakeholders = List.of(new Stakeholder());
		final var facilities = List.of(new Facility());
		final var decisions = List.of(new Decision());
		final var appeals = List.of(new Appeal());
		final var notes = List.of(new Note());
		final var messageIds = List.of("messageId1", "messageId2");
		final var createdByClient = "client1";
		final var updatedByClient = "client2";
		final var createdBy = "user1";
		final var updatedBy = "user2";
		final var extraParameters = List.of(ExtraParameter.builder().withKey("key1").withValues(List.of("value1")).build());
		final var created = OffsetDateTime.parse("2023-10-01T12:00:00Z");
		final var updated = OffsetDateTime.parse("2023-10-02T12:00:00Z");
		final var suspension = new Suspension();
		final var municipalityId = "municipalityId";
		final var namespace = "namespace";

		// Act
		final var bean = Errand.builder()
			.withId(id)
			.withVersion(version)
			.withErrandNumber(errandNumber)
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
			.withMessageIds(messageIds)
			.withCreatedByClient(createdByClient)
			.withUpdatedByClient(updatedByClient)
			.withCreatedBy(createdBy)
			.withUpdatedBy(updatedBy)
			.withExtraParameters(extraParameters)
			.withCreated(created)
			.withUpdated(updated)
			.withSuspension(suspension)
			.withMunicipalityId(municipalityId)
			.withNamespace(namespace)
			.build();

		// Assert
		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertBasicFields(bean, id, errandNumber, externalCaseId, caseType, channel, priority, description, caseTitleAddition, diaryNumber, phase, suspension, municipalityId, namespace);
		assertDates(bean, startDate, endDate, applicationReceived, created, updated);
		assertCollections(bean, statuses, stakeholders, facilities, decisions, appeals, notes, extraParameters);
		assertClients(bean, createdByClient, updatedByClient, createdBy, updatedBy);
	}

	private void assertBasicFields(Errand bean, Long id, String errandNumber, String externalCaseId, String caseType, Channel channel, Priority priority, String description, String caseTitleAddition, String diaryNumber, String phase,
		Suspension suspension, String municipalityId, String namespace) {
		assertThat(bean.getId()).isEqualTo(id);
		assertThat(bean.getErrandNumber()).isEqualTo(errandNumber);
		assertThat(bean.getExternalCaseId()).isEqualTo(externalCaseId);
		assertThat(bean.getCaseType()).isEqualTo(caseType);
		assertThat(bean.getChannel()).isEqualTo(channel);
		assertThat(bean.getPriority()).isEqualTo(priority);
		assertThat(bean.getDescription()).isEqualTo(description);
		assertThat(bean.getCaseTitleAddition()).isEqualTo(caseTitleAddition);
		assertThat(bean.getDiaryNumber()).isEqualTo(diaryNumber);
		assertThat(bean.getPhase()).isEqualTo(phase);
		assertThat(bean.getSuspension()).isEqualTo(suspension);
		assertThat(bean.getMunicipalityId()).isEqualTo(municipalityId);
		assertThat(bean.getNamespace()).isEqualTo(namespace);
	}

	private void assertDates(Errand bean, LocalDate startDate, LocalDate endDate, OffsetDateTime applicationReceived, OffsetDateTime created, OffsetDateTime updated) {
		assertThat(bean.getStartDate()).isEqualTo(startDate);
		assertThat(bean.getEndDate()).isEqualTo(endDate);
		assertThat(bean.getApplicationReceived()).isEqualTo(applicationReceived);
		assertThat(bean.getCreated()).isEqualTo(created);
		assertThat(bean.getUpdated()).isEqualTo(updated);
	}

	private void assertCollections(Errand bean, List<Status> statuses, List<Stakeholder> stakeholders, List<Facility> facilities, List<Decision> decisions, List<Appeal> appeals, List<Note> notes, List<ExtraParameter> extraParameters) {
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
	void testNoDirtOnEmptyBean() {
		assertThat(Errand.builder().build()).hasAllNullFieldsOrPropertiesExcept("id", "priority", "version")
			.satisfies(bean -> {
				assertThat(bean.getId()).isZero();
				assertThat(bean.getPriority()).isEqualTo(Priority.MEDIUM);
				assertThat(bean.getVersion()).isZero();
			});
		assertThat(new Errand()).hasAllNullFieldsOrPropertiesExcept("id", "priority", "version")
			.satisfies(bean -> {
				assertThat(bean.getId()).isZero();
				assertThat(bean.getPriority()).isEqualTo(Priority.MEDIUM);
				assertThat(bean.getVersion()).isZero();
			});
	}

}
