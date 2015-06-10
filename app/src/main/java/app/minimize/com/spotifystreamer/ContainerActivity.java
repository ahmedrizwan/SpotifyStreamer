package app.minimize.com.spotifystreamer;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.ChangeBounds;
import android.transition.ChangeImageTransform;
import android.transition.ChangeTransform;
import android.transition.TransitionSet;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

public class ContainerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.container_activity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.mainToolbar);
        setSupportActionBar(toolbar);
        setTitle(getString(R.string.app_name));
        //make transaction for the artists fragment
        getSupportFragmentManager().beginTransaction()
                .add(R.id.container, new TestFragmentOne())
                .commit();
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
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static class TestFragmentOne extends Fragment{
        @Nullable
        @Override
        public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.test_one,container,false);
            rootView.findViewById(R.id.imageViewArtist).setOnClickListener(view -> {
                final TransitionSet transitionSet = new TransitionSet();
                transitionSet.addTransition(new ChangeImageTransform());
                transitionSet.addTransition(new ChangeBounds());
                transitionSet.addTransition(new ChangeTransform());
                transitionSet.setDuration(300);

                setSharedElementReturnTransition(transitionSet);

                Fragment fragment = new TestFragmentTwo();
                fragment.setSharedElementEnterTransition(transitionSet);

                FragmentTransaction trans = getActivity().getSupportFragmentManager().beginTransaction();
                trans.replace(R.id.container, fragment);
                trans.addToBackStack(null);
                trans.addSharedElement(rootView.findViewById(R.id.imageViewArtist), getString(R.string.artists_image_transition));
                trans.commit();
            });


            return rootView;
        }
    }

    public static class TestFragmentTwo extends Fragment{
        @Nullable
        @Override
        public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.test_two,container,false);

            return rootView;
        }
    }
}
