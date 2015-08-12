package app.minimize.com.spotifystreamer;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import app.minimize.com.spotifystreamer.Activities.Keys;
import app.minimize.com.spotifystreamer.HelperClasses.MediaPlayerHandler;

public class MyPreferenceFragment extends PreferenceFragment {
    private String TAG = "File Picker";
    MyPreferenceFragment myPreferenceFragment;
    SharedPreferences sharedPreferences;
    public HashMap<String,String> countryToCode;


    public static String getSelectedCountry(Context context) {
        return context.getSharedPreferences(Keys.SHARED_PREFS,Context.MODE_PRIVATE).getString(Keys.KEY_LOCATION,"us");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myPreferenceFragment = this;
        addPreferencesFromResource(R.xml.pref);
        sharedPreferences = getActivity().getSharedPreferences(Keys.SHARED_PREFS, Context.MODE_PRIVATE);
        createCountryToCodeMap();
        setupLocationPreference(sharedPreferences);
        setupNotificationPreference(sharedPreferences);
    }

    private void createCountryToCodeMap() {
        countryToCode = new HashMap<>();
        countryToCode.put("USA", "us");
        countryToCode.put("United Kingdom","uk");
        countryToCode.put("Australia", "au");
        countryToCode.put("Hong Kong", "hk");
        countryToCode.put("Singapore", "sg");
        //TODO: Add more countries
    }

    private void setupNotificationPreference(final SharedPreferences sharedPreferences) {
        CheckBoxPreference switchPreference = (CheckBoxPreference) findPreference(getString(R.string.preference_notification_key));
        switchPreference.setChecked(isNotificationModeOn(getActivity()));

        switchPreference.setOnPreferenceChangeListener((preference, newValue) -> {
            sharedPreferences.edit()
                    .putBoolean(Keys.KEY_NOTIFICATION_PREF, ((Boolean) newValue))
                    .apply();
            switchPreference.setChecked(((Boolean) newValue));
            //make the mediaplayer handler re-send trackParcelables
            try {
                MediaPlayerHandler.getInstance()
                        .resendPlayerEvents();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        });
    }

    private void setupLocationPreference(final SharedPreferences sharedPreferences) {
        final Preference preference = findPreference(getString(R.string.preference_location_key));
        final String currentLocation = getKeyForValue(getSelectedCountry(getActivity()));
        preference.setSummary(currentLocation);
        List<String> listOfLocations = getAvailableLocations();
        preference.setOnPreferenceClickListener(preference1 -> {

            new MaterialDialog.Builder(getActivity())
                    .title(getString(R.string.locations))
                    .items(listOfLocations.toArray(new CharSequence[listOfLocations.size()]))
                    .itemsCallback((dialog, view, which, text) -> {
                        sharedPreferences.edit()
                                .putString(Keys.KEY_LOCATION, countryToCode.get(text.toString()))
                                .apply();
                        preference.setSummary(text.toString());
                    })
                    .show();
            return false;
        });
    }

    private String getKeyForValue(final String value) {
        for (String country : countryToCode.keySet()) {
            if(countryToCode.get(country).equals(value))
                return country;
        }
        return "";
    }


    public List<String> getAvailableLocations() {
        List<String> countries = new ArrayList<>();
        for (String country : countryToCode.keySet()) {
            countries.add(country);
        }
        return countries;
    }


    public static boolean isNotificationModeOn(final Context context) {
        return context.getSharedPreferences(Keys.SHARED_PREFS,Context.MODE_PRIVATE).getBoolean(Keys.KEY_NOTIFICATION_PREF,true);
    }
}