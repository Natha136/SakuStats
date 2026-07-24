package com.sakustats;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class TransactionHistoryActivity extends AppCompatActivity {

    private EditText etSearch;
    private TransactionAdapter adapter;
    private static final int EDIT_TX_REQUEST = 200;
    private Calendar selectedDate = Calendar.getInstance();
    private TextView tvSelectedDate;
    private LinearLayout layoutDateNavigation;
    private ImageView ivProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_history);
        getOnBackPressedDispatcher().addCallback(this,
                new androidx.activity.OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {

                        Intent intent = new Intent(
                                TransactionHistoryActivity.this,
                                MainActivity.class
                        );

                        startActivity(intent);
                        finish();
                    }
                });

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        initViews();

        etSearch = findViewById(R.id.et_search);
        layoutDateNavigation = findViewById(R.id.layout_date_navigation);
        tvSelectedDate = findViewById(R.id.tv_selected_date);

        updateDateText();

        setupClickListeners();
        setupBottomNavigation();
        loadProfileImage();

        filterTransactionsByDate();

        etSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String keyword = s.toString().trim();
                if (keyword.isEmpty()) {
                    layoutDateNavigation.setVisibility(View.VISIBLE);
                } else {
                    layoutDateNavigation.setVisibility(View.GONE);
                }
                filterTransactionsByDate();
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProfileImage();
    }

    private void updateDateText() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy", new Locale("id", "ID"));
        tvSelectedDate.setText(sdf.format(selectedDate.getTime()));
    }

    private void filterTransactionsByDate() {
        List<Transaction> filtered = new ArrayList<>();
        Calendar target = Calendar.getInstance();
        target.setTime(selectedDate.getTime());

        for (Transaction tx : Transaction.DATA) {
            Calendar txCal = Calendar.getInstance();
            txCal.setTime(tx.getDate());

            boolean sameDay = txCal.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
                    txCal.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR);

            String keyword = etSearch.getText().toString().trim().toLowerCase();
            boolean matchSearch = tx.getDescription().toLowerCase().contains(keyword);

            if (!keyword.isEmpty()) {
                if (matchSearch) {
                    filtered.add(tx);
                }
            } else {
                if (sameDay) {
                    filtered.add(tx);
                }
            }
        }

        filtered.sort((a, b) -> b.getDate().compareTo(a.getDate()));
        adapter.setTransactions(filtered);
    }

    private void initViews() {
        RecyclerView rv = findViewById(R.id.rv_all_transactions);
        rv.setLayoutManager(new LinearLayoutManager(this));

        adapter = new TransactionAdapter(this);
        rv.setAdapter(adapter);

        adapter.setOnItemLongClickListener(this::openEditTransaction);
        ivProfile = findViewById(R.id.iv_profile);
    }

    private void setupClickListeners() {
        ivProfile.setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
        });

        findViewById(R.id.iv_notification).setOnClickListener(v -> {
            startActivity(new Intent(this, NotificationActivity.class));
        });

        findViewById(R.id.btn_prev_day).setOnClickListener(v -> {
            selectedDate.add(Calendar.DAY_OF_MONTH, -1);
            updateDateText();
            filterTransactionsByDate();
        });

        findViewById(R.id.btn_next_day).setOnClickListener(v -> {
            selectedDate.add(Calendar.DAY_OF_MONTH, 1);
            updateDateText();
            filterTransactionsByDate();
        });

        tvSelectedDate.setOnClickListener(v -> {
            new android.app.DatePickerDialog(this, (view, year, month, day) -> {
                selectedDate.set(year, month, day);
                updateDateText();
                filterTransactionsByDate();
            }, selectedDate.get(Calendar.YEAR), selectedDate.get(Calendar.MONTH), selectedDate.get(Calendar.DAY_OF_MONTH)).show();
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
        bottomNav.setSelectedItemId(R.id.nav_history);
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
            if (id == R.id.nav_history) return true;
            if (id == R.id.nav_statistics) {
                startActivity(new Intent(this, StatisticsActivity.class));
                finish();
                return true;
            }
            return false;
        });
    }

    private void openEditTransaction(Transaction tx) {
        Intent intent = new Intent(this, EditTransactionActivity.class);
        intent.putExtra(EditTransactionActivity.EXTRA_ID, tx.getId());
        intent.putExtra(EditTransactionActivity.EXTRA_DESCRIPTION, tx.getDescription());
        intent.putExtra(EditTransactionActivity.EXTRA_NOTE, tx.getNote());
        intent.putExtra(EditTransactionActivity.EXTRA_AMOUNT, tx.getAmount());
        intent.putExtra(EditTransactionActivity.EXTRA_TYPE, tx.getType().name());
        intent.putExtra(EditTransactionActivity.EXTRA_DATE, tx.getDate().getTime());
        startActivityForResult(intent, EDIT_TX_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == EDIT_TX_REQUEST && resultCode == RESULT_OK && data != null) {
            String action = data.getStringExtra("action");
            long id = data.getLongExtra("id", -1);

            if ("delete".equals(action)) {
                deleteTransaction(id);
            } else if ("update".equals(action)) {
                updateTransaction(id, data);
            }
            refreshData();
        }
    }

    private void deleteTransaction(long id) {
        for (int i = 0; i < Transaction.DATA.size(); i++) {
            if (Transaction.DATA.get(i).getId() == id) {
                Transaction.DATA.remove(i);
                break;
            }
        }
    }

    private void updateTransaction(long id, Intent data) {
        for (Transaction tx : Transaction.DATA) {
            if (tx.getId() == id) {
                tx.setDescription(data.getStringExtra("description"));
                tx.setNote(data.getStringExtra("note"));
                tx.setAmount(data.getLongExtra("amount", 0));
                tx.setType(Transaction.Type.valueOf(data.getStringExtra("type")));
                tx.setDate(new java.util.Date(data.getLongExtra("date", System.currentTimeMillis())));
                break;
            }
        }
    }

    private void refreshData() {
        filterTransactionsByDate();
    }
}

