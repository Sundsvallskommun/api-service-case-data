package se.sundsvall.casedata.service;

import static java.util.Collections.emptyList;
import static org.zalando.problem.Status.NOT_FOUND;
import static se.sundsvall.casedata.service.NotificationService.EventType.CREATE;
import static se.sundsvall.casedata.service.NotificationService.EventType.UPDATE;
import static se.sundsvall.casedata.service.util.Constants.ERRAND_WAS_NOT_FOUND;
import static se.sundsvall.casedata.service.util.Constants.NOTIFICATION_NOTE_CREATED;
import static se.sundsvall.casedata.service.util.Constants.NOTIFICATION_NOTE_UPDATED;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toNote;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toNoteEntity;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toOwnerId;
import static se.sundsvall.casedata.service.util.mappers.PatchMapper.patchNote;
import static se.sundsvall.casedata.service.util.mappers.PutMapper.putNote;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;

import io.github.resilience4j.retry.annotation.Retry;
import se.sundsvall.casedata.api.model.Note;
import se.sundsvall.casedata.api.model.Notification;
import se.sundsvall.casedata.integration.db.ErrandRepository;
import se.sundsvall.casedata.integration.db.NoteRepository;
import se.sundsvall.casedata.integration.db.model.ErrandEntity;
import se.sundsvall.casedata.integration.db.model.enums.NoteType;
import se.sundsvall.casedata.service.util.mappers.EntityMapper;

@Service
public class NoteService {

	private static final String NOTE_WAS_NOT_FOUND = "Note was not found";
	private static final String NOTE_WITH_ID_X_WAS_NOT_FOUND_ON_ERRAND_WITH_ID_X = "Note with id: %s was not found on errand with id: %s";

	private final NoteRepository noteRepository;
	private final ErrandRepository errandRepository;
	private final ProcessService processService;
	private final NotificationService notificationService;

	public NoteService(final NoteRepository noteRepository,
		final ErrandRepository errandRepository,
		final ProcessService processService,
		final NotificationService notificationService) {

		this.noteRepository = noteRepository;
		this.errandRepository = errandRepository;
		this.processService = processService;
		this.notificationService = notificationService;
	}

	@Retry(name = "OptimisticLocking")
	public void updateNoteOnErrand(final Long errandId, final Long noteId, final String municipalityId, final String namespace, final Note updatedNote) {
		final var errandEntity = getErrandByIdAndMunicipalityIdAndNamespace(errandId, municipalityId, namespace);

		final var noteEntity = Optional.ofNullable(errandEntity.getNotes())
			.orElse(emptyList())
			.stream()
			.filter(note -> note.getId().equals(noteId))
			.findAny()
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, NOTE_WITH_ID_X_WAS_NOT_FOUND_ON_ERRAND_WITH_ID_X.formatted(noteId, errandId)));

		patchNote(noteEntity, updatedNote);
		noteRepository.save(noteEntity);
		processService.updateProcess(noteEntity.getErrand());

		// Create notification
		notificationService.createNotification(municipalityId, namespace, Notification.builder()
			.withCreatedBy(errandEntity.getCreatedBy())
			.withDescription(NOTIFICATION_NOTE_UPDATED)
			.withErrandId(errandEntity.getId())
			.withType(UPDATE.toString())
			.withOwnerId(toOwnerId(errandEntity))
			.build());
	}

	@Retry(name = "OptimisticLocking")
	public void replaceNoteOnErrand(final Long noteId, final String municipalityId, final String namespace, final Note note) {
		final var noteEntity = noteRepository.findByIdAndMunicipalityIdAndNamespace(noteId, municipalityId, namespace).orElseThrow(() -> Problem.valueOf(NOT_FOUND, NOTE_WAS_NOT_FOUND));
		putNote(noteEntity, note);
		noteRepository.save(noteEntity);
		processService.updateProcess(noteEntity.getErrand());

		// Create notification
		notificationService.createNotification(municipalityId, namespace, Notification.builder()
			.withCreatedBy(noteEntity.getErrand().getCreatedBy())
			.withDescription(NOTIFICATION_NOTE_UPDATED)
			.withErrandId(noteEntity.getErrand().getId())
			.withType(UPDATE.toString())
			.withOwnerId(toOwnerId(noteEntity.getErrand()))
			.build());
	}

	public Note getNoteOnErrand(final Long errandId, final Long noteId, final String municipalityId, final String namespace) {
		final var errandEntity = getErrandByIdAndMunicipalityIdAndNamespace(errandId, municipalityId, namespace);

		final var noteEntity = errandEntity.getNotes().stream()
			.filter(note -> note.getId().equals(noteId))
			.findAny()
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, NOTE_WITH_ID_X_WAS_NOT_FOUND_ON_ERRAND_WITH_ID_X.formatted(noteId, errandId)));

		return toNote(noteEntity);
	}

	public List<Note> getAllNotesOnErrand(final Long errandId, final String municipalityId, final String namespace, final Optional<NoteType> noteType) {
		final var errand = errandRepository.findByIdAndMunicipalityIdAndNamespace(errandId, municipalityId, namespace).orElseThrow(() -> Problem.valueOf(NOT_FOUND, ERRAND_WAS_NOT_FOUND.formatted(errandId)));

		return noteType.map(type -> errand.getNotes().stream()
			.filter(note -> note.getNoteType() == type)
			.map(EntityMapper::toNote)
			.toList()).orElseGet(() -> errand.getNotes().stream()
				.map(EntityMapper::toNote)
				.toList());
	}

	@Retry(name = "OptimisticLocking")
	public void deleteNoteOnErrand(final Long errandId, final String municipalityId, final String namespace, final Long noteId) {
		final var errand = getErrandByIdAndMunicipalityIdAndNamespace(errandId, municipalityId, namespace);
		final var noteToRemove = errand.getNotes().stream()
			.filter(note -> note.getId().equals(noteId))
			.findAny()
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, NOTE_WITH_ID_X_WAS_NOT_FOUND_ON_ERRAND_WITH_ID_X.formatted(noteId, errandId)));
		errand.getNotes().remove(noteToRemove);
		errandRepository.save(errand);
		processService.updateProcess(errand);
	}

	@Retry(name = "OptimisticLocking")
	public Note addNoteToErrand(final Long errandId, final String municipalityId, final String namespace, final Note note) {
		final var oldErrand = getErrandByIdAndMunicipalityIdAndNamespace(errandId, municipalityId, namespace);
		final var noteEntity = toNoteEntity(note, municipalityId, namespace);
		noteEntity.setErrand(oldErrand);
		oldErrand.getNotes().add(noteEntity);
		final var updatedErrand = errandRepository.save(oldErrand);
		processService.updateProcess(updatedErrand);

		// Create notification
		notificationService.createNotification(municipalityId, namespace, Notification.builder()
			.withCreatedBy(oldErrand.getCreatedBy())
			.withDescription(NOTIFICATION_NOTE_CREATED)
			.withErrandId(oldErrand.getId())
			.withType(CREATE.toString())
			.withOwnerId(toOwnerId(oldErrand))
			.build());

		return toNote(noteEntity);
	}

	public ErrandEntity getErrandByIdAndMunicipalityIdAndNamespace(final Long errandId, final String municipalityId, final String namespace) {
		return errandRepository.findByIdAndMunicipalityIdAndNamespace(errandId, municipalityId, namespace)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, ERRAND_WAS_NOT_FOUND.formatted(errandId)));
	}
}
