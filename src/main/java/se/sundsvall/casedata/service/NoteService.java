package se.sundsvall.casedata.service;

import static org.zalando.problem.Status.FORBIDDEN;
import static org.zalando.problem.Status.NOT_FOUND;
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
import se.sundsvall.casedata.integration.db.model.enums.NoteType;
import se.sundsvall.casedata.service.util.mappers.EntityMapper;

import io.github.resilience4j.retry.annotation.Retry;

@Service
public class NoteService {

	private static final String ERRAND_WAS_NOT_FOUND = "Errand was not found";

	private static final String NOTE_WAS_NOT_FOUND = "Note was not found";

	private static final String PUBLIC_NOTE_CANNOT_BE_DELETED = "Public notes can not be deleted";

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

	public NoteDTO getNoteByIdAndMunicipalityId(final Long noteId, final String municipalityId, final String namespace) {
		final var note = noteRepository.findByIdAndMunicipalityIdAndNamespace(noteId, municipalityId, namespace).orElseThrow(() -> Problem.valueOf(NOT_FOUND, NOTE_WAS_NOT_FOUND));
		return toNoteDto(note);
	}

	public List<NoteDTO> getNotesByErrandIdAndMunicipalityIdAndNoteType(final Long errandId, final String municipalityId, final String namespace, final Optional<NoteType> noteType) {
		final var errand = errandRepository.findByIdAndMunicipalityIdAndNamespace(errandId, municipalityId, namespace).orElseThrow(() -> Problem.valueOf(NOT_FOUND, ERRAND_WAS_NOT_FOUND));

		return noteType.map(type -> errand.getNotes().stream()
			.filter(note -> note.getNoteType() == type)
			.map(EntityMapper::toNoteDto)
			.toList()).orElseGet(() -> errand.getNotes().stream()
			.map(EntityMapper::toNoteDto)
			.toList());
	}

	public void deleteNoteByIdAndMunicipalityId(final Long noteId, final String municipalityId, final String namespace) {
		final var note = noteRepository.findByIdAndMunicipalityIdAndNamespace(noteId, municipalityId, namespace)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, NOTE_WAS_NOT_FOUND));

		if (note.getNoteType() == NoteType.PUBLIC) {
			throw Problem.valueOf(FORBIDDEN, PUBLIC_NOTE_CANNOT_BE_DELETED);
		}
		noteRepository.delete(note);
	}

}
