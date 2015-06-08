package app.minimize.com.spotifystreamer;

/**
 * Created by ahmedrizwan on 6/8/15.
 */
public class ArtistModel {
    public String name;
    public String imageUrl;

    public ArtistModel(final String name, final String imageUrl) {
        this.name = name;
        this.imageUrl = imageUrl;
    }
    public ArtistModel(final String name) {
       this(name,"");
    }
}
