package com.sakustats;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {

    private EditText etName, etEmail, etPassword;
    private CheckBox cbTerms;
    private Button btnRegister;
    private TextView tvLoginLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etName = findViewById(R.id.et_name);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        cbTerms = findViewById(R.id.cb_terms);
        btnRegister = findViewById(R.id.btn_register);
        tvLoginLink = findViewById(R.id.tv_login_link);

        btnRegister.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Semua field harus diisi", Toast.LENGTH_SHORT).show();
            } else if (!cbTerms.isChecked()) {
                Toast.makeText(this, "Anda harus menyetujui syarat & ketentuan", Toast.LENGTH_SHORT).show();
            } else {
                // Mock success
                FirebaseHelper.auth
                        .createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {

                            if (task.isSuccessful()) {

                                com.google.firebase.auth.UserProfileChangeRequest profileUpdates =
                                        new com.google.firebase.auth.UserProfileChangeRequest.Builder()
                                                .setDisplayName(name)
                                                .build();

                                FirebaseHelper.auth
                                        .getCurrentUser()
                                        .updateProfile(profileUpdates)
                                        .addOnCompleteListener(profileTask -> {

                                            Toast.makeText(
                                                    this,
                                                    "Register berhasil",
                                                    Toast.LENGTH_SHORT
                                            ).show();

                                            finish();
                                        });

                            } else {

                                Toast.makeText(
                                        this,
                                        task.getException().getMessage(),
                                        Toast.LENGTH_LONG
                                ).show();
                            }
                        });
            }
        });

        tvLoginLink.setOnClickListener(v -> finish());
    }
}
