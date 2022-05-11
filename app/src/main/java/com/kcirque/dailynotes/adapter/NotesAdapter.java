package com.kcirque.dailynotes.adapter;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.kcirque.dailynotes.R;
import com.kcirque.dailynotes.database.model.Note;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;


public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.MyViewHolder> implements Filterable {

    public static final int LIST_VIEW = 1;
    public static final int GRID_VIEW = 2;
    private Context context;
    private List<Note> filteredNotes = new ArrayList<>();
    private List<Note> allNotes = new ArrayList<>();
    private int type;

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String query = constraint.toString();
                List<Note> tempList = new ArrayList<>();
                if (query.isEmpty()) {
                    filteredNotes = allNotes;
                } else {
                    for (Note note : allNotes) {
                        if (note.getDescription().toLowerCase().contains(query.toLowerCase()) ||
                                note.getTitle().toLowerCase().contains(query.toLowerCase())) {
                            tempList.add(note);
                        }
                    }
                    filteredNotes = tempList;
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = filteredNotes;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filteredNotes = (List<Note>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView note, title;
        CircleImageView dot;
        TextView timestamp;
        ImageView isLockImageView;

        public MyViewHolder(View view) {
            super(view);
            note = view.findViewById(R.id.note_text_view);
            title = view.findViewById(R.id.note_title_text_view);
            dot = view.findViewById(R.id.divider);
            timestamp = view.findViewById(R.id.note_create_time_text_view);
            isLockImageView = view.findViewById(R.id.lock_image_view);

        }
    }


    public NotesAdapter(Context context, int type) {
        this.context = context;
        this.type = type;
    }

    public void setNotesList(List<Note> notesList) {
        this.filteredNotes = notesList;
        this.allNotes = notesList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView;
        if (type == LIST_VIEW) {
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.note_list_item, parent, false);
        } else {
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.note_grid_item, parent, false);
        }
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Note note = filteredNotes.get(position);

        holder.title.setText(note.getTitle());
        holder.note.setText(note.getDescription());
        holder.itemView.setBackgroundColor(getRandomMaterialColor("400"));
        // Changing dot color to random color
       // holder.dot.setBackgroundColor(getRandomMaterialColor("500"));
        //Glide.with(context).load(null).into(holder.dot);
        holder.dot.setColorFilter(getRandomMaterialColor("500"));
        // Formatting and displaying timestamp
        holder.timestamp.setText(formatDate(note.getDataTime()));
        if (note.isLock()) {
            holder.isLockImageView.setVisibility(View.VISIBLE);
        } else {
            holder.isLockImageView.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return filteredNotes.size();
    }

    /**
     * Chooses random color defined in res/array.xml
     */
    private int getRandomMaterialColor(String typeColor) {
        int returnColor = Color.GRAY;
        int arrayId = context.getResources().getIdentifier("mdcolor_" + typeColor, "array", context.getPackageName());

        if (arrayId != 0) {
            TypedArray colors = context.getResources().obtainTypedArray(arrayId);
            int index = (int) (Math.random() * colors.length());
            returnColor = colors.getColor(index, Color.GRAY);
            colors.recycle();
        }
        return returnColor;
    }

    /**
     * Formatting timestamp to `MMM d` format
     * Input: 2018-02-21 00:15:42
     * Output: Feb 21
     */
    private String formatDate(long timestamp) {
        Date date = new Date(timestamp);
        SimpleDateFormat fmtOut = new SimpleDateFormat("MMM dd", Locale.US);
        return fmtOut.format(date);
    }
}