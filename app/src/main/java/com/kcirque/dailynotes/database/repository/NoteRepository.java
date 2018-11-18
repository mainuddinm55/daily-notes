package com.kcirque.dailynotes.database.repository;

import android.app.Application;

import com.kcirque.dailynotes.database.NoteDatabase;
import com.kcirque.dailynotes.database.dao.NoteDao;
import com.kcirque.dailynotes.database.model.Note;

import java.util.List;

import io.reactivex.Flowable;

public class NoteRepository {
    private NoteDao noteDao;
    private Flowable<List<Note>> allNotes;
    private Flowable<List<Note>> allNotesOrderByTitle;

    public NoteRepository(Application application) {
        NoteDatabase noteDatabase = NoteDatabase.getInstance(application);
        noteDao = noteDatabase.noteDao();
        allNotes = noteDao.getAllNotes();
        allNotesOrderByTitle = noteDao.getAllNotesOrderByTitle();
    }

    public void insertNote(Note note) {
        noteDao.insertNote(note);
    }

    public void updateNote(Note note) {
        noteDao.updateNote(note);
    }

    public void deleteNote(Note note) {
        noteDao.deleteNote(note);
    }

    public void deleteAllNotes() {
        noteDao.deleteAllNotes();
    }

    public Flowable<List<Note>> getAllNotes() {
        return allNotes;
    }

    public Flowable<List<Note>> getAllNotesOrderByTitle() {
        return allNotesOrderByTitle;
    }
}
