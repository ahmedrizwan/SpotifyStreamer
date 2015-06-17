package app.minimize.com.spotifystreamer.Parcelables;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;

/**
 * Created by ahmedrizwan on 6/11/15.
 */
public class TrackParcelable implements Parcelable {
    public long songDuration;
    public String songName;
    public String albumName;
    public String artistName;
    public List<String> albumImageUrls;

    public TrackParcelable(Track track) {
        albumImageUrls = new ArrayList<>();
        this.songName = track.name;
        this.albumName = track.album.name;
        this.artistName = track.artists.get(0).name;
        this.songDuration = track.duration_ms;
        for (Image image : track.album.images) {
            this.albumImageUrls.add(image.url);
        }
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.songDuration);
        dest.writeString(this.songName);
        dest.writeString(this.albumName);
        dest.writeString(this.artistName);
        dest.writeStringList(this.albumImageUrls);
    }

    protected TrackParcelable(Parcel in) {
        this.songDuration = in.readLong();
        this.songName = in.readString();
        this.albumName = in.readString();
        this.artistName = in.readString();
        this.albumImageUrls = in.createStringArrayList();
    }

    public static final Parcelable.Creator<TrackParcelable> CREATOR = new Parcelable.Creator<TrackParcelable>() {
        public TrackParcelable createFromParcel(Parcel source) {
            return new TrackParcelable(source);
        }

        public TrackParcelable[] newArray(int size) {
            return new TrackParcelable[size];
        }
    };
}
