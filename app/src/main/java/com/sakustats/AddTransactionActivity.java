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

public class AddTransactionActivity extends AppCompatActivity {

    public static final String EXTRA_TYPE = "extra_type";

    private EditText etDescription;
    private EditText etAmount;
    private LinearLayout llTypeExpense, llTypeIncome;
    private LinearLayout llPickDate;
    private TextView tvDateLabel, tvDateValue;

    private Transaction.Type selectedType = Transaction.Type.INCOME;
    private Calendar selectedDate = Calendar.getInstance();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", new Locale("id", "ID"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);

        Toolbar toolbar = findViewById(R.id.toolbar_add);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        etDescription = findViewById(R.id.et_description);
        etAmount      = findViewById(R.id.et_amount);
        llTypeExpense = findViewById(R.id.ll_type_expense);
        llTypeIncome  = findViewById(R.id.ll_type_income);
        llPickDate    = findViewById(R.id.ll_pick_date);
        tvDateLabel   = findViewById(R.id.tv_date_label);
        tvDateValue   = findViewById(R.id.tv_date_value);
        MaterialButton btnSave = findViewById(R.id.btn_save);

        // Initial date setup
        updateDateDisplay();

        // Type toggle listeners
        llTypeExpense.setOnClickListener(v -> selectType(Transaction.Type.EXPENSE));
        llTypeIncome.setOnClickListener(v -> selectType(Transaction.Type.INCOME));

        // Date picker listener
        llPickDate.setOnClickListener(v -> showDatePicker());

        // Pre-select type if passed from quick action
        String extraType = getIntent().getStringExtra(EXTRA_TYPE);
        if (extraType != null) {
            if (extraType.equals(Transaction.Type.INCOME.name())) {
                selectType(Transaction.Type.INCOME);
            } else {
                selectType(Transaction.Type.EXPENSE);
            }
        } else {
            selectType(Transaction.Type.INCOME); // Default
        }

        btnSave.setOnClickListener(v -> saveTransaction());
    }

    private void selectType(Transaction.Type type) {
        selectedType = type;
        if (type == Transaction.Type.EXPENSE) {
            llTypeExpense.setBackgroundResource(R.drawable.bg_toggle_active);
            updateToggleContent(llTypeExpense, true);
            
            llTypeIncome.setBackground(null);
            updateToggleContent(llTypeIncome, false);
        } else {
            llTypeIncome.setBackgroundResource(R.drawable.bg_toggle_active);
            updateToggleContent(llTypeIncome, true);
            
            llTypeExpense.setBackground(null);
            updateToggleContent(llTypeExpense, false);
        }
    }

    private void updateToggleContent(LinearLayout layout, boolean active) {
        View icon = layout.getChildAt(0);
        TextView text = (TextView) layout.getChildAt(1);
        int color = active ? 
            ContextCompat.getColor(this, R.color.color_text_brand_dark) : 
            ContextCompat.getColor(this, R.color.color_text_secondary);
        
        icon.setAlpha(active ? 1.0f : 0.5f);
        text.setTextColor(color);
    }

    private void showDatePicker() {
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            selectedDate.set(Calendar.YEAR, year);
            selectedDate.set(Calendar.MONTH, month);
            selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateDateDisplay();
        }, selectedDate.get(Calendar.YEAR), selectedDate.get(Calendar.MONTH), 
           selectedDate.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void updateDateDisplay() {
        Calendar today = Calendar.getInstance();
        if (selectedDate.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
            selectedDate.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)) {
            tvDateLabel.setText(R.string.label_today);
        } else {
            tvDateLabel.setText(R.string.label_selected_date);
        }
        tvDateValue.setText(dateFormat.format(selectedDate.getTime()));
    }

    private void saveTransaction() {
        String desc = etDescription.getText().toString().trim();
        String amountStr = etAmount.getText().toString().trim();

        if (TextUtils.isEmpty(desc)) {
            etDescription.setError(getString(R.string.hint_description));
            return;
        }
        if (TextUtils.isEmpty(amountStr)) {
            etAmount.setError(getString(R.string.hint_amount));
            return;
        }

        long amount;
        try {
            amount = Long.parseLong(amountStr);
        } catch (NumberFormatException e) {
            etAmount.setError(getString(R.string.hint_amount));
            return;
        }

        Intent result = new Intent();
        result.putExtra("description", desc);
        result.putExtra("amount", amount);
        result.putExtra("type", selectedType.name());
        result.putExtra("date", selectedDate.getTimeInMillis());
        setResult(RESULT_OK, result);
        finish();
    }
}
