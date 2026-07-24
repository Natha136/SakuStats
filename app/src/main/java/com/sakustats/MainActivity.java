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

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView tvBalanceAmount;
    private TextView tvIncomeAmount;
    private TextView tvExpenseAmount;
    private LinearLayout llTransactions;
    private View includeEmptyState;
    private ImageView ivProfile;
    private static final int ADD_TX_REQUEST = 100;
    private static final int EDIT_TX_REQUEST = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        llTransactions.setVisibility(View.GONE);
        includeEmptyState.setVisibility(View.GONE);
        loadTransactions();
        loadProfileImage();
        setupClickListeners();
        setupBottomNavigation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProfileImage();
    }

    private void initViews() {
        tvBalanceAmount  = findViewById(R.id.tv_balance_amount);
        tvIncomeAmount   = findViewById(R.id.tv_income_amount);
        tvExpenseAmount  = findViewById(R.id.tv_expense_amount);
        llTransactions   = findViewById(R.id.ll_transactions);
        includeEmptyState = findViewById(R.id.include_empty_state);
        ivProfile        = findViewById(R.id.iv_profile);
    }

    private void setupClickListeners() {
        ivProfile.setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
        });

        findViewById(R.id.iv_notification).setOnClickListener(v -> {
            startActivity(new Intent(this, NotificationActivity.class));
        });

        findViewById(R.id.ll_uang_masuk).setOnClickListener(v -> openAddTransaction(Transaction.Type.INCOME));
        findViewById(R.id.ll_uang_keluar).setOnClickListener(v -> openAddTransaction(Transaction.Type.EXPENSE));
        findViewById(R.id.tv_lihat_semua).setOnClickListener(v -> {
            startActivity(new Intent(this, TransactionHistoryActivity.class));
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
        bottomNav.setSelectedItemId(R.id.nav_home);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) return true;
            if (id == R.id.nav_plan) {
                startActivity(new Intent(MainActivity.this, PlanActivity.class));
                finish();
                return true;
            }
            if (id == R.id.nav_history) {
                startActivity(new Intent(MainActivity.this, TransactionHistoryActivity.class));
                finish();
                return true;
            }
            if (id == R.id.nav_statistics) {
                startActivity(new Intent(MainActivity.this, StatisticsActivity.class));
                finish();
                return true;
            }
            return false;
        });
    }

    private void openAddTransaction(Transaction.Type preselectedType) {
        Intent intent = new Intent(this, AddTransactionActivity.class);
        if (preselectedType != null) {
            intent.putExtra(AddTransactionActivity.EXTRA_TYPE, preselectedType.name());
        }
        startActivityForResult(intent, ADD_TX_REQUEST);
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

    private void saveTransactionToFirebase(Transaction tx) {

        String uid =
                FirebaseHelper.auth
                        .getCurrentUser()
                        .getUid();

        FirebaseHelper.db
                .collection("users")
                .document(uid)
                .collection("transactions")
                .add(tx)

                .addOnSuccessListener(documentReference -> {

                    Transaction.DATA.add(0, tx);

                    refreshUI();
                });
    }

    private void loadTransactions() {

        String uid =
                FirebaseHelper.auth
                        .getCurrentUser()
                        .getUid();

        FirebaseHelper.db
                .collection("users")
                .document(uid)
                .collection("transactions")
                .orderBy("date",
                        com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()

                .addOnSuccessListener(queryDocumentSnapshots -> {

                    Transaction.DATA.clear();

                    for (com.google.firebase.firestore.DocumentSnapshot doc
                            : queryDocumentSnapshots) {

                        Transaction tx =
                                doc.toObject(Transaction.class);

                        Transaction.DATA.add(tx);
                    }

                    refreshUI();
                })

                .addOnFailureListener(e -> {

                    llTransactions.setVisibility(View.GONE);
                    includeEmptyState.setVisibility(View.VISIBLE);
                });
     }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == ADD_TX_REQUEST) {
                String desc = data.getStringExtra("description");
                long amount = data.getLongExtra("amount", 0);
                String typeStr = data.getStringExtra("type");
                Transaction.Type type = Transaction.Type.valueOf(typeStr);
                long dateMs = data.getLongExtra("date", System.currentTimeMillis());

                Transaction tx = new Transaction(
                        desc,
                        amount,
                        type,
                        new java.util.Date(dateMs)
                );

                saveTransactionToFirebase(tx);

            } else if (requestCode == EDIT_TX_REQUEST) {
                String action = data.getStringExtra("action");
                long id = data.getLongExtra("id", -1);

                if ("delete".equals(action)) {
                    for (int i = 0; i < Transaction.DATA.size(); i++) {
                        if (Transaction.DATA.get(i).getId() == id) {
                            Transaction.DATA.remove(i);
                            break;
                        }
                    }
                } else if ("update".equals(action)) {
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
            }
            refreshUI();
        }
    }

    private void refreshUI() {
        long totalIncome  = 0;
        long totalExpense = 0;

        for (Transaction tx : Transaction.DATA) {
            if (tx.getType() == Transaction.Type.INCOME) {
                totalIncome += tx.getAmount();
            } else {
                totalExpense += tx.getAmount();
            }
        }

        long balance = totalIncome - totalExpense;

        tvBalanceAmount.setText("Rp " + Transaction.formatRupiah(balance));
        tvIncomeAmount.setText("+ Rp " + Transaction.formatRupiah(totalIncome));
        tvExpenseAmount.setText("- Rp " + Transaction.formatRupiah(totalExpense));

        if (Transaction.DATA.isEmpty()) {
            llTransactions.setVisibility(View.GONE);
            includeEmptyState.setVisibility(View.VISIBLE);
        } else {
            llTransactions.setVisibility(View.VISIBLE);
            includeEmptyState.setVisibility(View.GONE);
            rebuildTransactionList();
        }
    }

    private void rebuildTransactionList() {
        llTransactions.removeAllViews();

        java.util.Collections.sort(
                Transaction.DATA,
                (t1, t2) -> t2.getDate().compareTo(t1.getDate())
        );

        int limit = Math.min(Transaction.DATA.size(), 3);
        for (int i = 0; i < limit; i++) {
            View item = getLayoutInflater().inflate(R.layout.item_transaction, llTransactions, false);
            Transaction tx = Transaction.DATA.get(i);

            ((TextView) item.findViewById(R.id.tv_transaction_name)).setText(tx.getDescription());

            TextView tvSubtitle = item.findViewById(R.id.tv_transaction_subtitle);

            java.text.SimpleDateFormat dateTimeFormat =
                    new java.text.SimpleDateFormat(
                            "dd MMM yyyy • hh:mm a",
                            new java.util.Locale("id", "ID")
                    );

            tvSubtitle.setText(
                    dateTimeFormat.format(tx.getDate())
                            + " • "
                            + (tx.getType() == Transaction.Type.INCOME ? "Masuk" : "Keluar")
            );

            ((TextView) item.findViewById(R.id.tv_transaction_amount)).setText(tx.getFormattedAmount());

            TextView tvType = item.findViewById(R.id.tv_transaction_type);
            TextView tvAmount = item.findViewById(R.id.tv_transaction_amount);
            if (tx.getType() == Transaction.Type.INCOME) {
                tvAmount.setTextColor(getColor(R.color.color_income));
                tvType.setText(getString(R.string.type_masuk));
            } else {
                tvAmount.setTextColor(getColor(R.color.color_expense));
                tvType.setText(getString(R.string.type_keluar));
            }

            item.setOnLongClickListener(v -> {
                openEditTransaction(tx);
                return true;
            });

            llTransactions.addView(item);
        }
    }
}
