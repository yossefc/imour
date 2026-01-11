package com.example.myapplication;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Game implements Serializable {

    private final int fixtureId;
    private final String team1;
    private final String team2;
    private final long kickoffTs; // epoch seconds
    private double odds1;
    private double oddsX;
    private double odds2;

    public Game(int fixtureId, String team1, String team2, long kickoffTs,
                double odds1, double oddsX, double odds2) {
        this.fixtureId = fixtureId;
        this.team1 = team1;
        this.team2 = team2;
        this.kickoffTs = kickoffTs;
        this.odds1 = odds1;
        this.oddsX = oddsX;
        this.odds2 = odds2;
    }

    // Getters principaux
    public int getFixtureId() {
        return fixtureId;
    }

    public String getTeam1() {
        return team1;
    }

    public String getTeam2() {
        return team2;
    }

    public long getKickoffTs() {
        return kickoffTs;
    }

    public double getOdds1() {
        return odds1;
    }

    public double getOddsX() {
        return oddsX;
    }

    public double getOdds2() {
        return odds2;
    }

    // Setters pour odds (si tu veux les mettre à jour plus tard)
    public void setOdds1(double odds1) {
        this.odds1 = odds1;
    }

    public void setOddsX(double oddsX) {
        this.oddsX = oddsX;
    }

    public void setOdds2(double odds2) {
        this.odds2 = odds2;
    }

    // Méthodes utilitaires pour l'affichage
    public String getFormattedDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return sdf.format(new Date(kickoffTs * 1000));
    }

    public String getFormattedTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdf.format(new Date(kickoffTs * 1000));
    }

    public String getFormattedDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM HH:mm", Locale.getDefault());
        return sdf.format(new Date(kickoffTs * 1000));
    }

    public String getMatchTitle() {
        return team1 + " vs " + team2;
    }

    public boolean hasOdds() {
        return odds1 > 0 && oddsX > 0 && odds2 > 0;
    }

    public boolean isFuture() {
        return kickoffTs > (System.currentTimeMillis() / 1000);
    }

    @Override
    public String toString() {
        return getFormattedDateTime() + " | " + getMatchTitle();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Game game = (Game) o;
        return fixtureId == game.fixtureId;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(fixtureId);
    }
}