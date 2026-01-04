package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class GameAdapter extends RecyclerView.Adapter<GameAdapter.GameViewHolder> {

    private List<Game> games;
    private Context context;

    public GameAdapter(Context context, List<Game> games) {
        this.context = context;
        this.games = games;
    }

    @NonNull
    @Override
    public GameViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.game_item, parent, false);
        return new GameViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GameViewHolder holder, int position) {
        Game game = games.get(position);
        holder.teamsTextView.setText(game.getTeam1() + " vs " + game.getTeam2());
        holder.betTeam1Button.setText("1 (" + game.getOdds1() + ")");
        holder.betDrawButton.setText("X (" + game.getOddsX() + ")");
        holder.betTeam2Button.setText("2 (" + game.getOdds2() + ")");

        holder.betTeam1Button.setOnClickListener(v -> openBetActivity(game, "1", game.getOdds1()));
        holder.betDrawButton.setOnClickListener(v -> openBetActivity(game, "X", game.getOddsX()));
        holder.betTeam2Button.setOnClickListener(v -> openBetActivity(game, "2", game.getOdds2()));
    }

    private void openBetActivity(Game game, String betType, double odds) {
        Intent intent = new Intent(context, BetActivity.class);
        intent.putExtra("fixtureId", game.getFixtureId());
        intent.putExtra("kickoffTs", game.getKickoffTs());
        intent.putExtra("team1", game.getTeam1());
        intent.putExtra("team2", game.getTeam2());
        intent.putExtra("betType", betType);
        intent.putExtra("odds", odds);
        context.startActivity(intent);
    }


    @Override
    public int getItemCount() {
        return games.size();
    }

    static class GameViewHolder extends RecyclerView.ViewHolder {
        TextView teamsTextView;
        Button betTeam1Button;
        Button betDrawButton;
        Button betTeam2Button;

        public GameViewHolder(@NonNull View itemView) {
            super(itemView);
            teamsTextView = itemView.findViewById(R.id.teamsTextView);
            betTeam1Button = itemView.findViewById(R.id.betTeam1Button);
            betDrawButton = itemView.findViewById(R.id.betDrawButton);
            betTeam2Button = itemView.findViewById(R.id.betTeam2Button);
        }
    }
}
