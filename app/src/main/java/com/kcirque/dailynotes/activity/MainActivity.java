package com.kcirque.dailynotes.activity;

import android.Manifest;
import androidx.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.kcirque.dailynotes.R;
import com.kcirque.dailynotes.adapter.NotesAdapter;
import com.kcirque.dailynotes.database.model.Note;
import com.kcirque.dailynotes.database.viewmodel.NoteViewModel;
import com.kcirque.dailynotes.utils.RecyclerTouchListener;
import com.kcirque.dailynotes.utils.SharedPref;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;


public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener, RewardedVideoAdListener {
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private NoteViewModel noteViewModel;
    private static final String TAG = "MainActivity";
    List<Note> allNotes;
    private NotesAdapter adapter;

    public static final int ORDER_BY_TITLE = 1;
    public static final int ORDER_BY_DATE = 2;
    private static final int LIST_VIEW = 1;
    private static final int GRID_VIEW = 2;
    private RecyclerView noteListRecyclerView;
    private SharedPref sharedPref;
    private InterstitialAd mInterstitialAd;
    private RewardedVideoAd mRewardedVideoAd;
    int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppTheme);
        setContentView(R.layout.activity_main);

        MobileAds.initialize(this,
                getString(R.string.admob_app_id));

        AdView adView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getString(R.string.interstitial_unit_id));
        mInterstitialAd.loadAd(new AdRequest.Builder().build());

        mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(this);
        mRewardedVideoAd.setRewardedVideoAdListener(this);
        loadRewardedVideoAd();

        noteViewModel = ViewModelProviders.of(this).get(NoteViewModel.class);
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.SEND_SMS)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        // permission is granted, open the camera
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        // check for permanent denial of permission
                        if (response.isPermanentlyDenied()) {
                            // navigate user to app settings
                            Log.e(TAG, "onPermissionDenied: ");
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }

                }).check();

        FloatingActionButton addNoteFab = findViewById(R.id.add_note_fab);
        addNoteFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                count++;
                if (count % 3 == 0) {
                    if (mRewardedVideoAd.isLoaded()) {
                        mRewardedVideoAd.show();
                    } else {
                        Intent addNoteIntent = new Intent(getApplicationContext(), AddNoteActivity.class);
                        startActivity(addNoteIntent);
                    }
                } else {
                    Intent addNoteIntent = new Intent(getApplicationContext(), AddNoteActivity.class);
                    startActivity(addNoteIntent);
                }
            }
        });
        sharedPref = new SharedPref(this);
        noteListRecyclerView = findViewById(R.id.note_list_recycler_view);
        noteListRecyclerView.setHasFixedSize(true);
        if (sharedPref.getView().equals("List View")) {
            showView(LIST_VIEW);
        } else {
            showView(GRID_VIEW);
        }
        noteListRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(this, noteListRecyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, final int position) {
                mInterstitialAd.setAdListener(new AdListener() {
                    @Override
                    public void onAdClosed() {
                        mInterstitialAd.loadAd(new AdRequest.Builder().build());
                        Intent updateNote = new Intent(MainActivity.this, AddNoteActivity.class);
                        updateNote.putExtra(AddNoteActivity.EXTRA_NOTE, allNotes.get(position));
                        startActivity(updateNote);
                    }
                });
                if (mInterstitialAd.isLoaded()) {
                    mInterstitialAd.show();
                } else {
                    Intent updateNote = new Intent(MainActivity.this, AddNoteActivity.class);
                    updateNote.putExtra(AddNoteActivity.EXTRA_NOTE, allNotes.get(position));
                    startActivity(updateNote);
                }
            }

            @Override
            public void onLongClick(View view, final int position) {
                PopupMenu popupMenu = new PopupMenu(MainActivity.this, view);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.action_delete:
                                deleteNote(allNotes.get(position));
                                //fetchAllNotes(ORDER_BY_DATE);
                                break;
                            case R.id.action_edit:
                                Intent updateNote = new Intent(MainActivity.this, AddNoteActivity.class);
                                updateNote.putExtra(AddNoteActivity.EXTRA_NOTE, allNotes.get(position));
                                startActivity(updateNote);
                                break;
                        }
                        return true;
                    }
                });
                popupMenu.inflate(R.menu.popup_menu);
                popupMenu.show();
            }
        }));

        if (sharedPref.getSortBy().equals("Note Title")) {
            fetchAllNotes(ORDER_BY_TITLE);
        } else {
            fetchAllNotes(ORDER_BY_DATE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setQueryHint("Search Here");
        searchView.setOnQueryTextListener(this);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_sort:
                showActionsDialog();
                break;
            case R.id.action_view:
                showViewDialog();
                break;
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            case R.id.action_share:
                shareApps();
                break;
            case R.id.action_like:
                likeApps();
                break;
            case R.id.action_more_apps:
                moreApps();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showView(int type) {
        if (type == LIST_VIEW) {
            adapter = new NotesAdapter(this, NotesAdapter.LIST_VIEW);
            noteListRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            noteListRecyclerView.setItemAnimator(new DefaultItemAnimator());
            noteListRecyclerView.setAdapter(adapter);
            if (sharedPref.getSortBy().equals("Note Title")) {
                fetchAllNotes(ORDER_BY_TITLE);
            } else {
                fetchAllNotes(ORDER_BY_DATE);
            }
        } else {
            adapter = new NotesAdapter(this, NotesAdapter.GRID_VIEW);
            noteListRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
            noteListRecyclerView.setItemAnimator(new DefaultItemAnimator());
            noteListRecyclerView.setAdapter(adapter);
            if (sharedPref.getSortBy().equals("Note Title")) {
                fetchAllNotes(ORDER_BY_TITLE);
            } else {
                fetchAllNotes(ORDER_BY_DATE);
            }
        }
    }

    private void showActionsDialog() {
        CharSequence colors[] = new CharSequence[]{"Note Title", "Create Date"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose option");
        builder.setItems(colors, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    fetchAllNotes(ORDER_BY_TITLE);
                } else {
                    fetchAllNotes(ORDER_BY_DATE);
                }
            }
        });
        builder.show();
    }

    private void showViewDialog() {
        CharSequence colors[] = new CharSequence[]{"List View", "Grid View"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose option");
        builder.setItems(colors, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    showView(LIST_VIEW);
                } else {
                    showView(GRID_VIEW);
                }
            }
        });
        builder.show();
    }

    private void deleteNote(final Note note) {
        if (note != null) {
            compositeDisposable.add(
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
                                    Toast.makeText(MainActivity.this, "" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }, new Action() {
                                @Override
                                public void run() {
                                    Toast.makeText(MainActivity.this, "Note Deleted", Toast.LENGTH_SHORT).show();
                                }
                            })
            );
        }
    }

    private void fetchAllNotes(int orderBy) {
        if (orderBy == ORDER_BY_TITLE) {
            compositeDisposable.add(noteViewModel.getAllNotesOrderByTitle().subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<List<Note>>() {
                        @Override
                        public void accept(List<Note> notes) {
                            getAllNotes(notes);
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) {
                            Toast.makeText(MainActivity.this, throwable.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }));
        } else {
            compositeDisposable.add(noteViewModel.getAllNotes().subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<List<Note>>() {
                        @Override
                        public void accept(List<Note> notes) {
                            getAllNotes(notes);
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) {
                            Toast.makeText(MainActivity.this, throwable.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }));
        }
    }

    private void getAllNotes(List<Note> notes) {
        allNotes = new ArrayList<>();
        allNotes.clear();
        allNotes.addAll(notes);
        adapter.setNotesList(allNotes);
        toggleNoNotes();
    }

    private void toggleNoNotes() {
        TextView noNotesTextView = findViewById(R.id.no_notes_text_view);
        if (allNotes.size() > 0) {
            noNotesTextView.setVisibility(View.GONE);
        } else {
            noNotesTextView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        adapter.getFilter().filter(newText);
        return true;
    }

    private void likeApps() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://learncoding.info/"));
        startActivity(intent);
    }

    private void moreApps() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://learncoding.info/"));
        startActivity(intent);
    }

    private void shareApps() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/type");
        String subject = "Daily Notes";
        String body = "This is most user app to keep personal notes to maintain daily life";
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        shareIntent.putExtra(Intent.EXTRA_TEXT, body);
        startActivity(Intent.createChooser(shareIntent, "Share with"));
    }

    private void loadRewardedVideoAd() {
        mRewardedVideoAd.loadAd(getString(R.string.reward_video_unit_id),
                new AdRequest.Builder().build());
    }

    @Override
    public void onRewardedVideoAdLoaded() {

    }

    @Override
    public void onRewardedVideoAdOpened() {

    }

    @Override
    public void onRewardedVideoStarted() {

    }

    @Override
    public void onRewardedVideoAdClosed() {
        loadRewardedVideoAd();
        Intent addNoteIntent = new Intent(getApplicationContext(), AddNoteActivity.class);
        startActivity(addNoteIntent);
    }

    @Override
    public void onRewarded(RewardItem rewardItem) {

    }

    @Override
    public void onRewardedVideoAdLeftApplication() {

    }

    @Override
    public void onRewardedVideoAdFailedToLoad(int i) {

    }

    @Override
    public void onRewardedVideoCompleted() {

    }
}
