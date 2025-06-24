package com.example.jigsaw_licenta.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jigsaw_licenta.R;
import com.example.jigsaw_licenta.utils.FirebaseStatsHelper;

import java.util.List;
import java.util.Locale;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.ViewHolder> {
    private final List<FirebaseStatsHelper.LeaderboardEntry> entries;
    private final Context context;

    public LeaderboardAdapter(Context context, List<FirebaseStatsHelper.LeaderboardEntry> entries) {
        this.context = context;
        this.entries = entries;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView rankText, nicknameText, scoreText;
        LinearLayout container;

        public ViewHolder(View view) {
            super(view);
            container = view.findViewById(R.id.itemContainer);
            rankText = view.findViewById(R.id.rankText);
            nicknameText = view.findViewById(R.id.nicknameText);
            scoreText = view.findViewById(R.id.scoreText);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_leaderboard_entry, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FirebaseStatsHelper.LeaderboardEntry entry = entries.get(position);
        holder.rankText.setText(String.format(Locale.getDefault(), "%d.", position + 1));
        holder.nicknameText.setText(entry.nickname);
        holder.scoreText.setText(String.format(Locale.getDefault(), "%.1f", entry.score));

        int bgColor;
        switch (position) {
            case 0:
                bgColor = ContextCompat.getColor(context, R.color.gold);
                break;
            case 1:
                bgColor = ContextCompat.getColor(context, R.color.silver);
                break;
            case 2:
                bgColor = ContextCompat.getColor(context, R.color.bronze);
                break;
            default:
                bgColor = ContextCompat.getColor(context, R.color.light_purple);
        }
        holder.container.setBackgroundColor(bgColor);
    }

    @Override
    public int getItemCount() {
        return entries.size();
    }
}
