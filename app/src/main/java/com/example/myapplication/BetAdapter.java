package com.example.myapplication;

import android.content.Context;
import android.widget.Button;
import android.widget.TextView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class BetAdapter extends RecyclerView.Adapter<BetAdapter.BetVH> {

    private final Context context;
    private final List<Bet> bets;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final ApiFootballClient api = new ApiFootballClient();

    public BetAdapter(Context context, List<Bet> bets) {
        this.context = context;
        this.bets = bets;
    }

    @NonNull
    @Override
    public BetVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.bet_item, parent, false);
        return new BetVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull BetVH h, int position) {
        Bet b = bets.get(position);

        h.title.setText(b.homeTeam + " vs " + b.awayTeam);

        String dateStr = (b.kickoffTs > 0)
                ? new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date(b.kickoffTs * 1000))
                : "לא ידוע";

        String details = "תאריך: " + dateStr +
                "\nבחירה: " + b.pick +
                "\nיחס: " + (b.odds > 0 ? b.odds : "לא זמין");

        if (b.finalHome != null && b.finalAway != null) {
            details += "\nתוצאה: " + b.finalHome + " - " + b.finalAway;
        }

        h.details.setText(details);

        String status = b.status == null ? "PENDING" : b.status;
        h.status.setText("סטטוס: " + status);

        boolean finished = status.equals("WON") || status.equals("LOST");
        h.btnCheck.setEnabled(!finished);

        h.btnCheck.setOnClickListener(v -> {
            if (b.id == null) return;
            h.btnCheck.setEnabled(false);

            Executors.newSingleThreadExecutor().execute(() -> {
                try {
                    ApiFootballClient.MatchResult result = api.getFinalResultIfFinished(b.fixtureId);

                    if (result == null) {
                        runToast("המשחק עדיין לא הסתיים");
                        runEnable(h);
                        return;
                    }

                    String newStatus = computeStatus(b.pick, result.home, result.away);

                    db.collection("bets").document(b.id)
                            .update(
                                    "status", newStatus,
                                    "finalHome", result.home,
                                    "finalAway", result.away
                            )
                            .addOnSuccessListener(x -> runToast("עודכן: " + newStatus))
                            .addOnFailureListener(err -> runToast("שגיאה בעדכון: " + err.getMessage()))
                            .addOnCompleteListener(x -> runEnable(h));

                } catch (Exception e) {
                    runToast("שגיאה בבדיקה: " + e.getMessage());
                    runEnable(h);
                }
            });
        });
    }

    private void runEnable(BetVH h) {
        ((android.app.Activity) context).runOnUiThread(() -> h.btnCheck.setEnabled(true));
    }

    private void runToast(String msg) {
        ((android.app.Activity) context).runOnUiThread(() ->
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
        );
    }

    private String computeStatus(String pick, int home, int away) {
        // pick: "1" home win, "X" draw, "2" away win
        if (pick == null) return "LOST";
        if (home > away) return pick.equals("1") ? "WON" : "LOST";
        if (home == away) return pick.equals("X") ? "WON" : "LOST";
        return pick.equals("2") ? "WON" : "LOST";
    }

    @Override
    public int getItemCount() {
        return bets.size();
    }

    static class BetVH extends RecyclerView.ViewHolder {
        TextView title, details, status;
        Button btnCheck;

        public BetVH(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.betTitleTextView);
            details = itemView.findViewById(R.id.betDetailsTextView);
            status = itemView.findViewById(R.id.betStatusTextView);
            btnCheck = itemView.findViewById(R.id.btnCheckResult);
        }
    }
}
