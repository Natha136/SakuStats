package com.sakustats;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddPlanActivity extends AppCompatActivity {

    private EditText etPlanName, etAmount;
    private Spinner spinnerCategory;
    private TextView tvDate;
    private TextView tvSavingsInfo;
    private View llInfo;
    private RadioGroup rgFrequency;
    private Calendar selectedDate = Calendar.getInstance();
    private Plan existingPlan;

    private final String[] categories = {"Education", "Vacation", "Emergency Fund", "Gadget", "Vehicle", "Housing", "Others"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_plan);

        etPlanName = findViewById(R.id.et_plan_name);
        spinnerCategory = findViewById(R.id.spinner_category);
        etAmount = findViewById(R.id.et_amount);
        tvDate = findViewById(R.id.tv_date);
        tvSavingsInfo = findViewById(R.id.tv_savings_info);
        llInfo = findViewById(R.id.ll_info);
        rgFrequency = findViewById(R.id.rg_frequency);

        setupCategorySpinner();

        long planId = getIntent().getLongExtra(PlanDetailsActivity.EXTRA_PLAN_ID, -1);
        if (planId != -1) {
            for (Plan p : Plan.DATA) {
                if (p.getId() == planId) {
                    existingPlan = p;
                    break;
                }
            }
        }

        if (existingPlan != null) {
            ((TextView) findViewById(R.id.tv_toolbar_title)).setText("Edit Plan");
            etPlanName.setText(existingPlan.getTitle());
            etAmount.setText(String.valueOf(existingPlan.getGoalAmount()));
            
            int categoryIndex = 0;
            for (int i = 0; i < categories.length; i++) {
                if (categories[i].equals(existingPlan.getCategory())) {
                    categoryIndex = i;
                    break;
                }
            }
            spinnerCategory.setSelection(categoryIndex);
            
            selectedDate.setTime(existingPlan.getAchievementDate());
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
            tvDate.setText(sdf.format(selectedDate.getTime()));
            
            if ("monthly".equals(existingPlan.getSavingFrequency())) {
                rgFrequency.check(R.id.rb_monthly);
            } else {
                rgFrequency.check(R.id.rb_daily);
            }
            
            updateSavingsInfo();
        }

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_cancel).setOnClickListener(v -> finish());

        findViewById(R.id.btn_select_date).setOnClickListener(v -> showDatePicker());

        etAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateSavingsInfo();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        rgFrequency.setOnCheckedChangeListener((group, checkedId) -> updateSavingsInfo());

        findViewById(R.id.btn_finish).setOnClickListener(v -> savePlan());
    }

    private void setupCategorySpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);
    }

    private void showDatePicker() {
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            selectedDate.set(Calendar.YEAR, year);
            selectedDate.set(Calendar.MONTH, month);
            selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
            tvDate.setText(sdf.format(selectedDate.getTime()));
            updateSavingsInfo();
        }, selectedDate.get(Calendar.YEAR), selectedDate.get(Calendar.MONTH), selectedDate.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void updateSavingsInfo() {
        String amountStr = etAmount.getText().toString();
        if (amountStr.isEmpty() || tvDate.getText().toString().equals("Select Date")) {
            llInfo.setVisibility(View.GONE);
            return;
        }

        try {
            long amount = Long.parseLong(amountStr);
            long diffMillis = selectedDate.getTimeInMillis() - System.currentTimeMillis();
            
            if (diffMillis <= 0) {
                llInfo.setVisibility(View.GONE);
                return;
            }

            if (rgFrequency.getCheckedRadioButtonId() == R.id.rb_monthly) {
                Calendar start = Calendar.getInstance();
                Calendar end = Calendar.getInstance();
                end.setTime(selectedDate.getTime());
                int diffYear = end.get(Calendar.YEAR) - start.get(Calendar.YEAR);
                int diffMonth = diffYear * 12 + end.get(Calendar.MONTH) - start.get(Calendar.MONTH);
                
                if (diffMonth <= 0) {
                    tvSavingsInfo.setText("To achieve your goal, you need to save Rp " + Transaction.formatRupiah(amount) + " this month");
                } else {
                    long monthly = amount / diffMonth;
                    tvSavingsInfo.setText("To achieve your goal, you need to save at least Rp " + Transaction.formatRupiah(monthly) + " each month (" + diffMonth + " months remaining)");
                }
            } else {
                long days = diffMillis / (1000 * 60 * 60 * 24);
                if (days <= 0) {
                    tvSavingsInfo.setText("To achieve your goal, you need to save Rp " + Transaction.formatRupiah(amount) + " today");
                } else {
                    long daily = amount / days;
                    tvSavingsInfo.setText("To achieve your goal, you need to save at least Rp " + Transaction.formatRupiah(daily) + " each day (" + days + " days remaining)");
                }
            }
            llInfo.setVisibility(View.VISIBLE);
        } catch (NumberFormatException e) {
            llInfo.setVisibility(View.GONE);
        }
    }

    private void savePlan() {
        String planName = etPlanName.getText().toString().trim();
        String amountStr = etAmount.getText().toString();
        String category = spinnerCategory.getSelectedItem().toString();
        String frequency = rgFrequency.getCheckedRadioButtonId() == R.id.rb_monthly ? "monthly" : "daily";

        if (planName.isEmpty()) {
            Toast.makeText(this, "Please enter plan name", Toast.LENGTH_SHORT).show();
            return;
        }

        if (amountStr.isEmpty()) {
            Toast.makeText(this, "Please enter amount", Toast.LENGTH_SHORT).show();
            return;
        }

        if (tvDate.getText().toString().equals("Select Date")) {
            Toast.makeText(this, "Please select date", Toast.LENGTH_SHORT).show();
            return;
        }

        long amount = Long.parseLong(amountStr);

        if (existingPlan != null) {
            existingPlan.setTitle(planName);
            existingPlan.setCategory(category);
            existingPlan.setGoalAmount(amount);
            existingPlan.setAchievementDate(selectedDate.getTime());
            existingPlan.setSavingFrequency(frequency);
            updatePlanInFirebase(existingPlan);
        } else {
            Plan plan = new Plan(planName, category, amount, 0, selectedDate.getTime(), R.drawable.ic_piggy);
            plan.setSavingFrequency(frequency);
            savePlanToFirebase(plan);
        }
    }

    private void savePlanToFirebase(Plan plan) {
        String uid = FirebaseHelper.auth.getCurrentUser().getUid();
        FirebaseHelper.db
                .collection("users")
                .document(uid)
                .collection("plans")
                .add(plan)
                .addOnSuccessListener(documentReference -> {
                    plan.setDocumentId(documentReference.getId());
                    Plan.DATA.add(plan);
                    Toast.makeText(this, "Plan berhasil disimpan", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                });
    }

    private void updatePlanInFirebase(Plan plan) {
        String uid = FirebaseHelper.auth.getCurrentUser().getUid();
        FirebaseHelper.db
                .collection("users")
                .document(uid)
                .collection("plans")
                .document(plan.getDocumentId())
                .set(plan)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Plan berhasil diperbarui", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Gagal memperbarui plan: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
