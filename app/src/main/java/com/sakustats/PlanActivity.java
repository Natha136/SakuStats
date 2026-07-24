package com.sakustats;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class PlanActivity extends AppCompatActivity {

    private LinearLayout llPlanList;
    private LinearLayout llEmptyState;
    private View btnCreatePlanBottom;
    private ImageView ivProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan);
        getOnBackPressedDispatcher().addCallback(this,
                new androidx.activity.OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {

                        Intent intent = new Intent(
                                PlanActivity.this,
                                MainActivity.class
                        );

                        startActivity(intent);
                        finish();
                    }
                });

        initViews();
        setupBottomNavigation();
        setupClickListeners();
        loadPlans();
        loadProfileImage();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPlans();
        loadProfileImage();
    }

    private void initViews() {
        llPlanList = findViewById(R.id.ll_plan_list);
        llEmptyState = findViewById(R.id.ll_empty_state);
        btnCreatePlanBottom = findViewById(R.id.btn_create_plan_bottom);
        ivProfile = findViewById(R.id.iv_profile);
    }

    private void setupClickListeners() {
        ivProfile.setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
        });

        findViewById(R.id.iv_notification).setOnClickListener(v -> {
            startActivity(new Intent(this, NotificationActivity.class));
        });

        findViewById(R.id.tv_create_now).setOnClickListener(v -> {
            startActivityForResult(new Intent(this, AddPlanActivity.class), 101);
        });

        btnCreatePlanBottom.setOnClickListener(v -> {
            startActivityForResult(new Intent(this, AddPlanActivity.class), 101);
        });
    }

    private void loadProfileImage() {
        if (FirebaseHelper.auth.getCurrentUser() == null) return;

        String uid = FirebaseHelper.auth.getCurrentUser().getUid();
        FirebaseHelper.db.collection("users").document(uid).get()
                .addOnSuccessListener(document -> {
                    String base64 = document.getString("profileImage");
                    if (base64 != null && !base64.isEmpty()) {
                        byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        ivProfile.setImageBitmap(bitmap);
                    } else {
                        ivProfile.setImageResource(R.drawable.ic_profile);
                    }
                });
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_plan);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, MainActivity.class));
                finish();
                return true;
            }
            if (id == R.id.nav_plan) return true;
            if (id == R.id.nav_history) {
                startActivity(new Intent(this, TransactionHistoryActivity.class));
                finish();
                return true;
            }
            if (id == R.id.nav_statistics) {
                startActivity(new Intent(this, StatisticsActivity.class));
                finish();
                return true;
            }
            return false;
        });
    }

    private void loadPlans() {
        String uid = FirebaseHelper.auth.getCurrentUser().getUid();

        FirebaseHelper.db
                .collection("users")
                .document(uid)
                .collection("plans")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Plan.DATA.clear();
                    for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots) {
                        Plan plan = doc.toObject(Plan.class);
                        if (plan != null) {
                            plan.setDocumentId(doc.getId());
                            Plan.DATA.add(plan);
                        }
                    }
                    refreshUI();
                });
    }

    private void refreshUI() {
        if (Plan.DATA.isEmpty()) {
            llPlanList.setVisibility(View.GONE);
            llEmptyState.setVisibility(View.VISIBLE);
            // We keep the button visible as requested, to replace the FAB functionality
            btnCreatePlanBottom.setVisibility(View.VISIBLE);
        } else {
            llPlanList.setVisibility(View.VISIBLE);
            llEmptyState.setVisibility(View.GONE);
            btnCreatePlanBottom.setVisibility(View.VISIBLE);
            rebuildPlanList();
        }
    }

    private void rebuildPlanList() {
        llPlanList.removeAllViews();
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
        for (Plan plan : Plan.DATA) {
            View itemView = getLayoutInflater().inflate(R.layout.item_plan, llPlanList, false);
            
            ((TextView) itemView.findViewById(R.id.tv_plan_title)).setText(plan.getTitle());
            ((TextView) itemView.findViewById(R.id.tv_plan_subtitle)).setText(plan.getCategory());
            ((TextView) itemView.findViewById(R.id.tv_category)).setText(plan.getCategory());
            
            String amountProgress = "Rp " + Transaction.formatRupiah(plan.getCurrentAmount()) + " / Rp " + Transaction.formatRupiah(plan.getGoalAmount());
            ((TextView) itemView.findViewById(R.id.tv_plan_amount_progress)).setText(amountProgress);
            
            ((android.widget.ProgressBar) itemView.findViewById(R.id.pb_plan_progress)).setProgress(plan.getProgress());
            
            ((TextView) itemView.findViewById(R.id.tv_remaining_time)).setText(plan.getRemainingDays() + " days");
            ((TextView) itemView.findViewById(R.id.tv_achievement_date)).setText(sdf.format(plan.getAchievementDate()));

            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(this, PlanDetailsActivity.class);
                intent.putExtra(PlanDetailsActivity.EXTRA_PLAN_ID, plan.getId());
                startActivity(intent);
            });

            llPlanList.addView(itemView);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            loadPlans();
        }
    }
}
