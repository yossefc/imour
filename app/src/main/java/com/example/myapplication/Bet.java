package com.example.myapplication;

import com.google.firebase.Timestamp;

public class Bet {
    public String id;          // doc id
    public String userId;
    public int fixtureId;
    public String homeTeam;
    public String awayTeam;
    public long kickoffTs;     // epoch seconds
    public String pick;        // "1" / "X" / "2"
    public double odds;
    public String status;      // "PENDING" / "WON" / "LOST"
    public Integer finalHome;  // nullable
    public Integer finalAway;  // nullable
    public Timestamp createdAt;

    public Bet() {} // Firestore needs empty ctor
}
