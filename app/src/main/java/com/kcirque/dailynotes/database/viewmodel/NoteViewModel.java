package com.kcirque.dailynotes.database.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.support.annotation.NonNull;

import com.kcirque.dailynotes.database.model.Note;
import com.kcirque.dailynotes.database.repository.NoteRepository;

import java.util.List;

import io.reactivex.Flowable;

public class NoteViewModel extends AndroidViewModel {
    private NoteRepository noteRepository;
    private Flowable<List<Note>> allNotes;
    private Flowable<List<Note>> allNotesOrderByTitle;

    public NoteViewModel(@NonNull Application application) {
        super(application);
        noteRepository = new NoteRepository(application);
        allNotes = noteRepository.getAllNotes();
        allNotesOrderByTitle = noteRepository.getAllNotesOrderByTitle();
    }

    public void insertNote(Note note) {
        noteRepository.insertNote(note);
    }

    public void updateNote(Note note) {
        noteRepository.updateNote(note);
    }

    public void deleteNote(Note note) {
        noteRepository.deleteNote(note);
    }

    public void deleteAllNote() {
        noteRepository.deleteAllNotes();
    }

    public Flowable<List<Note>> getAllNotes() {
        return allNotes;
    }

    public Flowable<List<Note>> getAllNotesOrderByTitle() {
        return allNotesOrderByTitle;
    }
}
