package com.example.myapplication;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class FixturesResponse {
    @SerializedName("response")
    public List<FixtureItem> response;

    public static class FixtureItem {
        @SerializedName("fixture")
        public Fixture fixture;

        @SerializedName("teams")
        public Teams teams;

        @SerializedName("goals")
        public Goals goals;
    }

    public static class Fixture {
        @SerializedName("id")
        public int id;

        @SerializedName("timestamp")
        public long timestamp;

        @SerializedName("status")
        public Status status;
    }

    public static class Status {
        @SerializedName("short")
        public String shortStatus; // למשל FT
    }

    public static class Teams {
        @SerializedName("home")
        public Team home;

        @SerializedName("away")
        public Team away;
    }

    public static class Team {
        @SerializedName("name")
        public String name;
    }

    public static class Goals {
        @SerializedName("home")
        public Integer home;

        @SerializedName("away")
        public Integer away;
    }
}
