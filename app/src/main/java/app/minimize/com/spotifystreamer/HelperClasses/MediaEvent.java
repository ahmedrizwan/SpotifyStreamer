package app.minimize.com.spotifystreamer.HelperClasses;

public class MediaEvent {

    public EventType getEventType() {
        return mEventType;
    }

    public static enum EventType {Play,Pause,Stop,Next,Previous}

    private final EventType mEventType;

    public MediaEvent(EventType event) {
        mEventType = event;
    }


}
