package com.sakustats;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.content.Intent;
import android.net.Uri;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.io.ByteArrayOutputStream;

import androidx.appcompat.app.AppCompatActivity;

public class EditProfileActivity extends AppCompatActivity {

    private EditText etName;
    private static final int PICK_IMAGE_REQUEST = 101;
    private ImageView ivProfile;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        initViews();
        loadCurrentData();

        ivProfile.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            Intent.ACTION_PICK
                    );

            intent.setType("image/*");

            startActivityForResult(
                    intent,
                    PICK_IMAGE_REQUEST
            );
        });

        ImageView btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        Button btnSave = findViewById(R.id.btn_save);
        btnSave.setOnClickListener(v -> saveData());
    }

    private void initViews() {
        etName = findViewById(R.id.et_name);

        ivProfile =
                findViewById(
                        R.id.iv_edit_profile_img
                );
    }

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data
    ) {
        super.onActivityResult(
                requestCode,
                resultCode,
                data
        );

        if (requestCode == PICK_IMAGE_REQUEST
                && resultCode == RESULT_OK
                && data != null) {

            imageUri = data.getData();

            ivProfile.setImageURI(imageUri);
        }
    }

    private String imageToBase64(Uri uri) {

        try {

            Bitmap bitmap =
                    android.provider.MediaStore.Images.Media
                            .getBitmap(
                                    getContentResolver(),
                                    uri
                            );

            ByteArrayOutputStream baos =
                    new ByteArrayOutputStream();

            bitmap.compress(
                    Bitmap.CompressFormat.JPEG,
                    50,
                    baos
            );

            return Base64.encodeToString(
                    baos.toByteArray(),
                    Base64.DEFAULT
            );

        } catch (Exception e) {

            e.printStackTrace();
            return null;
        }
    }

    private Bitmap base64ToBitmap(String base64) {

        byte[] bytes =
                Base64.decode(
                        base64,
                        Base64.DEFAULT
                );

        return BitmapFactory.decodeByteArray(
                bytes,
                0,
                bytes.length
        );
    }

    private void saveProfileImage() {

        if (imageUri == null) {

            Toast.makeText(
                    this,
                    "Profil berhasil diperbarui",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
            return;
        }

        String base64 =
                imageToBase64(imageUri);

        String uid =
                FirebaseHelper.auth
                        .getCurrentUser()
                        .getUid();

        FirebaseHelper.db
                .collection("users")
                .document(uid)
                .set(
                        java.util.Collections.singletonMap(
                                "profileImage",
                                base64
                        ),
                        com.google.firebase.firestore.SetOptions.merge()
                )

                .addOnSuccessListener(unused -> {

                    Toast.makeText(
                            this,
                            "Profil berhasil diperbarui",
                            Toast.LENGTH_SHORT
                    ).show();

                    finish();
                })

                .addOnFailureListener(e -> {

                    Toast.makeText(
                            this,
                            "Gagal menyimpan foto: "
                                    + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    private void loadCurrentData() {

        String uid =
                FirebaseHelper.auth
                        .getCurrentUser()
                        .getUid();


        FirebaseHelper.db
                .collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {

                    String image =
                            doc.getString(
                                    "profileImage"
                            );

                    if (image != null) {

                        ivProfile.setImageBitmap(
                                base64ToBitmap(image)
                        );
                    }
                });

        if (FirebaseHelper.auth.getCurrentUser() != null) {

            String name =
                    FirebaseHelper.auth
                            .getCurrentUser()
                            .getDisplayName();

            etName.setText(name);
        }
    }

    private void saveData() {

        String newName =
                etName.getText().toString().trim();

        if (newName.isEmpty()) {

            Toast.makeText(
                    this,
                    "Nama tidak boleh kosong",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        com.google.firebase.auth.FirebaseUser user =
                FirebaseHelper.auth.getCurrentUser();


        if (user == null) {

            Toast.makeText(
                    this,
                    "User tidak ditemukan",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        com.google.firebase.auth.UserProfileChangeRequest profileUpdates =
                new com.google.firebase.auth.UserProfileChangeRequest.Builder()
                        .setDisplayName(newName)
                        .build();

            user.updateProfile(profileUpdates)

                    .addOnSuccessListener(unused -> {

                        saveProfileImage();
                    })

                .addOnFailureListener(e -> {

                    Toast.makeText(
                            this,
                            "Gagal update profil: "
                                    + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }
}
