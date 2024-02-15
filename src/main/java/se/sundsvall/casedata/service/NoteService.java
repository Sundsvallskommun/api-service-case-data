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
import org.zalando.problem.ThrowableProblem;

import se.sundsvall.casedata.api.model.NoteDTO;
import se.sundsvall.casedata.integration.db.ErrandRepository;
import se.sundsvall.casedata.integration.db.NoteRepository;
import se.sundsvall.casedata.integration.db.model.enums.NoteType;
import se.sundsvall.casedata.service.util.mappers.EntityMapper;

import io.github.resilience4j.retry.annotation.Retry;

@Service
public class NoteService {

	static final String ERRAND_WAS_NOT_FOUND = "Errand was not found";
	static final String NOTE_WAS_NOT_FOUND = "Note was not found";
	static final String PUBLIC_NOTE_CANNOT_BE_DELETED = "Public notes can not be deleted";

	static final ThrowableProblem NOTE_NOT_FOUND_PROBLEM = Problem.valueOf(NOT_FOUND, NOTE_WAS_NOT_FOUND);
	static final ThrowableProblem ERRAND_NOT_FOUND_PROBLEM = Problem.valueOf(NOT_FOUND, ERRAND_WAS_NOT_FOUND);
	static final ThrowableProblem NOTE_CANNOT_BE_DELETED_PROBLEM = Problem.valueOf(FORBIDDEN, PUBLIC_NOTE_CANNOT_BE_DELETED);

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
	public void updateNote(final Long id, final NoteDTO noteDTO) {
		final var note = noteRepository.findById(id).orElseThrow(() -> NOTE_NOT_FOUND_PROBLEM);
		patchNote(note, noteDTO);
		noteRepository.save(note);
		processService.updateProcess(note.getErrand());
	}

	@Retry(name = "OptimisticLocking")
	public void replaceNote(final Long id, final NoteDTO dto) {
		final var note = noteRepository.findById(id).orElseThrow(() -> NOTE_NOT_FOUND_PROBLEM);
		putNote(note, dto);
		noteRepository.save(note);
		processService.updateProcess(note.getErrand());
	}

	public NoteDTO getNoteById(final Long id) {
		final var note = noteRepository.findById(id).orElseThrow(() -> NOTE_NOT_FOUND_PROBLEM);
		return toNoteDto(note);
	}

	public List<NoteDTO> getNotesByErrandIdAndNoteType(final Long errandId, final Optional<NoteType> noteType) {
		final var errand = errandRepository.findById(errandId).orElseThrow(() -> ERRAND_NOT_FOUND_PROBLEM);

		return noteType.map(type -> errand.getNotes().stream()
			.filter(note -> note.getNoteType() == type)
			.map(EntityMapper::toNoteDto)
			.toList()).orElseGet(() -> errand.getNotes().stream()
			.map(EntityMapper::toNoteDto)
			.toList());
	}

	public void deleteNoteById(final Long noteId) {
		var note = noteRepository.findById(noteId)
			.orElseThrow(() -> NOTE_NOT_FOUND_PROBLEM);

		if (note.getNoteType() == NoteType.PUBLIC) {
			throw NOTE_CANNOT_BE_DELETED_PROBLEM;
		}
		noteRepository.delete(note);
	}
}
