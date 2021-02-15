package com.tomi.ohl.szakdoga;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.tomi.ohl.szakdoga.controller.FamilyController;
import com.tomi.ohl.szakdoga.views.FamilyChooserBottomSheet;

import java.util.HashMap;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // A kiválasztott téma beállítása
        SharedPreferences pref = getSharedPreferences(BuildConfig.APPLICATION_ID + "_preferences", Context.MODE_PRIVATE);
        String themeSetting = pref.getString("theme", "system");
        if (themeSetting != null)
            switch(themeSetting) {
                case "system":
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                    break;
                case "light":
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    break;
                case "dark":
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            }

        // Van-e bejelentkezett user?
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            // Nincs, irány bejelentkezni
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        } else {
            // Van, kérjük le a családját
            FamilyController.getInstance().getFamily().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null && document.exists()) {
                        // Van családja, ezt beállítjuk, majd irány a főkepernyő
                        HashMap<String, Object> familydata = (HashMap<String, Object>) document.getData();
                        assert familydata != null;
                        FamilyController.getInstance().setCurrentFamily((String) familydata.get("family"));
                        startActivity(new Intent(SplashActivity.this, MainActivity.class));
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                        finish();
                    } else {
                        // Kérjük be a családját a bottom sheettel
                        FamilyChooserBottomSheet familyChooser = new FamilyChooserBottomSheet();
                        familyChooser.setCancelable(false);
                        familyChooser.show(getSupportFragmentManager(), "initial_" + FamilyChooserBottomSheet.class.getSimpleName());
                    }
                }
            });
        }
    }
}