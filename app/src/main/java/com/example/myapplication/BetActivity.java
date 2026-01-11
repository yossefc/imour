package com.example.myapplication;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class BetActivity extends AppCompatActivity {

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bet);

        db = FirebaseFirestore.getInstance();

        TextView betDetailsTextView = findViewById(R.id.betDetailsTextView);
                EditText betStakeEditText = findViewById(R.id.betStakeEditText);
        Button btnSaveBet = findViewById(R.id.btnSaveBet);

        int fixtureId = getIntent().getIntExtra("fixtureId", -1);
        long kickoffTs = getIntent().getLongExtra("kickoffTs", 0);

        String team1 = getIntent().getStringExtra("team1");
        String team2 = getIntent().getStringExtra("team2");
        String betType = getIntent().getStringExtra("betType"); // "1"/"X"/"2"
        double odds = 2.0;
        String betDetails =
                "משחק: " + team1 + " vs " + team2 + "\n" +
                        "בחירה: " + betType + "\n" +
                                    "יחס קבוע: " + odds + "\n" +         
            "fixtureId: " + fixtureId;

        betDetailsTextView.setText(betDetails);

        btnSaveBet.setOnClickListener(v -> {
            if (fixtureId <= 0 || team1 == null || team2 == null || betType == null) {
                Toast.makeText(this, "חסר מידע על המשחק", Toast.LENGTH_LONG).show();
                return;
            }
            String stakeText = betStakeEditText.getText() != null
                    ? betStakeEditText.getText().toString().trim()
                    : "";
            double stake;
            try {
                stake = Double.parseDouble(stakeText);
            } catch (NumberFormatException e) {
                stake = 0;
            }
            if (stake <= 0) {
                Toast.makeText(this, "אנא הזן סכום הימור תקין", Toast.LENGTH_LONG).show();
                return;
            }

            String userId = UserIdProvider.getOrCreate(this);

            Map<String, Object> bet = new HashMap<>();
            bet.put("userId", userId);
            bet.put("fixtureId", fixtureId);
            bet.put("homeTeam", team1);
            bet.put("awayTeam", team2);
            bet.put("kickoffTs", kickoffTs);
            bet.put("pick", betType);
                      bet.put("stake", stake);
            bet.put("odds", odds);
            bet.put("status", "PENDING");
            bet.put("createdAt", Timestamp.now());

            db.collection("bets")
                    .add(bet)
                    .addOnSuccessListener(doc -> {
                        Toast.makeText(this, "נשמר בהצלחה!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(err ->
                            Toast.makeText(this, "שגיאה בשמירה: " + err.getMessage(), Toast.LENGTH_LONG).show()
                    );
        });
    }
}
