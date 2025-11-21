package com.example.bunny;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.AppCompatImageButton;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;

public class DashboardActivity extends AppCompatActivity {

    private SharedPreferences prefs;
    private boolean darkModeOn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load theme setting
        prefs = getSharedPreferences("mindtokki_prefs", MODE_PRIVATE);
        darkModeOn = prefs.getBoolean("dark_mode", false);
        AppCompatDelegate.setDefaultNightMode(
                darkModeOn ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );

        setContentView(R.layout.activity_dashboard);

        // 🌸 Personalized greeting
        TextView tvHelloSmall = findViewById(R.id.tvHelloSmall);

        String username = getIntent().getStringExtra("username");
        if (username == null || username.isEmpty()) {
            username = "friend";
        }

        tvHelloSmall.setText("Hello, " + username + "! 🌸");

        // 🌙 Dark mode toggle button
        AppCompatImageButton btnDark = findViewById(R.id.btnDarkToggle);
        updateDarkToggleIcon(btnDark, darkModeOn);

        btnDark.setOnClickListener(v -> {
            darkModeOn = !darkModeOn;
            prefs.edit().putBoolean("dark_mode", darkModeOn).apply();
            AppCompatDelegate.setDefaultNightMode(
                    darkModeOn ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
            );
            updateDarkToggleIcon(btnDark, darkModeOn);
        });

        // 🌿 Bottom navigation bar
        BottomNavigationView bottom = findViewById(R.id.bottomNav);
        bottom.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show();
                return true;

            } else if (itemId == R.id.nav_journal) {

                // ✅ OPEN MOOD JOURNAL
                startActivity(new Intent(DashboardActivity.this, MoodJournalActivity.class));
                return true;

            } else if (itemId == R.id.nav_tokki) {

                // ✅ OPEN CHAT WITH TOKKI
                startActivity(new Intent(DashboardActivity.this, TokkiChatActivity.class));
                return true;

            } else if (itemId == R.id.nav_study) {

                // ✅ OPEN STUDY & WELLNESS
                startActivity(new Intent(DashboardActivity.this, StudyActivity.class));
                return true;
            }


        } else if (itemId == R.id.nav_garden) {
                Toast.makeText(this, "Garden", Toast.LENGTH_SHORT).show();
                return true;
            }

            return false;
        });

        // 🌸 Feature card click listeners
        MaterialCardView cardFeatureJournal = findViewById(R.id.cardFeatureJournal);
        MaterialCardView cardFeatureChat = findViewById(R.id.cardFeatureChat);
        MaterialCardView cardFeatureStudy = findViewById(R.id.cardFeatureStudy);
        MaterialCardView cardFeatureGarden = findViewById(R.id.cardFeatureGarden);

        // ✅ OPEN MOOD JOURNAL
        cardFeatureJournal.setOnClickListener(v ->
                startActivity(new Intent(DashboardActivity.this, MoodJournalActivity.class))
        );

        // ✅ OPEN CHAT WITH TOKKI (Feature card)
        cardFeatureChat.setOnClickListener(v ->
                startActivity(new Intent(DashboardActivity.this, TokkiChatActivity.class))
        );

        // Placeholder until Study is added
 cardFeatureStudy.setOnClickListener(v ->
    startActivity(new Intent(DashboardActivity.this, StudyActivity.class))
            );


    // Placeholder until Garden is added
        cardFeatureGarden.setOnClickListener(v ->
                Toast.makeText(this, "Open Garden", Toast.LENGTH_SHORT).show()
        );
    }

    private void updateDarkToggleIcon(AppCompatImageButton button, boolean dark) {
        if (dark) {
            button.setImageResource(R.drawable.ic_sun);
            button.setBackground(getDrawable(R.drawable.dark_toggle_bg_active));
        } else {
            button.setImageResource(R.drawable.ic_moon);
            button.setBackground(getDrawable(R.drawable.dark_toggle_bg));
        }
    }
}




