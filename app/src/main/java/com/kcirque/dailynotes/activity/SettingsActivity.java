package com.kcirque.dailynotes.activity;

import android.content.Intent;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.os.Bundle;
import android.view.MenuItem;

import com.kcirque.dailynotes.R;
import com.kcirque.dailynotes.utils.SharedPref;

public class SettingsActivity extends AppCompatPreferenceActivity {
    private static final String TAG = "SettingsActivity";
    private static SharedPref sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Settings");
        //load Fragment
        sharedPref = new SharedPref(this);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public static class SettingsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.setting_pref);

            Preference preference = findPreference(getString(R.string.master_pin_key));
            preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    startActivity(new Intent(getActivity(), ChangePinActivity.class));
                    return true;
                }
            });
            findPreference(getString(R.string.key_sort_by_pref)).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    ListPreference listPreference = (ListPreference) preference;
                    int index = listPreference.findIndexOfValue(newValue.toString());
                    preference.setSummary(index > 0 ? ((ListPreference) preference).getEntries()[index] : null);
                    sharedPref.putSortBy(newValue.toString());
                    return true;
                }
            });
            findPreference(getString(R.string.key_view_pref)).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    ListPreference listPreference = (ListPreference) preference;
                    int index = listPreference.findIndexOfValue(newValue.toString());
                    preference.setSummary(index > 0 ? ((ListPreference) preference).getEntries()[index] : null);
                    sharedPref.putView(newValue.toString());
                    return true;
                }
            });
        }
    }
}
