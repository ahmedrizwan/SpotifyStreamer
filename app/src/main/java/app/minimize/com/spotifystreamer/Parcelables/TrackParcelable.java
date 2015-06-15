package app.minimize.com.spotifystreamer.Parcelables;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.models.Image;

/**
 * Created by ahmedrizwan on 6/11/15.
 */
public class TrackParcelable implements Parcelable {
    public String songName;
    public String albumName;
    public List<String> albumImageUrls;

    public TrackParcelable(final String songName, final String albumName, final List<Image> albumImages) {
        albumImageUrls = new ArrayList<>();
        this.songName = songName;
        this.albumName = albumName;
        for(Image image:albumImages){
            this.albumImageUrls.add(image.url);
        }
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.songName);
        dest.writeString(this.albumName);
        dest.writeStringList(this.albumImageUrls);
    }

    protected TrackParcelable(Parcel in) {
        this.songName = in.readString();
        this.albumName = in.readString();
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
