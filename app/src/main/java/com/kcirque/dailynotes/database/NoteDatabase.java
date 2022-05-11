package com.kcirque.dailynotes.database;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import android.content.Context;

import com.kcirque.dailynotes.database.dao.NoteDao;
import com.kcirque.dailynotes.database.model.Note;

import static com.kcirque.dailynotes.database.NoteDatabase.DATABASE_VERSION;

@Database(entities = {Note.class}, version = DATABASE_VERSION,exportSchema = false)
public abstract class NoteDatabase extends RoomDatabase {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "note_database";

    private static NoteDatabase sInstance;

    public abstract NoteDao noteDao();

    public synchronized static NoteDatabase getInstance(Context context) {
        if (sInstance == null) {
            sInstance = Room.databaseBuilder(context, NoteDatabase.class, DATABASE_NAME)
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return sInstance;
    }
}
