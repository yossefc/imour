package com.example.myapplication;

public class Game {
    private int fixtureId;
    private String team1;
    private String team2;
    private long kickoffTs; // epoch seconds
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

    public int getFixtureId() { return fixtureId; }
    public String getTeam1() { return team1; }
    public String getTeam2() { return team2; }
    public long getKickoffTs() { return kickoffTs; }
    public double getOdds1() { return odds1; }
    public double getOddsX() { return oddsX; }
    public double getOdds2() { return odds2; }
}
