package com.example.myapplication;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class OddsResponse {
    @SerializedName("response")
    public List<OddsItem> response;

    public static class OddsItem {
        @SerializedName("fixture")
        public Fixture fixture;

        @SerializedName("bookmakers")
        public List<Bookmaker> bookmakers;
    }

    public static class Fixture {
        @SerializedName("id")
        public int id;
    }

    public static class Bookmaker {
        @SerializedName("bets")
        public List<BetMarket> bets;
    }

    public static class BetMarket {
        @SerializedName("name")
        public String name; // לדוגמה "Match Winner"

        @SerializedName("values")
        public List<Value> values;
    }

    public static class Value {
        @SerializedName("value")
        public String value; // "Home"/"Draw"/"Away" או "1"/"X"/"2" תלוי ספק

        @SerializedName("odd")
        public String odd; // "2.35" וכו'
    }
}
