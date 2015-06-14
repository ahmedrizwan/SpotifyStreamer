package app.minimize.com.spotifystreamer;

import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

public class ContainerActivity extends AppCompatActivity {

    boolean mTwoPane;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_container);
        Toolbar toolbar = (Toolbar) findViewById(R.id.mainToolbar);
        setSupportActionBar(toolbar);

        if(findViewById(R.id.tracksContainer)!=null){
            mTwoPane = true;
        } else {
            //make transaction for the artists fragment
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.container, new ArtistsFragment())
                        .commit();
            }
        }
    }

    public boolean isTwoPane(){
        return mTwoPane;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_about) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(getString(R.string.about_message));
            builder.setCancelable(true);
            builder.setPositiveButton(getString(R.string.close), (dialogInterface, i) -> dialogInterface.dismiss());

            AlertDialog dialog = builder.create();
            dialog.show();
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.color_primary));
            return true;
        } else if(id== android.R.id.home) {
            getSupportFragmentManager().popBackStack();
        }

        return super.onOptionsItemSelected(item);
    }

}
