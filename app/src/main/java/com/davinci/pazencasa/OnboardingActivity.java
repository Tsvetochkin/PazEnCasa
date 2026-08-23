package com.davinci.pazencasa;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class OnboardingActivity extends AppCompatActivity {

    private View[] pages;
    private TextView dot1, dot2, dot3;
    private Button btnNext;
    private int currentPage = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Si ya está logueado, ir directo al juego
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            go(MainActivity.class);
            return;
        }

        setContentView(R.layout.activity_onboarding);

        pages = new View[]{
            findViewById(R.id.page1),
            findViewById(R.id.page2),
            findViewById(R.id.page3)
        };

        dot1    = findViewById(R.id.dot1);
        dot2    = findViewById(R.id.dot2);
        dot3    = findViewById(R.id.dot3);
        btnNext = findViewById(R.id.btn_next);

        int w = getResources().getDisplayMetrics().widthPixels;
        pages[1].setTranslationX(w);
        pages[2].setTranslationX(w);

        updateUI();

        btnNext.setOnClickListener(v -> { if (currentPage < 2) animateTo(currentPage + 1); });
        findViewById(R.id.btn_skip).setOnClickListener(v -> go(LoginActivity.class));
        findViewById(R.id.btn_login).setOnClickListener(v -> go(LoginActivity.class));
        findViewById(R.id.btn_register).setOnClickListener(v -> go(RegisterActivity.class));
    }

    private void animateTo(int next) {
        int w = getResources().getDisplayMetrics().widthPixels;
        pages[currentPage].animate().translationX(-w)
                .setDuration(350).setInterpolator(new DecelerateInterpolator()).start();
        pages[next].setTranslationX(w);
        pages[next].animate().translationX(0)
                .setDuration(350).setInterpolator(new DecelerateInterpolator()).start();
        currentPage = next;
        updateUI();
    }

    private void updateUI() {
        int on  = 0xFFFFFFFF;
        int off = 0x55FFFFFF;
        dot1.setTextColor(currentPage == 0 ? on : off);
        dot2.setTextColor(currentPage == 1 ? on : off);
        dot3.setTextColor(currentPage == 2 ? on : off);
        btnNext.setVisibility(currentPage < 2 ? View.VISIBLE : View.INVISIBLE);
    }

    private void go(Class<?> dest) {
        startActivity(new Intent(this, dest));
        finish();
    }
}
