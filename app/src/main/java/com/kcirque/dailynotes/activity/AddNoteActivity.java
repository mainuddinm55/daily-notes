package com.kcirque.dailynotes.activity;

import androidx.lifecycle.ViewModelProviders;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.kcirque.dailynotes.R;
import com.kcirque.dailynotes.database.model.Note;
import com.kcirque.dailynotes.database.viewmodel.NoteViewModel;
import com.kcirque.dailynotes.utils.SharedPref;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class AddNoteActivity extends AppCompatActivity {
    public static final String EXTRA_NOTE = "com.kcirque.dailynotes.EXTRA_NOTE";
    private static final String TAG = "AddNoteActivity";
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM, yyyy hh:mm a", Locale.US);
    Date date = new Date(System.currentTimeMillis());

    NoteViewModel noteViewModel;

    CompositeDisposable disposable = new CompositeDisposable();
    private EditText noteEditText, titleEditText;

    private Note note = null;

    private SharedPref sharedPref;
    private TextView currentDateTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);
        noteViewModel = ViewModelProviders.of(this).get(NoteViewModel.class);

        currentDateTextView = findViewById(R.id.date_text_view);
        noteEditText = findViewById(R.id.note_edit_text);
        titleEditText = findViewById(R.id.note_title_edit_text);

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Bundle noteBundle = getIntent().getExtras();
        sharedPref = new SharedPref(this);
        if (noteBundle != null) {
            note = (Note) noteBundle.getSerializable(EXTRA_NOTE);
            getSupportActionBar().setTitle(note.getTitle());
            if (note.isLock()) {
                showLoginDialog(false);
            } else {
                updateUI(note);
            }
        } else {
            currentDateTextView.setText(dateFormat.format(date));
        }

    }

    private void updateUI(Note note) {
        Date date = new Date(note.getDataTime());
        long different = (System.currentTimeMillis() - note.getDataTime()) / 1000;
        TextView timeSpentTextView = findViewById(R.id.time_spend_text_view);
        long minute = different / 60;
        if (minute > 60) {
            long hours = minute / 60;
            if (hours > 24) {
                int day = (int) (hours / 24);
                timeSpentTextView.setText(String.format(Locale.US, "%d Days ago", day));
            } else {
                timeSpentTextView.setText(String.format(Locale.US, "%d Hours ago", hours));
            }
        } else {
            timeSpentTextView.setText(String.format(Locale.US, "%d Minutes ago", minute));
        }
        currentDateTextView.setText(dateFormat.format(date));
        noteEditText.setText(note.getDescription());
        noteEditText.setSelection(noteEditText.getText().toString().length());
        titleEditText.setText(note.getTitle());
        titleEditText.setSelection(titleEditText.getText().toString().length());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_note_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem lockMenuItem = menu.findItem(R.id.action_lock);
        MenuItem unlockMenuItem = menu.findItem(R.id.action_unlock);
        if (note != null && note.isLock()) {
            lockMenuItem.setVisible(false);
            unlockMenuItem.setVisible(true);
        } else {
            lockMenuItem.setVisible(true);
            unlockMenuItem.setVisible(false);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.action_save_note:
                if (note != null) {
                    updateNote(note, note.isLock());
                } else {
                    saveNote();
                }
                break;
            case R.id.action_discard_note:
                deleteNote();
                break;
            case R.id.action_send:
                sendMessage();
                break;
            case R.id.action_lock:
                lockNoteDialog();
                break;
            case R.id.action_unlock:
                break;
            case R.id.action_copy:
                copyToClipboard();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void lockNoteDialog() {
        if (sharedPref.getPin() == null) {
            AlertDialog.Builder pinDialog = new AlertDialog.Builder(this);
            pinDialog.setTitle("SETUP MASTER PIN");
            View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.lock_note_dialog, null);
            pinDialog.setView(view);
            final EditText oldPin = view.findViewById(R.id.old_pin_edit_text);
            final EditText newPin = view.findViewById(R.id.new_pin_edit_text);
            final EditText confirmPin = view.findViewById(R.id.confirm_pin_edit_text);
            oldPin.setVisibility(View.GONE);
            pinDialog.setPositiveButton("SET", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (newPin.getText().toString().length() < 4) {
                        Toast.makeText(AddNoteActivity.this, "Please enter minimum 4 digit", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (!newPin.getText().toString().equals(confirmPin.getText().toString())) {
                        Toast.makeText(AddNoteActivity.this, "Pin does not match", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    sharedPref.putPin(newPin.getText().toString());
                    updateNote(note, true);
                    dialog.dismiss();
                }
            });

            pinDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            pinDialog.show();
        } else {
            showLoginDialog(true);
        }
    }


    private void showLoginDialog(final boolean isLock) {
        AlertDialog.Builder pinDialog = new AlertDialog.Builder(this);
        pinDialog.setTitle("ENTER PIN");
        pinDialog.setCancelable(false);
        View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.login_dialog, null);
        pinDialog.setView(view);
        final EditText pin = view.findViewById(R.id.pin_edit_text);
        pinDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (!pin.getText().toString().equals(sharedPref.getPin())) {
                    Toast.makeText(AddNoteActivity.this, "Invalid pin", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                if (isLock) {
                    updateNote(note, true);
                } else {
                    updateUI(note);
                }
                dialog.dismiss();
            }
        });

        pinDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if (!isLock) {
                    finish();
                }
            }
        });
        pinDialog.show();
    }

    private void sendMessage() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        //intent.setType("vnd.android-dir/mms-sms");
        //intent.setData(Uri.parse("smsto:"));
        intent.setDataAndType(Uri.parse("smsto:"), "vnd.android-dir/mms-sms");
        intent.putExtra("sms_body", noteEditText.getText().toString());
        try {
            startActivity(intent);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this,
                    "SMS faild, please try again later.", Toast.LENGTH_SHORT).show();
        }
    }

    private void copyToClipboard() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("", noteEditText.getText().toString());
        if (clipboard != null)
            clipboard.setPrimaryClip(clip);

        Toast.makeText(getApplicationContext(), "Copied to clipboard", Toast.LENGTH_SHORT).show();
    }

    private void deleteNote() {
        if (note != null) {
            disposable.add(
                    Observable.create(new ObservableOnSubscribe<Object>() {
                        @Override
                        public void subscribe(ObservableEmitter<Object> emitter) {
                            noteViewModel.deleteNote(note);
                            emitter.onComplete();
                        }
                    }).subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Consumer<Object>() {
                                @Override
                                public void accept(Object o) {

                                }
                            }, new Consumer<Throwable>() {
                                @Override
                                public void accept(Throwable throwable) {
                                    Log.e(TAG, "accept: " + throwable.getMessage());
                                    Toast.makeText(AddNoteActivity.this, "" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }, new Action() {
                                @Override
                                public void run() {
                                    Toast.makeText(AddNoteActivity.this, "Note Deleted", Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                            })
            );
        } else {
            finish();
        }
    }

    private void saveNote() {
        if (TextUtils.isEmpty(titleEditText.getText())) {
            Toast.makeText(this, "Please enter title", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(noteEditText.getText())) {
            Toast.makeText(this, "Please enter note", Toast.LENGTH_SHORT).show();
            return;
        }

        String description = noteEditText.getText().toString();
        String title = titleEditText.getText().toString();

        long noteDate = date.getTime();
        final Note note = new Note(title, description, noteDate, false);
        disposable.add(
                Observable.create(new ObservableOnSubscribe<Object>() {
                    @Override
                    public void subscribe(ObservableEmitter<Object> emitter) {
                        noteViewModel.insertNote(note);
                        emitter.onComplete();
                    }
                }).observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe(new Consumer<Object>() {
                            @Override
                            public void accept(Object o) {

                            }
                        }, new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) {

                            }
                        }, new Action() {
                            @Override
                            public void run() {
                                Toast.makeText(AddNoteActivity.this, "Note Added", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        })
        );
    }

    private void updateNote(Note note, boolean isLock) {
        if (TextUtils.isEmpty(titleEditText.getText())) {
            Toast.makeText(this, "Please enter title", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(noteEditText.getText())) {
            Toast.makeText(this, "Please enter note", Toast.LENGTH_SHORT).show();
            return;
        }
        String description = noteEditText.getText().toString();
        String title = titleEditText.getText().toString();
        long noteDate = date.getTime();
        final Note updateNote = new Note(title, description, noteDate, isLock);
        updateNote.setId(note.getId());
        disposable.add(
                Observable.create(new ObservableOnSubscribe<Object>() {
                    @Override
                    public void subscribe(ObservableEmitter<Object> emitter) {
                        noteViewModel.updateNote(updateNote);
                        emitter.onComplete();
                    }
                }).observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe(new Consumer<Object>() {
                            @Override
                            public void accept(Object o) {

                            }
                        }, new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) {

                            }
                        }, new Action() {
                            @Override
                            public void run() {
                                Toast.makeText(AddNoteActivity.this, "Note Updated", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        })
        );
    }
}
