package com.example.myapplication;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class LeaguesResponse {
    @SerializedName("response")
    public List<LeagueItem> response;

    public static class LeagueItem {
        @SerializedName("league")
        public League league;

        @SerializedName("seasons")
        public List<Season> seasons;
    }

    public static class League {
        @SerializedName("id")
        public int id;

        @SerializedName("name")
        public String name;

        @SerializedName("country")
        public String country;
    }

    public static class Season {
        @SerializedName("year")
        public int year;

        @SerializedName("current")
        public boolean current;
    }
}
