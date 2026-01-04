package com.example.myapplication;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class MyBetsActivity extends AppCompatActivity {

    private RecyclerView betsRecyclerView;
    private BetAdapter betAdapter;
    private final List<Bet> bets = new ArrayList<>();

    private FirebaseFirestore db;
    private ListenerRegistration reg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_bets);

        db = FirebaseFirestore.getInstance();

        betsRecyclerView = findViewById(R.id.betsRecyclerView);
        betsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        betAdapter = new BetAdapter(this, bets);
        betsRecyclerView.setAdapter(betAdapter);

        listenMyBets();
    }

    private void listenMyBets() {
        String userId = UserIdProvider.getOrCreate(this);

        reg = db.collection("bets")
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((snap, err) -> {
                    if (err != null) {
                        Toast.makeText(this, "שגיאה: " + err.getMessage(), Toast.LENGTH_LONG).show();
                        return;
                    }
                    bets.clear();
                    if (snap != null) {
                        snap.getDocuments().forEach(d -> {
                            Bet b = d.toObject(Bet.class);
                            if (b != null) {
                                b.id = d.getId();
                                bets.add(b);
                            }
                        });
                    }
                    betAdapter.notifyDataSetChanged();
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (reg != null) reg.remove();
    }
}
