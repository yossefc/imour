package com.example.myapplication;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ApiFootballClient {

    private static final String TAG = "ApiFootballClient";
    private static final String BASE_URL = "https://v3.football.api-sports.io/";

    public static final int PREMIER_LEAGUE_ID = 39;
    public static final int ISRAEL_PREMIER_ID = 383;

    private final OkHttpClient http;

    public ApiFootballClient() {
        this.http = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    private Request.Builder baseRequest(String url) {
        return new Request.Builder()
                .url(url)
                .addHeader("x-apisports-key", BuildConfig.API_FOOTBALL_KEY);
    }

    private static String encode(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (Exception e) {
            return s;
        }
    }

    // ============================================================
    // Test connexion API
    // ============================================================
    public boolean testApiConnection() {
        String url = BASE_URL + "status";
        Log.d(TAG, "🔍 Testing API: " + url);

        try {
            Request req = baseRequest(url).get().build();
            try (Response res = http.newCall(req).execute()) {
                String body = (res.body() != null) ? res.body().string() : "";
                Log.d(TAG, "📥 Status response: " + body.substring(0, Math.min(300, body.length())));

                if (!res.isSuccessful()) {
                    Log.e(TAG, "❌ HTTP " + res.code());
                    return false;
                }

                JSONObject root = new JSONObject(body);
                JSONObject response = root.optJSONObject("response");
                if (response != null) {
                    JSONObject requests = response.optJSONObject("requests");
                    if (requests != null) {
                        int current = requests.optInt("current");
                        int limit = requests.optInt("limit_day");
                        Log.d(TAG, "📈 Quota: " + current + "/" + limit);
                    }
                }
                Log.d(TAG, "✅ API OK!");
                return true;
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Error: " + e.getMessage(), e);
            return false;
        }
    }

    // ============================================================
    // Obtenir la saison actuelle
    // ============================================================
    public int getCurrentSeasonStartYear(int leagueId) throws IOException {
        String url = BASE_URL + "leagues?id=" + leagueId;
        Log.d(TAG, "📅 Getting season for league " + leagueId);

        Request req = baseRequest(url).get().build();
        try (Response res = http.newCall(req).execute()) {
            String body = (res.body() != null) ? res.body().string() : "";
            Log.d(TAG, "📥 Leagues response: " + body.substring(0, Math.min(500, body.length())));

            if (!res.isSuccessful()) throw new IOException("HTTP " + res.code());

            JSONObject root = new JSONObject(body);
            JSONArray response = root.getJSONArray("response");
            if (response.length() == 0) throw new IOException("No league found");

            JSONObject leagueObj = response.getJSONObject(0);
            JSONArray seasons = leagueObj.getJSONArray("seasons");

            // Chercher current=true
            for (int i = 0; i < seasons.length(); i++) {
                JSONObject s = seasons.getJSONObject(i);
                if (s.optBoolean("current", false)) {
                    int year = s.getInt("year");
                    Log.d(TAG, "✅ Current season: " + year);
                    return year;
                }
            }

            // Fallback: année max
            int maxYear = -1;
            for (int i = 0; i < seasons.length(); i++) {
                int y = seasons.getJSONObject(i).optInt("year", -1);
                if (y > maxYear) maxYear = y;
            }
            Log.d(TAG, "⚠️ Fallback season: " + maxYear);
            return maxYear;

        } catch (JSONException e) {
            throw new IOException("JSON error", e);
        }
    }

    // ============================================================
    // MÉTHODE PRINCIPALE - Pour FREE PLAN (season 2024)
    // ============================================================
    public List<Game> getUpcomingFixtures(int leagueId, int season) throws IOException {
        List<Game> games;

        // Pour le plan gratuit avec saison 2024, on récupère les derniers matchs de la saison
        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        Log.d(TAG, "🔍 Fetching fixtures for season " + season + " (FREE PLAN)...");

        // Stratégie: Récupérer les matchs de mai 2025 (fin de saison 2024)
        // La saison 2024/2025 se termine en mai 2025
        games = fetchWithDateRange(leagueId, season, "2025-04-01", "2025-05-31");

        if (!games.isEmpty()) {
            Log.d(TAG, "✅ Found " + games.size() + " games!");
            // Retourner les 20 derniers
            int start = Math.max(0, games.size() - 20);
            return games.subList(start, games.size());
        }

        // Fallback: tous les matchs de la saison
        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        Log.d(TAG, "🔍 Fallback: All season fixtures...");
        games = fetchAllSeason(leagueId, season);
        Log.d(TAG, "📊 Found " + games.size() + " total games");

        // Retourner les 20 derniers
        int start = Math.max(0, games.size() - 20);
        return games.subList(start, games.size());
    }

    // ============================================================
    // Stratégie 1: Avec "next"
    // ============================================================
    private List<Game> fetchWithNext(int leagueId, int season, int next) throws IOException {
        String url = BASE_URL + "fixtures?league=" + leagueId
                + "&season=" + season
                + "&next=" + next
                + "&timezone=" + encode("Asia/Jerusalem");

        return fetchFixtures(url);
    }

    // ============================================================
    // Stratégie 2/3: Avec plage de dates spécifiques
    // ============================================================
    private List<Game> fetchWithDateRange(int leagueId, int season, String from, String to) throws IOException {
        String url = BASE_URL + "fixtures?league=" + leagueId
                + "&season=" + season
                + "&from=" + from
                + "&to=" + to
                + "&timezone=" + encode("Asia/Jerusalem");

        Log.d(TAG, "📅 Date range: " + from + " → " + to);
        return fetchFixtures(url);
    }

    // Avec nombre de jours (pour compatibilité)
    private List<Game> fetchWithDateRangeDays(int leagueId, int season, int days) throws IOException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        Calendar cal = Calendar.getInstance();
        String from = sdf.format(cal.getTime());
        cal.add(Calendar.DAY_OF_YEAR, days);
        String to = sdf.format(cal.getTime());

        return fetchWithDateRange(leagueId, season, from, to);
    }

    // ============================================================
    // Stratégie 4: Tous les matchs de la saison
    // ============================================================
    private List<Game> fetchAllSeason(int leagueId, int season) throws IOException {
        String url = BASE_URL + "fixtures?league=" + leagueId
                + "&season=" + season
                + "&timezone=" + encode("Asia/Jerusalem");

        return fetchFixtures(url);
    }

    // ============================================================
    // Fetch générique
    // ============================================================
    private List<Game> fetchFixtures(String url) throws IOException {
        Log.d(TAG, "🌐 URL: " + url);

        Request req = baseRequest(url).get().build();
        try (Response res = http.newCall(req).execute()) {
            String body = (res.body() != null) ? res.body().string() : "";

            // Log le début de la réponse
            Log.d(TAG, "📥 Response (" + body.length() + " chars): " +
                    body.substring(0, Math.min(400, body.length())));

            if (!res.isSuccessful()) {
                Log.e(TAG, "❌ HTTP " + res.code());
                throw new IOException("HTTP " + res.code());
            }

            JSONObject root = new JSONObject(body);

            // Vérifier erreurs API
            JSONObject errors = root.optJSONObject("errors");
            if (errors != null && errors.length() > 0) {
                Log.e(TAG, "❌ API Errors: " + errors.toString());
            }

            int results = root.optInt("results", 0);
            Log.d(TAG, "📊 Results count from API: " + results);

            JSONArray response = root.getJSONArray("response");
            List<Game> games = new ArrayList<>();

            for (int i = 0; i < response.length(); i++) {
                JSONObject item = response.getJSONObject(i);
                JSONObject fixture = item.getJSONObject("fixture");
                JSONObject teams = item.getJSONObject("teams");

                int fixtureId = fixture.getInt("id");
                long kickoffTs = fixture.getLong("timestamp");
                String home = teams.getJSONObject("home").getString("name");
                String away = teams.getJSONObject("away").getString("name");

                JSONObject statusObj = fixture.optJSONObject("status");
                String status = (statusObj != null) ? statusObj.optString("short", "?") : "?";

                games.add(new Game(fixtureId, home, away, kickoffTs, -1, -1, -1));

                // Log les premiers matchs
                if (i < 5) {
                    String date = new SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())
                            .format(new java.util.Date(kickoffTs * 1000));
                    Log.d(TAG, "⚽ " + date + " | " + home + " vs " + away + " [" + status + "]");
                }
            }

            if (response.length() > 5) {
                Log.d(TAG, "⚽ ... and " + (response.length() - 5) + " more games");
            }

            return games;

        } catch (JSONException e) {
            Log.e(TAG, "❌ JSON Error", e);
            throw new IOException("JSON error", e);
        }
    }

    // ============================================================
    // Vérifier résultat d'un match
    // ============================================================
    public MatchResult getFinalResultIfFinished(int fixtureId) throws IOException {
        String url = BASE_URL + "fixtures?id=" + fixtureId;

        Request req = baseRequest(url).get().build();
        try (Response res = http.newCall(req).execute()) {
            String body = (res.body() != null) ? res.body().string() : "";
            if (!res.isSuccessful()) throw new IOException("HTTP " + res.code());

            JSONObject root = new JSONObject(body);
            JSONArray response = root.getJSONArray("response");
            if (response.length() == 0) return null;

            JSONObject item = response.getJSONObject(0);
            JSONObject fixture = item.getJSONObject("fixture");
            JSONObject status = fixture.getJSONObject("status");
            String shortStatus = status.optString("short", "");

            boolean finished = shortStatus.equals("FT") || shortStatus.equals("AET") || shortStatus.equals("PEN");
            if (!finished) return null;

            JSONObject goals = item.getJSONObject("goals");
            int homeGoals = goals.isNull("home") ? 0 : goals.getInt("home");
            int awayGoals = goals.isNull("away") ? 0 : goals.getInt("away");

            return new MatchResult(homeGoals, awayGoals, shortStatus);

        } catch (JSONException e) {
            throw new IOException("JSON error", e);
        }
    }

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