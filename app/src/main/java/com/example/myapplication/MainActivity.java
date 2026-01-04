package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
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

    // ליגת העל (הרבה פעמים זה 383, אבל אם אצלך זה אחר – נברר דרך /leagues)
    private static final int ISRAEL_PREMIER_LEAGUE_ID = 383;

    private RecyclerView gamesRecyclerView;
    private GameAdapter adapter;
    private final List<Game> games = new ArrayList<>();

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private TextView balanceTextView;
    private Button openMyBetsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gamesRecyclerView = findViewById(R.id.gamesRecyclerView);
        gamesRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new GameAdapter(this, games);
        gamesRecyclerView.setAdapter(adapter);

        balanceTextView = findViewById(R.id.balanceTextView);
        if (balanceTextView != null) {
            balanceTextView.setText("יתרה: 1000 (דמו)");
        }

        openMyBetsButton = findViewById(R.id.openMyBetsButton);
        if (openMyBetsButton != null) {
            openMyBetsButton.setOnClickListener(v ->
                    startActivity(new Intent(MainActivity.this, MyBetsActivity.class))
            );
        }

        loadNextFixtures();
    }

    private void loadNextFixtures() {
        if (BuildConfig.API_FOOTBALL_KEY == null || BuildConfig.API_FOOTBALL_KEY.trim().isEmpty()) {
            Toast.makeText(this, "חסר API_FOOTBALL_KEY", Toast.LENGTH_LONG).show();
            return;
        }

        final int leagueId = ISRAEL_PREMIER_LEAGUE_ID;
        final int next = 30;

        Toast.makeText(this, "טוען משחקים…", Toast.LENGTH_SHORT).show();

        executor.execute(() -> {
            try {
                ApiFootballClient api = new ApiFootballClient();
                int seasonStart = api.getCurrentSeasonStartYear(383); // יחזיר 2025 אצלך
                List<Game> result = api.getNextFixtures(383, seasonStart, 200);

                if (result.isEmpty()) {
                    // fallback בטווח תאריכים (דוגמה)
                    result = api.getFixturesInRange(383, seasonStart, "2026-01-01", "2026-02-28");
                }

                final int seasonFinal = seasonStart;
                final String seasonLabel = seasonFinal + "/" + (seasonFinal + 1); // 2025/2026
                final List<Game> finalResult = result;

                runOnUiThread(() -> {
                    games.clear();
                    games.addAll(finalResult);
                    adapter.notifyDataSetChanged();

                    if (finalResult.isEmpty()) {
                        Toast.makeText(
                                MainActivity.this,
                                "לא נמצאו משחקים עתידיים (leagueId=" + leagueId +
                                        ", season=" + seasonLabel +
                                        ", next=" + next + ").",
                                Toast.LENGTH_LONG
                        ).show();
                    } else {
                        Toast.makeText(
                                MainActivity.this,
                                "נטענו " + finalResult.size() + " משחקים (" + seasonLabel + ")",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });

            } catch (Exception e) {
                Log.e(TAG, "Failed loading fixtures", e);
                runOnUiThread(() ->
                        Toast.makeText(MainActivity.this, "שגיאה: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }
}
