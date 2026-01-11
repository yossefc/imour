package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    // Premier League = 39 (recommandé pour tests)
    // Israel Premier = 383
    private static final int LEAGUE_ID = ApiFootballClient.PREMIER_LEAGUE_ID;

    private RecyclerView gamesRecyclerView;
    private GameAdapter adapter;
    private final List<Game> games = new ArrayList<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private TextView titleTextView;
    private TextView balanceTextView;
    private ProgressBar loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Init views
        gamesRecyclerView = findViewById(R.id.gamesRecyclerView);
        gamesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new GameAdapter(this, games);
        gamesRecyclerView.setAdapter(adapter);

        titleTextView = findViewById(R.id.titleTextView);
        balanceTextView = findViewById(R.id.balanceTextView);
        loadingBar = findViewById(R.id.loadingBar);

        if (balanceTextView != null) {
            balanceTextView.setText("יתרה: 1000 (דמו)");
        }

        Button openMyBetsButton = findViewById(R.id.openMyBetsButton);
        if (openMyBetsButton != null) {
            openMyBetsButton.setOnClickListener(v ->
                    startActivity(new Intent(this, MyBetsActivity.class)));
        }

        // Vérifier la clé API
        if (BuildConfig.API_FOOTBALL_KEY == null || BuildConfig.API_FOOTBALL_KEY.trim().isEmpty()) {
            showError("❌ חסר API Key!");
            return;
        }

        Log.d(TAG, "🔑 API Key: " + BuildConfig.API_FOOTBALL_KEY.substring(0, 8) + "...");

        loadFixtures();
    }

    private void showLoading(boolean show) {
        if (loadingBar != null) {
            loadingBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    private void showError(String message) {
        showLoading(false);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        Log.e(TAG, message);
        if (titleTextView != null) {
            titleTextView.setText("❌ שגיאה");
        }
    }

    private void loadFixtures() {
        showLoading(true);
        updateTitle("מתחבר...");

        executor.execute(() -> {
            try {
                ApiFootballClient api = new ApiFootballClient();

                // 1. Test connexion
                Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                Log.d(TAG, "📡 Step 1: Testing API connection...");
                updateTitle("בודק חיבור...");

                if (!api.testApiConnection()) {
                    runOnUiThread(() -> showError("❌ החיבור נכשל!"));
                    return;
                }

                // 2. Utiliser la saison 2024 (FREE PLAN = 2022-2024 seulement!)
                Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                Log.d(TAG, "📅 Step 2: Using season 2024 (free plan limit)...");
                updateTitle("טוען עונת 2024...");

                // FREE PLAN: Only seasons 2022-2024 are available!
                int season = 2024;
                String seasonLabel = "2024/2025 (Free Plan)";

                // 3. Charger les matchs
                Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                Log.d(TAG, "⚽ Step 3: Loading fixtures...");
                updateTitle("טוען משחקים...");

                List<Game> result = api.getUpcomingFixtures(LEAGUE_ID, season);

                Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                Log.d(TAG, "📊 FINAL RESULT: " + result.size() + " games");

                // 4. Mettre à jour l'UI
                final List<Game> finalResult = result;
                runOnUiThread(() -> {
                    showLoading(false);

                    games.clear();
                    games.addAll(result);
                    adapter.notifyDataSetChanged();

                    String title = "Premier League " + seasonLabel;
                    if (titleTextView != null) {
                        titleTextView.setText(title);
                    }

                    if (result.isEmpty()) {
                        Toast.makeText(this,
                                "❌ לא נמצאו משחקים!\nבדוק Logcat לפרטים",
                                Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this,
                                "✅ נטענו " + result.size() + " משחקים!",
                                Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception e) {
                Log.e(TAG, "❌ Error: " + e.getMessage(), e);
                runOnUiThread(() -> showError("שגיאה: " + e.getMessage()));
            }
        });
    }

    private void updateTitle(String text) {
        runOnUiThread(() -> {
            if (titleTextView != null) {
                titleTextView.setText(text);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }
}