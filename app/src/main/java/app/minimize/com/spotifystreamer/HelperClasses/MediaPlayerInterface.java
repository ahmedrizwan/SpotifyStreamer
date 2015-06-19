package app.minimize.com.spotifystreamer.HelperClasses;

import android.content.Context;

/**
 * Created by ahmedrizwan on 2/9/15.
 */
public interface MediaPlayerInterface {

    void playing(int duration, int progress);

    void stopped(final int duration);

    void paused();

    enum MediaPlayerState {Playing, Stopped, Paused, Idle}

    public Context getContext();

}
