package com.example.kahootmaster;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ScoresAdapter extends RecyclerView.Adapter<ScoresAdapter.ScoreViewHolder> {

    private List<Player> playersList;

    public ScoresAdapter(List<Player> playersList) {
        this.playersList = playersList;
    }

    @NonNull
    @Override
    public ScoreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_score, parent, false);
        return new ScoreViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ScoreViewHolder holder, int position) {
        Player player = playersList.get(position);
        holder.playerNameTextView.setText(player.getName());
        holder.playerScoreTextView.setText(String.valueOf(player.getScore()));
    }

    @Override
    public int getItemCount() {
        return playersList.size();
    }

    static class ScoreViewHolder extends RecyclerView.ViewHolder {
        TextView playerNameTextView;
        TextView playerScoreTextView;

        public ScoreViewHolder(@NonNull View itemView) {
            super(itemView);
            playerNameTextView = itemView.findViewById(R.id.playerNameTextView);
            playerScoreTextView = itemView.findViewById(R.id.playerScoreTextView);
        }
    }
}