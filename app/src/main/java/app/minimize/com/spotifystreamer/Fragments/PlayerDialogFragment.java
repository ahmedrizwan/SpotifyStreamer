package app.minimize.com.spotifystreamer.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import app.minimize.com.spotifystreamer.Activities.ContainerActivity;
import app.minimize.com.spotifystreamer.Activities.Keys;
import app.minimize.com.spotifystreamer.MediaPlayerService;
import app.minimize.com.spotifystreamer.Parcelables.TrackParcelable;
import app.minimize.com.spotifystreamer.R;
import app.minimize.com.spotifystreamer.Utility;
import app.minimize.com.spotifystreamer.Views.NextButton;
import app.minimize.com.spotifystreamer.Views.PlayButton;
import app.minimize.com.spotifystreamer.Views.PreviousButton;
import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by ahmedrizwan on 6/15/15.
 */
public class PlayerDialogFragment extends DialogFragment {

    @InjectView(R.id.textViewTrackName)
    TextView textViewTrackName;
    @InjectView(R.id.seekBarPlayer)
    SeekBar seekBarPlayer;
    @InjectView(R.id.textViewStartTime)
    TextView textViewStartTime;
    @InjectView(R.id.textViewEndTime)
    TextView textViewEndTime;
    @InjectView(R.id.imageViewPrevious)
    PreviousButton imageViewPrevious;
    @InjectView(R.id.imageViewPlay)
    PlayButton imageViewPlay;
    @InjectView(R.id.imageViewNext)
    NextButton imageViewNext;

    private TracksFragment tracksFragment;
    private TrackParcelable mTrackParcelable;

    public static PlayerDialogFragment getInstance(TracksFragment tracksFragment) {
        PlayerDialogFragment playerDialogFragment = new PlayerDialogFragment();
        playerDialogFragment.tracksFragment = tracksFragment;
        return playerDialogFragment;
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_player, container, false);
        ButterKnife.inject(this, rootView);
        ((AppCompatActivity) tracksFragment.getActivity()).getSupportActionBar()
                .setTitle("Player");

        mTrackParcelable = getArguments().getParcelable(getString(R.string.key_tracks_parcelable));

        if (mTrackParcelable != null) {
            textViewTrackName.setText(mTrackParcelable.songName + " "
                    + mTrackParcelable.artistName + " " + mTrackParcelable.albumName);

            playTrack();
        }

        int colorPrimary = Utility.getPrimaryColorFromSelectedTheme(getActivity());
        DrawableCompat.setTint(seekBarPlayer.getThumb(), colorPrimary);
        DrawableCompat.setTint(seekBarPlayer.getProgressDrawable(), colorPrimary);
        return rootView;
    }

    private void playTrack() {
        Intent intent = new Intent(getActivity(),
                MediaPlayerService.class);
        intent.putExtra(Keys.KEY_TRACK_URL, mTrackParcelable.previewUrl);
        intent.putExtra(Keys.KEY_TRACK_NAME, mTrackParcelable.songName);
        intent.putExtra(Keys.KEY_PLAYER_RECEIVER,
                ((ContainerActivity) getActivity()).getPlayerReceiver());
        getActivity().startService(intent);
    }

    private void showLog(final String message) {
        Log.e("Exception", message);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

}
