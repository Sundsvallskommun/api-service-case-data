package se.sundsvall.casedata.integration.db.model;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Random;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import se.sundsvall.casedata.integration.db.model.enums.Channel;
import se.sundsvall.casedata.integration.db.model.enums.Priority;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static com.google.code.beanmatchers.BeanMatchers.registerValueGenerator;
import static java.time.OffsetDateTime.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;

class ErrandEntityTest {

	private static final long ID = 1L;
	private static final String ERRAND_NUMBER = "errandNumber";
	private static final String MUNICIPALITY_ID = "municipalityId";
	private static final String NAMESPACE = "namespace";
	private static final String EXTERNAL_CASE_ID = "externalCaseId";
	private static final String CASE_TYPE = "caseType";
	private static final Channel CHANNEL = Channel.EMAIL;
	private static final Priority PRIORITY = Priority.HIGH;
	private static final String DESCRIPTION = "description";
	private static final String CASE_TITLE_ADDITION = "caseTitleAddition";
	private static final String DIARY_NUMBER = "diaryNumber";
	private static final String PHASE = "phase";
	private static final List<StatusEntity> STATUSES = List.of(new StatusEntity());
	private static final LocalDate START_DATE = LocalDate.now();
	private static final LocalDate END_DATE = LocalDate.now();
	private static final OffsetDateTime APPLICATION_RECEIVED = now();
	private static final String PROCESS_ID = "processId";
	private static final List<StakeholderEntity> STAKEHOLDERS = List.of(new StakeholderEntity());
	private static final List<FacilityEntity> FACILITIES = List.of(new FacilityEntity());
	private static final List<DecisionEntity> DECISIONS = List.of(new DecisionEntity());
	private static final List<NoteEntity> NOTES = List.of(new NoteEntity());
	private static final List<NotificationEntity> NOTIFICATIONS = List.of(new NotificationEntity());
	private static final String CREATED_BY_CLIENT = "createdByClient";
	private static final String UPDATED_BY_CLIENT = "updatedByClient";
	private static final String CREATED_BY = "createdBy";
	private static final String UPDATED_BY = "updatedBy";
	private static final List<ExtraParameterEntity> EXTRA_PARAMETERS = List.of(new ExtraParameterEntity());
	private static final OffsetDateTime CREATED = now();
	private static final OffsetDateTime UPDATED = now();
	private static final OffsetDateTime SUSPENSION_FROM = now();
	private static final OffsetDateTime SUSPENSION_TO = now();
	private static final List<RelatedErrandEntity> RELATES_TO = List.of(new RelatedErrandEntity());
	private static final List<String> LABELS = List.of("label");
	private static final StatusEntity STATUS = new StatusEntity();

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

		// Act
		final var bean = ErrandEntity.builder()
			.withId(ID)
			.withErrandNumber(ERRAND_NUMBER)
			.withMunicipalityId(MUNICIPALITY_ID)
			.withNamespace(NAMESPACE)
			.withExternalCaseId(EXTERNAL_CASE_ID)
			.withCaseType(CASE_TYPE)
			.withChannel(CHANNEL)
			.withPriority(PRIORITY)
			.withDescription(DESCRIPTION)
			.withCaseTitleAddition(CASE_TITLE_ADDITION)
			.withDiaryNumber(DIARY_NUMBER)
			.withPhase(PHASE)
			.withStatuses(STATUSES)
			.withStartDate(START_DATE)
			.withEndDate(END_DATE)
			.withApplicationReceived(APPLICATION_RECEIVED)
			.withProcessId(PROCESS_ID)
			.withStakeholders(STAKEHOLDERS)
			.withFacilities(FACILITIES)
			.withDecisions(DECISIONS)
			.withNotes(NOTES)
			.withNotifications(NOTIFICATIONS)
			.withCreatedByClient(CREATED_BY_CLIENT)
			.withUpdatedByClient(UPDATED_BY_CLIENT)
			.withCreatedBy(CREATED_BY)
			.withUpdatedBy(UPDATED_BY)
			.withExtraParameters(EXTRA_PARAMETERS)
			.withCreated(CREATED)
			.withUpdated(UPDATED)
			.withSuspendedFrom(SUSPENSION_FROM)
			.withSuspendedTo(SUSPENSION_TO)
			.withRelatesTo(RELATES_TO)
			.withLabels(LABELS)
			.withStatus(STATUS)
			.build();

		// Assert
		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertBasicFields(bean);
		assertDates(bean);
		assertCollections(bean);
		assertClients(bean);
	}

	private void assertBasicFields(final ErrandEntity bean) {
		assertThat(bean.getId()).isEqualTo(ID);
		assertThat(bean.getErrandNumber()).isEqualTo(ERRAND_NUMBER);
		assertThat(bean.getMunicipalityId()).isEqualTo(MUNICIPALITY_ID);
		assertThat(bean.getNamespace()).isEqualTo(NAMESPACE);
		assertThat(bean.getExternalCaseId()).isEqualTo(EXTERNAL_CASE_ID);
		assertThat(bean.getCaseType()).isEqualTo(CASE_TYPE);
		assertThat(bean.getChannel()).isEqualTo(CHANNEL);
		assertThat(bean.getPriority()).isEqualTo(PRIORITY);
		assertThat(bean.getDescription()).isEqualTo(DESCRIPTION);
		assertThat(bean.getCaseTitleAddition()).isEqualTo(CASE_TITLE_ADDITION);
		assertThat(bean.getDiaryNumber()).isEqualTo(DIARY_NUMBER);
		assertThat(bean.getPhase()).isEqualTo(PHASE);
		assertThat(bean.getStatus()).isEqualTo(STATUS);
		assertThat(bean.getProcessId()).isEqualTo(PROCESS_ID);
	}

	private void assertDates(final ErrandEntity bean) {
		assertThat(bean.getStartDate()).isEqualTo(START_DATE);
		assertThat(bean.getEndDate()).isEqualTo(END_DATE);
		assertThat(bean.getApplicationReceived()).isEqualTo(APPLICATION_RECEIVED);
		assertThat(bean.getCreated()).isEqualTo(CREATED);
		assertThat(bean.getUpdated()).isEqualTo(UPDATED);
		assertThat(bean.getSuspendedFrom()).isEqualTo(SUSPENSION_FROM);
		assertThat(bean.getSuspendedTo()).isEqualTo(SUSPENSION_TO);
	}

	private void assertCollections(final ErrandEntity bean) {

		assertThat(bean.getStatuses()).isEqualTo(STATUSES);
		assertThat(bean.getStakeholders()).isEqualTo(STAKEHOLDERS);
		assertThat(bean.getFacilities()).isEqualTo(FACILITIES);
		assertThat(bean.getDecisions()).isEqualTo(DECISIONS);
		assertThat(bean.getNotes()).isEqualTo(NOTES);
		assertThat(bean.getNotifications()).isEqualTo(NOTIFICATIONS);
		assertThat(bean.getExtraParameters()).isEqualTo(EXTRA_PARAMETERS);
		assertThat(bean.getRelatesTo()).isEqualTo(RELATES_TO);
		assertThat(bean.getLabels()).isEqualTo(LABELS);
	}

	private void assertClients(final ErrandEntity bean) {
		assertThat(bean.getCreatedByClient()).isEqualTo(CREATED_BY_CLIENT);
		assertThat(bean.getUpdatedByClient()).isEqualTo(UPDATED_BY_CLIENT);
		assertThat(bean.getCreatedBy()).isEqualTo(CREATED_BY);
		assertThat(bean.getUpdatedBy()).isEqualTo(UPDATED_BY);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(ErrandEntity.builder().build()).hasAllNullFieldsOrPropertiesExcept("version")
			.satisfies(bean -> assertThat(bean.getVersion()).isZero());
		assertThat(new ErrandEntity()).hasAllNullFieldsOrPropertiesExcept("version")
			.satisfies(bean -> assertThat(bean.getVersion()).isZero());
	}
}
