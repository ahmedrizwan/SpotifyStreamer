package app.minimize.com.spotifystreamer.Activities;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import app.minimize.com.spotifystreamer.R;
import app.minimize.com.spotifystreamer.databinding.ActivitySettingsBinding;

/**
 * Created by ahmedrizwan on 7/6/15.
 */
public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivitySettingsBinding activitySettingsBinding = DataBindingUtil.setContentView(this, R.layout.activity_settings);
        activitySettingsBinding.setTitle(getString(R.string.action_settings));
    }
}
