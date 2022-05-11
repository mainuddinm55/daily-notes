package com.kcirque.dailynotes.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.kcirque.dailynotes.database.model.Note;

import java.util.List;

import io.reactivex.Flowable;

@Dao
public interface NoteDao {
    @Insert
    void insertNote(Note note);

    @Update
    void updateNote(Note note);

    @Delete
    void deleteNote(Note note);

    @Query("DELETE FROM note")
    void deleteAllNotes();

    @Query("SELECT * FROM note ORDER BY date_time DESC")
    Flowable<List<Note>> getAllNotes();

    @Query("SELECT * FROM note ORDER BY title ASC")
    Flowable<List<Note>> getAllNotesOrderByTitle();
}
