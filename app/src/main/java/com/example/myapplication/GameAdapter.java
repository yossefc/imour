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

    private final List<Game> games;
    private final Context context;

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

        // Afficher équipes + date/heure
        String teamsText = game.getTeam1() + " vs " + game.getTeam2();
        String dateText = "📅 " + game.getFormattedDateTime();
        holder.teamsTextView.setText(teamsText + "\n" + dateText);

        // Afficher les cotes (ou "?" si pas disponibles)
        String odds1Text = game.getOdds1() > 0 ? String.format("%.2f", game.getOdds1()) : "?";
        String oddsXText = game.getOddsX() > 0 ? String.format("%.2f", game.getOddsX()) : "?";
        String odds2Text = game.getOdds2() > 0 ? String.format("%.2f", game.getOdds2()) : "?";

        holder.betTeam1Button.setText("1 (" + odds1Text + ")");
        holder.betDrawButton.setText("X (" + oddsXText + ")");
        holder.betTeam2Button.setText("2 (" + odds2Text + ")");

        // Pour le plan gratuit (données historiques), on active tous les boutons
        // En production avec plan payant, décommenter le code ci-dessous
        holder.betTeam1Button.setEnabled(true);
        holder.betDrawButton.setEnabled(true);
        holder.betTeam2Button.setEnabled(true);
        holder.betTeam1Button.setAlpha(1f);
        holder.betDrawButton.setAlpha(1f);
        holder.betTeam2Button.setAlpha(1f);

        /* // Code pour plan payant (désactiver matchs passés):
        boolean isFuture = game.isFuture();
        holder.betTeam1Button.setEnabled(isFuture);
        holder.betDrawButton.setEnabled(isFuture);
        holder.betTeam2Button.setEnabled(isFuture);
        if (!isFuture) {
            holder.betTeam1Button.setAlpha(0.5f);
            holder.betDrawButton.setAlpha(0.5f);
            holder.betTeam2Button.setAlpha(0.5f);
        }
        */

        // Click listeners
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