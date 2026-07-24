package com.sakustats;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class NotificationActivity extends AppCompatActivity {

    private RecyclerView rvNotifications;
    private NotificationAdapter adapter;
    private List<PlanNotification> notificationList = new ArrayList<>();
    private View llEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        rvNotifications = findViewById(R.id.rv_notifications);
        llEmpty = findViewById(R.id.ll_empty);
        ImageView btnBack = findViewById(R.id.btn_back);

        btnBack.setOnClickListener(v -> finish());

        setupRecyclerView();
        loadNotificationsFromFirebase();
    }

    private void setupRecyclerView() {
        adapter = new NotificationAdapter(notificationList);
        rvNotifications.setLayoutManager(new LinearLayoutManager(this));
        rvNotifications.setAdapter(adapter);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                deleteNotification(notificationList.get(position), position);
            }
        });
        itemTouchHelper.attachToRecyclerView(rvNotifications);
    }

    private void loadNotificationsFromFirebase() {
        String uid = FirebaseHelper.auth.getCurrentUser().getUid();
        FirebaseHelper.db.collection("users").document(uid).collection("notifications")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    notificationList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        PlanNotification notif = doc.toObject(PlanNotification.class);
                        if (notif != null) {
                            notif.setId(doc.getId());
                            notificationList.add(notif);
                        }
                    }
                    
                    // If empty, generate some initial notifications based on current plans
                    if (notificationList.isEmpty()) {
                        generateInitialNotifications();
                    } else {
                        updateUI();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Gagal memuat notifikasi", Toast.LENGTH_SHORT).show();
                });
    }

    private void generateInitialNotifications() {
        String uid = FirebaseHelper.auth.getCurrentUser().getUid();
        List<PlanNotification> newNotifs = new ArrayList<>();

        for (Plan plan : Plan.DATA) {
            if (!plan.hasSavedToday()) {
                newNotifs.add(new PlanNotification(null, "💰 Reminder Menabung", 
                    "Kamu belum menyisihkan uang untuk " + plan.getTitle() + " hari ini.", 
                    new Date(), plan.getDocumentId(), "reminder"));
            }
            if (plan.getRemainingDays() <= 7 && plan.getRemainingDays() > 0) {
                newNotifs.add(new PlanNotification(null, "⚠️ Deadline Mendekat", 
                    plan.getTitle() + " tinggal " + plan.getRemainingDays() + " hari lagi!", 
                    new Date(), plan.getDocumentId(), "deadline"));
            }
            if (plan.getProgress() >= 100) {
                newNotifs.add(new PlanNotification(null, "🏆 Target Tercapai", 
                    "Selamat! Target " + plan.getTitle() + " sudah tercapai.", 
                    new Date(), plan.getDocumentId(), "achievement"));
            }
        }

        if (newNotifs.isEmpty()) {
            updateUI();
            return;
        }

        for (PlanNotification notif : newNotifs) {
            FirebaseHelper.db.collection("users").document(uid).collection("notifications")
                    .add(notif)
                    .addOnSuccessListener(documentReference -> {
                        notif.setId(documentReference.getId());
                        notificationList.add(notif);
                        updateUI();
                    });
        }
    }

    private void deleteNotification(PlanNotification notif, int position) {
        String uid = FirebaseHelper.auth.getCurrentUser().getUid();
        FirebaseHelper.db.collection("users").document(uid).collection("notifications")
                .document(notif.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    notificationList.remove(position);
                    adapter.notifyItemRemoved(position);
                    updateUI();
                })
                .addOnFailureListener(e -> {
                    adapter.notifyItemChanged(position);
                    Toast.makeText(this, "Gagal menghapus notifikasi", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateUI() {
        if (notificationList.isEmpty()) {
            llEmpty.setVisibility(View.VISIBLE);
            rvNotifications.setVisibility(View.GONE);
        } else {
            llEmpty.setVisibility(View.GONE);
            rvNotifications.setVisibility(View.VISIBLE);
            adapter.notifyDataSetChanged();
        }
    }
}
