package se.sundsvall.casedata.service;

import static java.text.MessageFormat.format;
import static org.zalando.problem.Status.FORBIDDEN;
import static org.zalando.problem.Status.NOT_FOUND;
import static se.sundsvall.casedata.service.util.Constants.ERRAND_WAS_NOT_FOUND;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toNote;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toNoteDto;
import static se.sundsvall.casedata.service.util.mappers.PatchMapper.patchNote;
import static se.sundsvall.casedata.service.util.mappers.PutMapper.putNote;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;

import se.sundsvall.casedata.api.model.NoteDTO;
import se.sundsvall.casedata.integration.db.ErrandRepository;
import se.sundsvall.casedata.integration.db.NoteRepository;
import se.sundsvall.casedata.integration.db.model.Errand;
import se.sundsvall.casedata.integration.db.model.enums.NoteType;
import se.sundsvall.casedata.service.util.mappers.EntityMapper;

import io.github.resilience4j.retry.annotation.Retry;

@Service
public class NoteService {


	private static final String NOTE_WAS_NOT_FOUND = "Note was not found";

	private static final String PUBLIC_NOTE_CANNOT_BE_DELETED = "Public notes can not be deleted";

	private static final String NOTE_WITH_ID_X_WAS_NOT_FOUND_ON_ERRAND_WITH_ID_X = "Note with id: {0} was not found on errand with id: {1}";


	private final NoteRepository noteRepository;

	private final ErrandRepository errandRepository;

	private final ProcessService processService;

	public NoteService(final NoteRepository noteRepository,
		final ErrandRepository errandRepository,
		final ProcessService processService) {
		this.noteRepository = noteRepository;
		this.errandRepository = errandRepository;
		this.processService = processService;
	}

	@Retry(name = "OptimisticLocking")
	public void updateNote(final Long noteId, final String municipalityId, final String namespace, final NoteDTO noteDTO) {
		final var note = noteRepository.findByIdAndMunicipalityIdAndNamespace(noteId, municipalityId, namespace).orElseThrow(() -> Problem.valueOf(NOT_FOUND, NOTE_WAS_NOT_FOUND));
		patchNote(note, noteDTO);
		noteRepository.save(note);
		processService.updateProcess(note.getErrand());
	}

	@Retry(name = "OptimisticLocking")
	public void replaceNote(final Long noteId, final String municipalityId, final String namespace, final NoteDTO dto) {
		final var note = noteRepository.findByIdAndMunicipalityIdAndNamespace(noteId, municipalityId, namespace).orElseThrow(() -> Problem.valueOf(NOT_FOUND, NOTE_WAS_NOT_FOUND));
		putNote(note, dto);
		noteRepository.save(note);
		processService.updateProcess(note.getErrand());
	}

	public NoteDTO getNoteByIdAndMunicipalityIdAndNamespace(final Long noteId, final String municipalityId, final String namespace) {
		final var note = noteRepository.findByIdAndMunicipalityIdAndNamespace(noteId, municipalityId, namespace).orElseThrow(() -> Problem.valueOf(NOT_FOUND, NOTE_WAS_NOT_FOUND));
		return toNoteDto(note);
	}

	public List<NoteDTO> getNotesByErrandIdAndMunicipalityIdAndNamespaceAndNoteType(final Long errandId, final String municipalityId, final String namespace, final Optional<NoteType> noteType) {
		final var errand = errandRepository.findByIdAndMunicipalityIdAndNamespace(errandId, municipalityId, namespace).orElseThrow(() -> Problem.valueOf(NOT_FOUND, format(ERRAND_WAS_NOT_FOUND, errandId)));

		return noteType.map(type -> errand.getNotes().stream()
			.filter(note -> note.getNoteType() == type)
			.map(EntityMapper::toNoteDto)
			.toList()).orElseGet(() -> errand.getNotes().stream()
			.map(EntityMapper::toNoteDto)
			.toList());
	}

	public void deleteNoteByIdAndMunicipalityIdAndNamespace(final Long noteId, final String municipalityId, final String namespace) {
		final var note = noteRepository.findByIdAndMunicipalityIdAndNamespace(noteId, municipalityId, namespace)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, NOTE_WAS_NOT_FOUND));

		if (note.getNoteType() == NoteType.PUBLIC) {
			throw Problem.valueOf(FORBIDDEN, PUBLIC_NOTE_CANNOT_BE_DELETED);
		}
		noteRepository.delete(note);
	}

	@Retry(name = "OptimisticLocking")
	public void deleteNoteOnErrand(final Long errandId, final String municipalityId, final String namespace, final Long noteId) {
		final var errand = getErrandByIdAndMunicipalityIdAndNamespace(errandId, municipalityId, namespace);
		final var noteToRemove = errand.getNotes().stream()
			.filter(note -> note.getId().equals(noteId))
			.findAny()
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, format(NOTE_WITH_ID_X_WAS_NOT_FOUND_ON_ERRAND_WITH_ID_X, noteId, errandId)));
		errand.getNotes().remove(noteToRemove);
		errandRepository.save(errand);
		processService.updateProcess(errand);
	}

	@Retry(name = "OptimisticLocking")
	public NoteDTO addNoteToErrand(final Long errandId, final String municipalityId, final String namespace, final NoteDTO noteDTO) {
		final var oldErrand = getErrandByIdAndMunicipalityIdAndNamespace(errandId, municipalityId, namespace);
		final var note = toNote(noteDTO, municipalityId, namespace);
		note.setErrand(oldErrand);
		oldErrand.getNotes().add(note);
		final var updatedErrand = errandRepository.save(oldErrand);
		processService.updateProcess(updatedErrand);
		return toNoteDto(note);
	}

	public Errand getErrandByIdAndMunicipalityIdAndNamespace(final Long errandId, final String municipalityId, final String namespace) {
		return errandRepository.findByIdAndMunicipalityIdAndNamespace(errandId, municipalityId, namespace)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND,
				format(ERRAND_WAS_NOT_FOUND, errandId)));
	}


}
