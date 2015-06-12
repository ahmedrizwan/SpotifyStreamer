package app.minimize.com.spotifystreamer;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import kaaes.spotify.webapi.android.models.Image;

/**
 * Created by ahmedrizwan on 6/11/15.
 */
public class ArtistParcelable implements Parcelable {
    public String id;
    public String name;
    public List<String> artistImageUrls = Collections.emptyList();

    public ArtistParcelable(final String id, final String name, final List<Image> images) {
        this.artistImageUrls = new ArrayList<>();
        this.id = id;
        this.name = name;
        for(Image image:images){
            this.artistImageUrls.add(image.url);
        }

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.name);
        dest.writeStringList(this.artistImageUrls);
    }

    public ArtistParcelable() {
    }

    protected ArtistParcelable(Parcel in) {
        this.id = in.readString();
        this.name = in.readString();
        this.artistImageUrls = in.createStringArrayList();
    }

    public static final Parcelable.Creator<ArtistParcelable> CREATOR = new Parcelable.Creator<ArtistParcelable>() {
        public ArtistParcelable createFromParcel(Parcel source) {
            return new ArtistParcelable(source);
        }

        public ArtistParcelable[] newArray(int size) {
            return new ArtistParcelable[size];
        }
    };
}
