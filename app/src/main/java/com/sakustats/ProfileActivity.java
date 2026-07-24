package com.sakustats;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvName, tvEmail, tvTotalSavings;
    private ImageView ivProfile;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        tvName = findViewById(R.id.tv_profile_name);
        tvEmail = findViewById(R.id.tv_profile_email);
        tvTotalSavings = findViewById(R.id.tv_total_savings);
        ivProfile = findViewById(R.id.iv_profile_img);

        ImageView btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        ImageView btnToolbarNotif = findViewById(R.id.btn_toolbar_notif);
        btnToolbarNotif.setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, NotificationActivity.class));
            finish();
        });

        LinearLayout btnEditProfile = findViewById(R.id.btn_edit_profile);
        btnEditProfile.setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, EditProfileActivity.class));
        });

        LinearLayout btnNotifications = findViewById(R.id.btn_notifications);
        btnNotifications.setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, NotificationActivity.class));
        });

        LinearLayout btnLogout = findViewById(R.id.btn_logout);
        btnLogout.setOnClickListener(v -> showLogoutConfirmation());
        
        loadProfileData();
        loadProfileImage();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProfileData();
        loadProfileImage();
    }

    private void loadProfileData() {

        if (FirebaseHelper.auth.getCurrentUser() != null) {

            String email =
                    FirebaseHelper.auth
                            .getCurrentUser()
                            .getEmail();

            String name =
                    FirebaseHelper.auth
                            .getCurrentUser()
                            .getDisplayName();

            tvEmail.setText(email);

            if (name != null && !name.isEmpty()) {
                tvName.setText(name);
            } else {
                tvName.setText(email.split("@")[0]);
            }
        }

        long totalIncome = 0;
        long totalExpense = 0;

        for (Transaction tx : Transaction.DATA) {

            if (tx.getType() == Transaction.Type.INCOME) {
                totalIncome += tx.getAmount();
            } else {
                totalExpense += tx.getAmount();
            }
        }

        long balance = totalIncome - totalExpense;

        tvTotalSavings.setText(
                "Rp " + Transaction.formatRupiah(balance)
        );
    }

    private void loadProfileImage() {

        String uid =
                FirebaseHelper.auth
                        .getCurrentUser()
                        .getUid();

        FirebaseHelper.db
                .collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(document -> {

                    String base64 =
                            document.getString("profileImage");

                    if (base64 != null && !base64.isEmpty()) {

                        byte[] bytes =
                                android.util.Base64.decode(
                                        base64,
                                        android.util.Base64.DEFAULT
                                );

                        android.graphics.Bitmap bitmap =
                                android.graphics.BitmapFactory
                                        .decodeByteArray(
                                                bytes,
                                                0,
                                                bytes.length
                                        );

                        ivProfile.setImageBitmap(bitmap);
                    }
                });
    }

    private void showLogoutConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Konfirmasi Keluar")
                .setMessage("Apakah Anda yakin ingin keluar dari akun ini?")
                .setPositiveButton("Ya, Keluar", (dialog, which) -> {
                    FirebaseHelper.auth.signOut();
                    Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Batal", null)
                .show();
    }
}
