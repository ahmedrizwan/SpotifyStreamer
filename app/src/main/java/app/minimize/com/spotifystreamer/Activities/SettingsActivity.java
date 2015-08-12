package app.minimize.com.spotifystreamer.Activities;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import app.minimize.com.spotifystreamer.MyPreferenceFragment;
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
        //load the fragment
        getFragmentManager().beginTransaction()
                .replace(R.id.container, new MyPreferenceFragment())
                .commit();
        setSupportActionBar(activitySettingsBinding.mainToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

}
