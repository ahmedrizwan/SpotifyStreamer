package app.minimize.com.spotifystreamer.Rx;

import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;
import rx.subjects.Subject;

/**
 * Created by ahmedrizwan on 6/28/15.
 */
public class RxBus {

    private final Subject<Object, Object> _bus = new SerializedSubject<>(PublishSubject.create());
    private static RxBus sRxBus;

    private RxBus() {
        //Private Constructor
    }

    public static RxBus getInstance() {
        if (sRxBus == null) {
            sRxBus = new RxBus();
        }
        return sRxBus;
    }

    public void send(Object o) {
        _bus.onNext(o);
    }

    public Observable<Object> toObserverable() {
        return _bus;
    }

}
