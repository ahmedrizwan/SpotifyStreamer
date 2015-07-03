package app.minimize.com.spotifystreamer;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.view.View;

import java.util.List;

import app.minimize.com.spotifystreamer.Activities.Keys;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;

public class MyPreferenceFragment extends PreferenceFragment implements View.OnClickListener {
        private String TAG = "File Picker";
        MyPreferenceFragment myPreferenceFragment;
        SharedPreferences sharedPreferences;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            myPreferenceFragment = this;
            addPreferencesFromResource(R.xml.pref);
            sharedPreferences = getActivity().getSharedPreferences(Keys.SHARED_PREFS, Context.MODE_PRIVATE);

            setupLocationPreference(sharedPreferences);

        }

    private void setupLocationPreference(final SharedPreferences sharedPreferences) {
        final Preference preference = findPreference(getString(R.string.preference_location_key));
        List<String> listOfLocations = getAvailableLocations();
        preference.setOnPreferenceClickListener(preference1 -> {
            final String  currentLocation = sharedPreferences.getString(Keys.KEY_LOCATION, "US");
            //TODO: show list dialog here with all the locations listed

            return false;
        });
    }

    private List<String> getAvailableLocations() {

        Utility.runOnWorkerThread(() -> {
            SpotifyApi spotifyApi = new SpotifyApi();
            SpotifyService spotifyService = spotifyApi.getService();
            //artists

            return null;
        });
        return null;
    }

    @Override
        public void onClick(final View v) {

        }
    }