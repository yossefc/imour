package com.example.myapplication;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ApiFootballClient {

    private static final String TAG = "ApiFootballClient";
    private static final String BASE_URL = "https://v3.football.api-sports.io/";
    private static final int LOG_BODY_LIMIT = 350;

    private final OkHttpClient http = new OkHttpClient();

    private Request.Builder baseRequest(String url) {
        return new Request.Builder()
                .url(url)
                .addHeader("x-apisports-key", BuildConfig.API_FOOTBALL_KEY);
    }

    private static String encode(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    private static void debugLog(String url, String body) {
        Log.d(TAG, "URL=" + url);
        if (body == null) body = "";
        String cut = body.substring(0, Math.min(LOG_BODY_LIMIT, body.length()));
        Log.d(TAG, "BODY(first " + cut.length() + ")=" + cut);
    }

    // ------------------------------------------------------------
    // 1) עונה נוכחית (Start Year) לפי leagues?id=...
    // מחזיר למשל 2025 => עונת 2025/2026
    // ------------------------------------------------------------
    public int getCurrentSeasonStartYear(int leagueId) throws IOException {
        String url = BASE_URL + "leagues?id=" + leagueId;

        Request req = baseRequest(url).get().build();
        try (Response res = http.newCall(req).execute()) {
            String body = (res.body() != null) ? res.body().string() : "";
            debugLog(url, body);

            if (!res.isSuccessful()) throw new IOException("HTTP " + res.code() + " => " + body);

            try {
                JSONObject root = new JSONObject(body);
                JSONArray response = root.getJSONArray("response");
                if (response.length() == 0) throw new IOException("No league found for id=" + leagueId);

                JSONObject leagueObj = response.getJSONObject(0);
                JSONArray seasons = leagueObj.getJSONArray("seasons");

                // current=true
                for (int i = 0; i < seasons.length(); i++) {
                    JSONObject s = seasons.getJSONObject(i);
                    if (s.optBoolean("current", false)) {
                        return s.getInt("year");
                    }
                }

                // fallback: מקסימום year
                int maxYear = -1;
                for (int i = 0; i < seasons.length(); i++) {
                    int y = seasons.getJSONObject(i).optInt("year", -1);
                    if (y > maxYear) maxYear = y;
                }
                if (maxYear > 0) return maxYear;

                throw new IOException("No seasons found for leagueId=" + leagueId);

            } catch (JSONException e) {
                throw new IOException("Bad JSON from API (leagues): " + body, e);
            }
        }
    }

    // ------------------------------------------------------------
    // 2) משחקים עתידיים: fixtures?league&season&status=NS&next
    // status=NS = Not Started (מומלץ כדי לקבל עתידיים בלבד)
    // ------------------------------------------------------------
    public List<Game> getNextFixtures(int leagueId, int seasonStartYear, int next) throws IOException {
        String url = BASE_URL + "fixtures?league=" + leagueId
                + "&season=" + seasonStartYear
                + "&status=NS"
                + "&next=" + next
                + "&timezone=" + encode("Asia/Jerusalem");

        return fetchFixtures(url);
    }

    // ------------------------------------------------------------
    // 3) Fallback לפי טווח תאריכים (אם next מחזיר 0)
    // from/to בפורמט YYYY-MM-DD
    // ------------------------------------------------------------
    public List<Game> getFixturesInRange(int leagueId, int seasonStartYear, String fromIso, String toIso) throws IOException {
        String url = BASE_URL + "fixtures?league=" + leagueId
                + "&season=" + seasonStartYear
                + "&from=" + encode(fromIso)
                + "&to=" + encode(toIso)
                + "&timezone=" + encode("Asia/Jerusalem");

        return fetchFixtures(url);
    }

    // ------------------------------------------------------------
    // 4) בדיקת תוצאה סופית למשחק: fixtures?id=...
    // מחזיר null אם לא הסתיים
    // ------------------------------------------------------------
    public MatchResult getFinalResultIfFinished(int fixtureId) throws IOException {
        String url = BASE_URL + "fixtures?id=" + fixtureId;

        Request req = baseRequest(url).get().build();
        try (Response res = http.newCall(req).execute()) {
            String body = (res.body() != null) ? res.body().string() : "";
            debugLog(url, body);

            if (!res.isSuccessful()) throw new IOException("HTTP " + res.code() + " => " + body);

            try {
                JSONObject root = new JSONObject(body);
                JSONArray response = root.getJSONArray("response");
                if (response.length() == 0) return null;

                JSONObject item = response.getJSONObject(0);
                JSONObject fixture = item.getJSONObject("fixture");
                JSONObject status = fixture.getJSONObject("status");
                String shortStatus = status.optString("short", "");

                // FT/AET/PEN = Finished states (מקובל אצל API-Football)
                boolean finished = shortStatus.equals("FT")
                        || shortStatus.equals("AET")
                        || shortStatus.equals("PEN");
                if (!finished) return null;

                JSONObject goals = item.getJSONObject("goals");
                int homeGoals = goals.isNull("home") ? 0 : goals.getInt("home");
                int awayGoals = goals.isNull("away") ? 0 : goals.getInt("away");

                return new MatchResult(homeGoals, awayGoals, shortStatus);

            } catch (JSONException e) {
                throw new IOException("Bad JSON from API (result): " + body, e);
            }
        }
    }

    // ------------------------------------------------------------
    // פנימי: הבאת fixtures + parse
    // ------------------------------------------------------------
    private List<Game> fetchFixtures(String url) throws IOException {
        Request req = baseRequest(url).get().build();
        try (Response res = http.newCall(req).execute()) {
            String body = (res.body() != null) ? res.body().string() : "";
            debugLog(url, body);

            if (!res.isSuccessful()) throw new IOException("HTTP " + res.code() + " => " + body);

            try {
                JSONObject root = new JSONObject(body);
                JSONArray response = root.getJSONArray("response");

                List<Game> games = new ArrayList<>();
                for (int i = 0; i < response.length(); i++) {
                    JSONObject item = response.getJSONObject(i);

                    JSONObject fixture = item.getJSONObject("fixture");
                    int fixtureId = fixture.getInt("id");
                    long kickoffTs = fixture.getLong("timestamp");

                    JSONObject teams = item.getJSONObject("teams");
                    String home = teams.getJSONObject("home").getString("name");
                    String away = teams.getJSONObject("away").getString("name");

                    // odds כרגע לא חובה (תוכל להוסיף בהמשך מ-odds endpoint)
                    games.add(new Game(fixtureId, home, away, kickoffTs, -1, -1, -1));
                }
                return games;

            } catch (JSONException e) {
                throw new IOException("Bad JSON from API (fixtures): " + body, e);
            }
        }
    }

    // ------------------------------------------------------------
    // תוצאה סופית
    // ------------------------------------------------------------
    public static class MatchResult {
        public final int home;
        public final int away;
        public final String status;

        public MatchResult(int home, int away, String status) {
            this.home = home;
            this.away = away;
            this.status = status;
        }
    }
}
