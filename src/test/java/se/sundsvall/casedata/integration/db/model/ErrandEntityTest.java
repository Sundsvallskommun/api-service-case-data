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
import java.util.Random;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import se.sundsvall.casedata.integration.db.model.enums.Channel;
import se.sundsvall.casedata.integration.db.model.enums.Priority;

class ErrandEntityTest {

	@BeforeAll
	static void setup() {
		registerValueGenerator(() -> now().plusDays(new Random().nextInt()), OffsetDateTime.class);
		registerValueGenerator(() -> LocalDate.now().plusDays(new Random().nextInt()), LocalDate.class);
	}

	@Test
	void testBean() {
		MatcherAssert.assertThat(ErrandEntity.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void builder() {
		// Arrange

		final var id = 1L;
		final var errandNumber = "errandNumber";
		final var municipalityId = "municipalityId";
		final var namespace = "namespace";
		final var externalCaseId = "externalCaseId";
		final var caseType = "caseType";
		final var channel = Channel.EMAIL;
		final var priority = Priority.HIGH;
		final var description = "description";
		final var caseTitleAddition = "caseTitleAddition";
		final var diaryNumber = "diaryNumber";
		final var phase = "phase";
		final var statuses = List.of(new StatusEntity());
		final var startDate = LocalDate.now();
		final var endDate = LocalDate.now();
		final var applicationReceived = now();
		final var processId = "processId";
		final var stakeholders = List.of(new StakeholderEntity());
		final var facilities = List.of(new FacilityEntity());
		final var decisions = List.of(new DecisionEntity());
		final var notes = List.of(new NoteEntity());
		final var notifications = List.of(new NotificationEntity());
		final var createdByClient = "createdByClient";
		final var updatedByClient = "updatedByClient";
		final var createdBy = "createdBy";
		final var updatedBy = "updatedBy";
		final var extraParameters = List.of(new ExtraParameterEntity());
		final var created = now();
		final var updated = now();
		final var suspensionFrom = now();
		final var suspensionTo = now();
		final var relatesTo = List.of(new RelatedErrandEntity());
		final var labels = List.of("label");

		// Act
		final var bean = ErrandEntity.builder()
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
			.withNotes(notes)
			.withNotifications(notifications)
			.withCreatedByClient(createdByClient)
			.withUpdatedByClient(updatedByClient)
			.withCreatedBy(createdBy)
			.withUpdatedBy(updatedBy)
			.withExtraParameters(extraParameters)
			.withCreated(created)
			.withUpdated(updated)
			.withSuspendedFrom(suspensionFrom)
			.withSuspendedTo(suspensionTo)
			.withRelatesTo(relatesTo)
			.withLabels(labels)
			.build();

		// Assert
		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertBasicFields(bean, id, errandNumber, municipalityId, namespace, externalCaseId, caseType, channel, priority, description, caseTitleAddition, diaryNumber, phase);
		assertDates(bean, startDate, endDate, applicationReceived, created, updated, suspensionFrom, suspensionTo);
		assertCollections(bean, statuses, stakeholders, facilities, decisions, notes, notifications, extraParameters, relatesTo, labels);
		assertClients(bean, createdByClient, updatedByClient, createdBy, updatedBy);
	}

	private void assertBasicFields(final ErrandEntity bean, final Long id, final String errandNumber, final String municipalityId, final String namespace, final String externalCaseId, final String caseType, final Channel channel, final Priority priority,
		final String description, final String caseTitleAddition,
		final String diaryNumber, final String phase) {
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

	private void assertDates(final ErrandEntity bean, final LocalDate startDate, final LocalDate endDate, final OffsetDateTime applicationReceived, final OffsetDateTime created, final OffsetDateTime updated, final OffsetDateTime suspensionFrom,
		final OffsetDateTime suspensionTo) {
		assertThat(bean.getStartDate()).isEqualTo(startDate);
		assertThat(bean.getEndDate()).isEqualTo(endDate);
		assertThat(bean.getApplicationReceived()).isEqualTo(applicationReceived);
		assertThat(bean.getCreated()).isEqualTo(created);
		assertThat(bean.getUpdated()).isEqualTo(updated);
		assertThat(bean.getSuspendedFrom()).isEqualTo(suspensionFrom);
		assertThat(bean.getSuspendedTo()).isEqualTo(suspensionTo);
	}

	private void assertCollections(final ErrandEntity bean, final List<StatusEntity> statuses, final List<StakeholderEntity> stakeholders, final List<FacilityEntity> facilities, final List<DecisionEntity> decisions, final List<NoteEntity> notes,
		final List<NotificationEntity> notifications, final List<ExtraParameterEntity> extraParameters, final List<RelatedErrandEntity> relatesTo, final List<String> labels) {

		assertThat(bean.getStatuses()).isEqualTo(statuses);
		assertThat(bean.getStakeholders()).isEqualTo(stakeholders);
		assertThat(bean.getFacilities()).isEqualTo(facilities);
		assertThat(bean.getDecisions()).isEqualTo(decisions);
		assertThat(bean.getNotes()).isEqualTo(notes);
		assertThat(bean.getNotifications()).isEqualTo(notifications);
		assertThat(bean.getExtraParameters()).isEqualTo(extraParameters);
		assertThat(bean.getRelatesTo()).isEqualTo(relatesTo);
		assertThat(bean.getLabels()).isEqualTo(labels);
	}

	private void assertClients(final ErrandEntity bean, final String createdByClient, final String updatedByClient, final String createdBy, final String updatedBy) {
		assertThat(bean.getCreatedByClient()).isEqualTo(createdByClient);
		assertThat(bean.getUpdatedByClient()).isEqualTo(updatedByClient);
		assertThat(bean.getCreatedBy()).isEqualTo(createdBy);
		assertThat(bean.getUpdatedBy()).isEqualTo(updatedBy);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(ErrandEntity.builder().build()).hasAllNullFieldsOrPropertiesExcept("version")
			.satisfies(bean -> {
				assertThat(bean.getVersion()).isZero();
			});
		assertThat(new ErrandEntity()).hasAllNullFieldsOrPropertiesExcept("version")
			.satisfies(bean -> {
				assertThat(bean.getVersion()).isZero();
			});
	}

}
