package com.sakustats;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class StatisticsActivity extends AppCompatActivity {

    private Calendar currentWeek;
    private TextView tvWeekRange;
    private BarChart barChart;
    private LinearLayout llIncomeList;
    private LinearLayout llExpenseList;
    private ImageView ivProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);
        getOnBackPressedDispatcher().addCallback(this,
                new androidx.activity.OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {

                        Intent intent = new Intent(
                                StatisticsActivity.this,
                                MainActivity.class
                        );

                        startActivity(intent);
                        finish();
                    }
                });

        initViews();
        setupClickListeners();
        setupBottomNav();
        loadWeekData();
        loadProfileImage();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProfileImage();
    }

    private void initViews() {
        currentWeek = Calendar.getInstance();
        tvWeekRange = findViewById(R.id.tv_week_range);
        barChart = findViewById(R.id.bar_chart);
        llIncomeList = findViewById(R.id.ll_income_list);
        llExpenseList = findViewById(R.id.ll_expense_list);
        ivProfile = findViewById(R.id.iv_profile);
    }

    private void setupClickListeners() {
        ivProfile.setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
        });

        findViewById(R.id.iv_notification).setOnClickListener(v -> {
            startActivity(new Intent(this, NotificationActivity.class));
        });

        ImageButton btnPrev = findViewById(R.id.btn_prev_week);
        ImageButton btnNext = findViewById(R.id.btn_next_week);

        btnPrev.setOnClickListener(v -> {
            currentWeek.add(Calendar.WEEK_OF_YEAR, -1);
            loadWeekData();
        });

        btnNext.setOnClickListener(v -> {
            currentWeek.add(Calendar.WEEK_OF_YEAR, 1);
            loadWeekData();
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

    private void setupBottomNav() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_statistics);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, MainActivity.class));
                finish();
                return true;
            }
            if (id == R.id.nav_plan) {
                startActivity(new Intent(this, PlanActivity.class));
                finish();
                return true;
            }
            if (id == R.id.nav_history) {
                startActivity(new Intent(this, TransactionHistoryActivity.class));
                finish();
                return true;
            }
            if (id == R.id.nav_statistics) return true;
            return false;
        });
    }

    private void loadWeekData() {
        Calendar start = (Calendar) currentWeek.clone();
        start.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        start.set(Calendar.HOUR_OF_DAY, 0);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);

        Calendar end = (Calendar) start.clone();
        end.add(Calendar.DAY_OF_YEAR, 6);
        end.set(Calendar.HOUR_OF_DAY, 23);
        end.set(Calendar.MINUTE, 59);

        SimpleDateFormat sdf = new SimpleDateFormat("d MMM", new Locale("id", "ID"));
        tvWeekRange.setText(sdf.format(start.getTime()) + " - " + sdf.format(end.getTime()));

        float[] dailyExpense = new float[7];
        float[] dailyIncome = new float[7];

        llIncomeList.removeAllViews();
        llExpenseList.removeAllViews();

        for (Transaction tx : Transaction.DATA) {
            DateCompareResult result = isDateInsideWeek(tx.getDate(), start, end);
            if (!result.isInside) continue;

            if (tx.getType() == Transaction.Type.INCOME) {
                dailyIncome[result.dayIndex] += tx.getAmount();
            } else {
                dailyExpense[result.dayIndex] += tx.getAmount();
            }

            View item = getLayoutInflater().inflate(R.layout.item_transaction, null, false);
            ((TextView) item.findViewById(R.id.tv_transaction_name)).setText(tx.getDescription());
            ((TextView) item.findViewById(R.id.tv_transaction_subtitle)).setText(
                    new SimpleDateFormat("dd MMM yyyy", new Locale("id", "ID")).format(tx.getDate()));
            ((TextView) item.findViewById(R.id.tv_transaction_amount)).setText(tx.getFormattedAmount());

            TextView tvType = item.findViewById(R.id.tv_transaction_type);
            TextView tvAmount = item.findViewById(R.id.tv_transaction_amount);
            if (tx.getType() == Transaction.Type.INCOME) {
                tvAmount.setTextColor(getColor(R.color.color_income));
                tvType.setText(getString(R.string.type_masuk));
                llIncomeList.addView(item);
            } else {
                tvAmount.setTextColor(getColor(R.color.color_expense));
                tvType.setText(getString(R.string.type_keluar));
                llExpenseList.addView(item);
            }
        }
        setupChart(dailyIncome, dailyExpense);
    }

    private void setupChart(float[] dailyIncome, float[] dailyExpense) {
        List<BarEntry> incomeEntries = new ArrayList<>();
        List<BarEntry> expenseEntries = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            incomeEntries.add(new BarEntry(i, dailyIncome[i]));
            expenseEntries.add(new BarEntry(i, dailyExpense[i]));
        }

        BarDataSet incomeDataSet = new BarDataSet(incomeEntries, "Pemasukan");
        incomeDataSet.setColor(Color.parseColor("#4CAF50"));

        BarDataSet expenseDataSet = new BarDataSet(expenseEntries, "Pengeluaran");
        expenseDataSet.setColor(Color.parseColor("#FF9800"));

        BarData data = new BarData(incomeDataSet, expenseDataSet);
        data.setBarWidth(0.3f);
        barChart.setData(data);

        barChart.groupBars(0f, 0.2f, 0.02f);
        barChart.getDescription().setEnabled(false);
        barChart.setFitBars(true);
        barChart.animateY(700);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setCenterAxisLabels(true);
        xAxis.setDrawGridLines(false);
        xAxis.setValueFormatter(new com.github.mikephil.charting.formatter.IndexAxisValueFormatter(
                new String[]{"Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min"}));
        xAxis.setAxisMinimum(0f);
        xAxis.setAxisMaximum(7f);

        barChart.getAxisRight().setEnabled(false);
        barChart.invalidate();
    }

    private DateCompareResult isDateInsideWeek(java.util.Date date, Calendar start, Calendar end) {
        Calendar txCal = Calendar.getInstance();
        txCal.setTime(date);
        boolean inside = !txCal.before(start) && !txCal.after(end);
        int dayIndex = txCal.get(Calendar.DAY_OF_WEEK) - 2;
        if (dayIndex < 0) dayIndex = 6;
        return new DateCompareResult(inside, dayIndex);
    }

    private static class DateCompareResult {
        boolean isInside;
        int dayIndex;
        DateCompareResult(boolean isInside, int dayIndex) {
            this.isInside = isInside;
            this.dayIndex = dayIndex;
        }
    }
}
