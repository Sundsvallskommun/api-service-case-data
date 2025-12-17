package se.sundsvall.casedata.service;

import static java.util.Collections.emptyList;
import static org.zalando.problem.Status.NOT_FOUND;
import static se.sundsvall.casedata.integration.db.model.enums.NotificationSubType.NOTE;
import static se.sundsvall.casedata.service.NotificationService.EventType.CREATE;
import static se.sundsvall.casedata.service.NotificationService.EventType.UPDATE;
import static se.sundsvall.casedata.service.util.Constants.ERRAND_ENTITY_NOT_FOUND;
import static se.sundsvall.casedata.service.util.Constants.NOTE_WITH_ID_X_WAS_NOT_FOUND_ON_ERRAND_WITH_ID_X;
import static se.sundsvall.casedata.service.util.Constants.NOTIFICATION_NOTE_CREATED;
import static se.sundsvall.casedata.service.util.Constants.NOTIFICATION_NOTE_UPDATED;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toNote;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toNoteEntity;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toOwnerId;
import static se.sundsvall.casedata.service.util.mappers.PatchMapper.patchNote;

import java.util.List;
import java.util.Optional;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zalando.problem.Problem;
import se.sundsvall.casedata.api.model.Note;
import se.sundsvall.casedata.api.model.Notification;
import se.sundsvall.casedata.integration.db.ErrandRepository;
import se.sundsvall.casedata.integration.db.NoteRepository;
import se.sundsvall.casedata.integration.db.model.ErrandEntity;
import se.sundsvall.casedata.integration.db.model.enums.NoteType;
import se.sundsvall.casedata.service.util.mappers.EntityMapper;

@Service
@Transactional
public class NoteService {

	private final NoteRepository noteRepository;
	private final ErrandRepository errandRepository;
	private final ApplicationEventPublisher applicationEventPublisher;
	private final NotificationService notificationService;

	public NoteService(final NoteRepository noteRepository,
		final ErrandRepository errandRepository,
		final ApplicationEventPublisher applicationEventPublisher,
		final NotificationService notificationService) {

		this.noteRepository = noteRepository;
		this.errandRepository = errandRepository;
		this.applicationEventPublisher = applicationEventPublisher;
		this.notificationService = notificationService;
	}

	public void update(final Long errandId, final Long noteId, final String municipalityId, final String namespace, final Note updatedNote) {
		final var errandEntity = findErrandEntity(errandId, municipalityId, namespace, true);

		final var noteEntity = Optional.ofNullable(errandEntity.getNotes())
			.orElse(emptyList())
			.stream()
			.filter(note -> note.getId().equals(noteId))
			.findAny()
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, NOTE_WITH_ID_X_WAS_NOT_FOUND_ON_ERRAND_WITH_ID_X.formatted(noteId, errandId)));

		patchNote(noteEntity, updatedNote);
		noteRepository.saveAndFlush(noteEntity);
		applicationEventPublisher.publishEvent(noteEntity.getErrand());

		// Create notification
		notificationService.create(municipalityId, namespace, Notification.builder()
			.withCreatedBy(errandEntity.getCreatedBy())
			.withDescription(NOTIFICATION_NOTE_UPDATED)
			.withErrandId(errandEntity.getId())
			.withType(UPDATE.toString())
			.withSubType(NOTE.toString())
			.withOwnerId(toOwnerId(errandEntity))
			.build(), errandEntity);
	}

	public Note findNote(final Long errandId, final Long noteId, final String municipalityId, final String namespace) {
		final var errandEntity = findErrandEntity(errandId, municipalityId, namespace, false);

		final var noteEntity = errandEntity.getNotes().stream()
			.filter(note -> note.getId().equals(noteId))
			.findAny()
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, NOTE_WITH_ID_X_WAS_NOT_FOUND_ON_ERRAND_WITH_ID_X.formatted(noteId, errandId)));

		return toNote(noteEntity);
	}

	public List<Note> findNotes(final Long errandId, final String municipalityId, final String namespace, final Optional<NoteType> noteType) {
		final var errand = findErrandEntity(errandId, municipalityId, namespace, false);

		return noteType.map(type -> errand.getNotes().stream()
			.filter(note -> note.getNoteType() == type)
			.map(EntityMapper::toNote)
			.toList()).orElseGet(() -> errand.getNotes().stream()
				.map(EntityMapper::toNote)
				.toList());
	}

	public void delete(final Long errandId, final String municipalityId, final String namespace, final Long noteId) {
		final var errand = findErrandEntity(errandId, municipalityId, namespace, true);
		final var noteToRemove = errand.getNotes().stream()
			.filter(note -> note.getId().equals(noteId))
			.findAny()
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, NOTE_WITH_ID_X_WAS_NOT_FOUND_ON_ERRAND_WITH_ID_X.formatted(noteId, errandId)));
		errand.getNotes().remove(noteToRemove);
		errandRepository.saveAndFlush(errand);
		applicationEventPublisher.publishEvent(errand);
	}

	public Note addNote(final Long errandId, final String municipalityId, final String namespace, final Note note) {
		final var oldErrand = findErrandEntity(errandId, municipalityId, namespace, true);
		final var noteEntity = toNoteEntity(note, municipalityId, namespace);
		noteEntity.setErrand(oldErrand);
		oldErrand.getNotes().add(noteEntity);
		final var updatedErrand = errandRepository.saveAndFlush(oldErrand);
		applicationEventPublisher.publishEvent(updatedErrand);

		// Create notification
		notificationService.create(municipalityId, namespace, Notification.builder()
			.withCreatedBy(oldErrand.getCreatedBy())
			.withDescription(NOTIFICATION_NOTE_CREATED)
			.withErrandId(oldErrand.getId())
			.withType(CREATE.toString())
			.withSubType(NOTE.toString())
			.withOwnerId(toOwnerId(oldErrand))
			.build(), updatedErrand);

		return toNote(noteEntity);
	}

	private ErrandEntity findErrandEntity(final Long errandId, final String municipalityId, final String namespace, boolean locking) {
		if (locking) {
			return errandRepository.findWithPessimisticLockingByIdAndMunicipalityIdAndNamespace(errandId, municipalityId, namespace)
				.orElseThrow(() -> Problem.valueOf(NOT_FOUND, ERRAND_ENTITY_NOT_FOUND.formatted(errandId, namespace, municipalityId)));
		} else {
			return errandRepository.findByIdAndMunicipalityIdAndNamespace(errandId, municipalityId, namespace)
				.orElseThrow(() -> Problem.valueOf(NOT_FOUND, ERRAND_ENTITY_NOT_FOUND.formatted(errandId, namespace, municipalityId)));
		}
	}
}
