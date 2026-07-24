package com.sakustats;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.animation.AnimationSet;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DURATION_MS = 2200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ImageView ivLogo = findViewById(R.id.iv_splash_logo);
        TextView tvAppName = findViewById(R.id.tv_splash_name);
        TextView tvTagline = findViewById(R.id.tv_splash_tagline);

        // Fade + scale animation for logo
        AnimationSet logoAnim = new AnimationSet(true);
        AlphaAnimation fadeIn = new AlphaAnimation(0f, 1f);
        fadeIn.setDuration(700);
        ScaleAnimation scaleUp = new ScaleAnimation(
                0.7f, 1f, 0.7f, 1f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        scaleUp.setDuration(700);
        logoAnim.addAnimation(fadeIn);
        logoAnim.addAnimation(scaleUp);
        logoAnim.setFillAfter(true);
        ivLogo.startAnimation(logoAnim);

        // Delayed fade in for app name
        AlphaAnimation nameAnim = new AlphaAnimation(0f, 1f);
        nameAnim.setDuration(500);
        nameAnim.setStartOffset(500);
        nameAnim.setFillAfter(true);
        tvAppName.startAnimation(nameAnim);

        // Delayed fade in for tagline
        AlphaAnimation taglineAnim = new AlphaAnimation(0f, 1f);
        taglineAnim.setDuration(500);
        taglineAnim.setStartOffset(900);
        taglineAnim.setFillAfter(true);
        tvTagline.startAnimation(taglineAnim);

        // Navigate to LoginActivity after splash duration
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (FirebaseHelper.auth.getCurrentUser() != null) {

                startActivity(
                        new Intent(
                                SplashActivity.this,
                                MainActivity.class
                        )
                );

            } else {

                startActivity(
                        new Intent(
                                SplashActivity.this,
                                LoginActivity.class
                        )
                );
            }
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }, SPLASH_DURATION_MS);
    }
}
