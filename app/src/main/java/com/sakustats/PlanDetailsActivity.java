package com.sakustats;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class PlanDetailsActivity extends AppCompatActivity {

    public static final String EXTRA_PLAN_ID = "extra_plan_id";
    private static final int REQUEST_EDIT_PLAN = 101;
    private Plan plan;
    private View btnAddFunds;
    private View btnWithdraw;
    private View btnEdit;
    private View btnDelete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan_details);

        loadPlan();

        if (plan == null) {
            finish();
            return;
        }

        initViews();
    }

    private void loadPlan() {
        long planId = getIntent().getLongExtra(EXTRA_PLAN_ID, -1);
        for (Plan p : Plan.DATA) {
            if (p.getId() == planId) {
                plan = p;
                break;
            }
        }
    }

    private void initViews() {
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        btnAddFunds = findViewById(R.id.btn_add_funds);
        btnWithdraw = findViewById(R.id.btn_withdraw);
        btnEdit = findViewById(R.id.btn_edit);
        btnDelete = findViewById(R.id.btn_delete);

        updateUI();

        btnAddFunds.setOnClickListener(v -> {
            showAddFundsDialog();
        });

        btnWithdraw.setOnClickListener(v -> {
            showWithdrawDialog();
        });

        btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddPlanActivity.class);
            intent.putExtra(EXTRA_PLAN_ID, plan.getId());
            startActivityForResult(intent, REQUEST_EDIT_PLAN);
        });

        btnDelete.setOnClickListener(v -> {
            showDeleteConfirmation();
        });
    }

    private void updateUI() {
        ((TextView) findViewById(R.id.tv_plan_title)).setText(plan.getTitle());
        ((TextView) findViewById(R.id.tv_category)).setText(plan.getCategory());
        ((ImageView) findViewById(R.id.iv_plan_icon)).setImageResource(plan.getIconResId());

        String amountProgress = "Rp " + Transaction.formatRupiah(plan.getCurrentAmount()) + " / Rp " + Transaction.formatRupiah(plan.getGoalAmount());
        ((TextView) findViewById(R.id.tv_amount_progress)).setText(amountProgress);

        ProgressBar pb = findViewById(R.id.pb_progress);
        pb.setProgress(plan.getProgress());

        String infoText;
        if ("monthly".equals(plan.getSavingFrequency())) {
            infoText = "Remaining " + plan.getRemainingMonths() + " months till you reach your goal";
        } else {
            infoText = "Remaining " + plan.getRemainingDays() + " days till you reach your goal";
        }
        ((TextView) findViewById(R.id.tv_info_text)).setText(infoText);

        ((TextView) findViewById(R.id.tv_required_funds)).setText("Rp " + Transaction.formatRupiah(plan.getGoalAmount()));

        setupDetailRow(findViewById(R.id.row_category), "Categories", plan.getCategory());
        setupDetailRow(findViewById(R.id.row_collected), "Collected Funds", "Rp " + Transaction.formatRupiah(plan.getCurrentAmount()));

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
        setupDetailRow(findViewById(R.id.row_date), "Achievement Date", sdf.format(plan.getAchievementDate()));
        
        if ("monthly".equals(plan.getSavingFrequency())) {
            setupDetailRow(findViewById(R.id.row_remaining), "Remaining Time", plan.getRemainingMonths() + " Months");
            setupDetailRow(
                    findViewById(R.id.row_daily_target),
                    "Monthly Saving Target",
                    "Rp " + Transaction.formatRupiah(plan.getSavingTarget())
            );
        } else {
            setupDetailRow(findViewById(R.id.row_remaining), "Remaining Time", plan.getRemainingDays() + " Days");
            setupDetailRow(
                    findViewById(R.id.row_daily_target),
                    "Daily Saving Target",
                    "Rp " + Transaction.formatRupiah(plan.getSavingTarget())
            );
        }
    }

    private void setupDetailRow(View rowView, String label, String value) {
        ((TextView) rowView.findViewById(R.id.tv_label)).setText(label);
        ((TextView) rowView.findViewById(R.id.tv_value)).setText(value);
    }

    private void updatePlanInFirebase() {
        String uid = FirebaseHelper.auth.getCurrentUser().getUid();
        FirebaseHelper.db
                .collection("users")
                .document(uid)
                .collection("plans")
                .document(plan.getDocumentId())
                .set(plan);
    }

    private void showDeleteConfirmation() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Hapus Plan")
                .setMessage("Apakah Anda yakin ingin menghapus plan ini?")
                .setPositiveButton("Hapus", (dialog, which) -> {
                    deletePlanFromFirebase();
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private void deletePlanFromFirebase() {
        String uid = FirebaseHelper.auth.getCurrentUser().getUid();
        FirebaseHelper.db
                .collection("users")
                .document(uid)
                .collection("plans")
                .document(plan.getDocumentId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Plan.DATA.remove(plan);
                    Toast.makeText(this, "Plan berhasil dihapus", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Gagal menghapus plan: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showAddFundsDialog() {
        android.widget.EditText input = new android.widget.EditText(this);
        input.setHint("Masukkan jumlah");
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Tambah Dana")
                .setView(input)
                .setPositiveButton("Tambah", (dialog, which) -> {
                    String value = input.getText().toString();
                    if (!value.isEmpty()) {
                        long amount = Long.parseLong(value);
                        plan.setCurrentAmount(plan.getCurrentAmount() + amount);
                        plan.setLastSavingDate(new java.util.Date());
                        updatePlanInFirebase();
                        updateUI();
                        Toast.makeText(this, "Dana berhasil ditambahkan", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private void showWithdrawDialog() {
        android.widget.EditText input = new android.widget.EditText(this);
        input.setHint("Masukkan jumlah");
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Tarik Dana")
                .setView(input)
                .setPositiveButton("Tarik", (dialog, which) -> {
                    String value = input.getText().toString();
                    if (!value.isEmpty()) {
                        long amount = Long.parseLong(value);
                        long current = plan.getCurrentAmount();
                        if (amount > current) {
                            Toast.makeText(this, "Saldo tidak cukup", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        plan.setCurrentAmount(current - amount);
                        updatePlanInFirebase();
                        updateUI();
                        Toast.makeText(this, "Dana berhasil ditarik", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_EDIT_PLAN && resultCode == RESULT_OK) {
            updateUI();
        }
    }
}
