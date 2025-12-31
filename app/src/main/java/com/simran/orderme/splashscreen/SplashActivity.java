package com.simran.orderme.splashscreen;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.simran.orderme.introscreen.IntroActivity;
import com.simran.orderme.R;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends Activity {

    private static final int SPLASH_SCREEN_TIMEOUT = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);

        ImageView mainLogo = findViewById(R.id.main_logo);
        TextView welcomeText = findViewById(R.id.welcome_text);
        TextView sloganText = findViewById(R.id.slogan_text);

        // Load animations
        Animation popUpAnimation = AnimationUtils.loadAnimation(this, R.anim.pop_up);
        Animation slideUpAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_up);

        // Start animation for main logo
        mainLogo.startAnimation(popUpAnimation);
        mainLogo.setVisibility(ImageView.VISIBLE);

        // Delay the text animations until the logo animation completes
        popUpAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                // Do nothing
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                welcomeText.startAnimation(slideUpAnimation);
                welcomeText.setVisibility(TextView.VISIBLE);
                sloganText.startAnimation(slideUpAnimation);
                sloganText.setVisibility(TextView.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // Do nothing
            }
        });

        // Start a new activity after the splash screen timeout
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, IntroActivity.class);
            startActivity(intent);
            finish();
        }, SPLASH_SCREEN_TIMEOUT);
    }
}
