package app.minimize.com.spotifystreamer;

import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

public class TracksActivity extends AppCompatActivity {

    public static final String IMAGE_URL = "ImageUrl";
    public static final String ARTIST_NAME = "ArtistName";
    public static final String ARTIST_ID = "ArtistId";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracks);

        getSupportFragmentManager().beginTransaction().add(R.id.tracksFragmentContainer, new TracksFragment()).commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
