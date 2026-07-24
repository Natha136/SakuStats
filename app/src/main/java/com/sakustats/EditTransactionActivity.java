package com.sakustats;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class EditTransactionActivity extends AppCompatActivity {

    public static final String EXTRA_ID = "extra_id";
    public static final String EXTRA_DESCRIPTION = "extra_description";
    public static final String EXTRA_NOTE = "extra_note";
    public static final String EXTRA_AMOUNT = "extra_amount";
    public static final String EXTRA_TYPE = "extra_type";
    public static final String EXTRA_DATE = "extra_date";

    private EditText etDescription, etAmount, etNote;
    private TextView tvTypeExpense, tvTypeIncome, tvDate;
    private LinearLayout llPickDate;
    private MaterialButton btnDelete, btnSave;

    private Transaction.Type selectedType = Transaction.Type.EXPENSE;
    private Calendar selectedDate = Calendar.getInstance();
    private SimpleDateFormat displayFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
    private long transactionId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_transaction);

        Toolbar toolbar = findViewById(R.id.toolbar_edit);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        etDescription = findViewById(R.id.et_description);
        etAmount = findViewById(R.id.et_amount);
        etNote = findViewById(R.id.et_note);
        tvTypeExpense = findViewById(R.id.tv_type_expense);
        tvTypeIncome = findViewById(R.id.tv_type_income);
        tvDate = findViewById(R.id.tv_date);
        llPickDate = findViewById(R.id.ll_pick_date);
        btnDelete = findViewById(R.id.btn_delete);
        btnSave = findViewById(R.id.btn_save);

        // Load data from Intent
        Intent intent = getIntent();
        if (intent != null) {
            transactionId = intent.getLongExtra(EXTRA_ID, -1);
            etDescription.setText(intent.getStringExtra(EXTRA_DESCRIPTION));
            etAmount.setText(String.valueOf(intent.getLongExtra(EXTRA_AMOUNT, 0)));
            etNote.setText(intent.getStringExtra(EXTRA_NOTE));
            
            String typeStr = intent.getStringExtra(EXTRA_TYPE);
            if (typeStr != null) {
                selectedType = Transaction.Type.valueOf(typeStr);
                updateTypeToggle();
            }

            long dateMs = intent.getLongExtra(EXTRA_DATE, System.currentTimeMillis());
            selectedDate.setTimeInMillis(dateMs);
            tvDate.setText(displayFormat.format(selectedDate.getTime()));
        }

        tvTypeExpense.setOnClickListener(v -> {
            selectedType = Transaction.Type.EXPENSE;
            updateTypeToggle();
        });

        tvTypeIncome.setOnClickListener(v -> {
            selectedType = Transaction.Type.INCOME;
            updateTypeToggle();
        });

        llPickDate.setOnClickListener(v -> showDatePicker());

        btnDelete.setOnClickListener(v -> {
            Intent result = new Intent();
            result.putExtra("action", "delete");
            result.putExtra("id", transactionId);
            setResult(RESULT_OK, result);
            finish();
        });

        btnSave.setOnClickListener(v -> saveChanges());
    }

    private void updateTypeToggle() {
        if (selectedType == Transaction.Type.EXPENSE) {
            tvTypeExpense.setBackgroundResource(R.drawable.bg_filter_active);
            tvTypeExpense.setBackgroundTintList(null);
            tvTypeExpense.setTextColor(ContextCompat.getColor(this, R.color.color_white));
            
            tvTypeIncome.setBackgroundResource(R.drawable.bg_filter_inactive);
            tvTypeIncome.setTextColor(ContextCompat.getColor(this, R.color.color_text_secondary));
        } else {
            tvTypeIncome.setBackgroundResource(R.drawable.bg_filter_active);
            tvTypeIncome.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.color_text_brand_dark));
            tvTypeIncome.setTextColor(ContextCompat.getColor(this, R.color.color_white));
            
            tvTypeExpense.setBackgroundResource(R.drawable.bg_filter_inactive);
            tvTypeExpense.setTextColor(ContextCompat.getColor(this, R.color.color_text_secondary));
        }
    }

    private void showDatePicker() {
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            selectedDate.set(Calendar.YEAR, year);
            selectedDate.set(Calendar.MONTH, month);
            selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            tvDate.setText(displayFormat.format(selectedDate.getTime()));
        }, selectedDate.get(Calendar.YEAR), selectedDate.get(Calendar.MONTH), 
           selectedDate.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void saveChanges() {
        String desc = etDescription.getText().toString().trim();
        String amountStr = etAmount.getText().toString().trim();
        String note = etNote.getText().toString().trim();

        if (TextUtils.isEmpty(desc)) {
            etDescription.setError("Wajib diisi");
            return;
        }
        if (TextUtils.isEmpty(amountStr)) {
            etAmount.setError("Wajib diisi");
            return;
        }

        Intent result = new Intent();
        result.putExtra("action", "update");
        result.putExtra("id", transactionId);
        result.putExtra("description", desc);
        result.putExtra("note", note);
        result.putExtra("amount", Long.parseLong(amountStr));
        result.putExtra("type", selectedType.name());
        result.putExtra("date", selectedDate.getTimeInMillis());
        setResult(RESULT_OK, result);
        finish();
    }
}
