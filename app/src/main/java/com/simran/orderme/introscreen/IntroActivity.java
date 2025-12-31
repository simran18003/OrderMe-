package com.simran.orderme.introscreen;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager.widget.ViewPager.OnPageChangeListener;

import com.simran.orderme.adapters.IntroViewPagerAdapter;
import com.simran.orderme.loginsignup.LoginSignupActivity;
import com.simran.orderme.R;

public class IntroActivity extends AppCompatActivity {

    private ViewPager viewPager;
    private IntroViewPagerAdapter viewPagerAdapter;
    private LinearLayout dotsLayout;
    private TextView[] dots;
    private Button btnGo;
    private int[] layouts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        viewPager = findViewById(R.id.viewPager);
        dotsLayout = findViewById(R.id.dotsLayout);
        btnGo = findViewById(R.id.btn_go);

        // Array of layouts for each slide
        layouts = new int[]{
                R.layout.slide_1,
                R.layout.slide_2,
                R.layout.slide_3
        };

        // Adding bottom dots
        addBottomDots(0);

        viewPagerAdapter = new IntroViewPagerAdapter(layouts, this);
        viewPager.setAdapter(viewPagerAdapter);
        viewPager.addOnPageChangeListener(viewPagerPageChangeListener);

        btnGo.setOnClickListener(v -> {
            Intent intent = new Intent(IntroActivity.this, LoginSignupActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void addBottomDots(int currentPage) {
        dots = new TextView[layouts.length];

        int[] colorsActive = getResources().getIntArray(R.array.array_dot_active);
        int[] colorsInactive = getResources().getIntArray(R.array.array_dot_inactive);

        dotsLayout.removeAllViews();
        for (int i = 0; i < dots.length; i++) {
            dots[i] = new TextView(this);
            dots[i].setText("\u2022");
            dots[i].setTextSize(35);
            dots[i].setTextColor(colorsInactive[currentPage]);
            dotsLayout.addView(dots[i]);
        }

        if (dots.length > 0) {
            dots[currentPage].setTextColor(colorsActive[currentPage]);
        }
    }

    // ViewPager page change listener
    ViewPager.OnPageChangeListener viewPagerPageChangeListener = new OnPageChangeListener() {

        @Override
        public void onPageSelected(int position) {
            addBottomDots(position);

            // Show the "Go" button only on the last page
            if (position == layouts.length - 1) {
                btnGo.setVisibility(View.VISIBLE);
            } else {
                btnGo.setVisibility(View.GONE);
            }
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        @Override
        public void onPageScrollStateChanged(int state) {
        }
    };
}

