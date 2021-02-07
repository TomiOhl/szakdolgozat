package com.tomi.ohl.szakdoga;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.ListenerRegistration;
import com.tomi.ohl.szakdoga.controller.FamilyController;

public class SettingsFragment extends PreferenceFragmentCompat {

    private MainActivity mainActivity;
    FirebaseUser user;
    int counter;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);

        mainActivity = (MainActivity) getActivity();
        user = FirebaseAuth.getInstance().getCurrentUser();

        // Néhány beállítás megjelenített értékének beállítása
        final EditTextPreference namePref = getPreferenceManager().findPreference("displayName");
        if (namePref != null) {
            namePref.setText(user.getDisplayName());
            namePref.setOnPreferenceChangeListener((preference, newValue) -> {
                updateProfile((String) newValue);
                return true;
            });
        }

        final Preference versionPref = getPreferenceManager().findPreference("version");
        if (versionPref != null) {
            versionPref.setSummary(BuildConfig.VERSION_NAME);
            versionPref.setOnPreferenceClickListener(preference -> {
                counter++;
                if (counter > 4) {
                    counter = 0;
                    Toast.makeText(getContext(), R.string.whatsup, Toast.LENGTH_SHORT).show();
                }
                return true;
            });
        }
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        switch (preference.getKey()){
            case "family":
                FamilyChooserBottomSheet familyChooser = new FamilyChooserBottomSheet();
                familyChooser.show(getChildFragmentManager(), "settings_" + FamilyChooserBottomSheet.class.getSimpleName());
                break;
            case "logout":
                logout();
                break;
        }
        return super.onPreferenceTreeClick(preference);
    }

    private void updateProfile(String newName) {
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
            .setDisplayName(newName)
            .build();
        user.updateProfile(profileUpdates);
    }

    private void logout() {
        // Regisztrált db listenerek eltávolítása
        for(ListenerRegistration elem : ((MainActivity)requireActivity()).dbListeners) {
            elem.remove();
        }
        // User kijelentkeztetése
        FirebaseAuth.getInstance().signOut();
        FamilyController.getInstance().setCurrentFamily(null);
        // Átirányítás a login képernyőre
        startActivity(new Intent(mainActivity, LoginActivity.class));
        mainActivity.finish();
    }

}