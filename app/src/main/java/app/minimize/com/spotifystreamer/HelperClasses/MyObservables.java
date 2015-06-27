package app.minimize.com.spotifystreamer.HelperClasses;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import rx.Observable;

/**
 * Created by ahmedrizwan on 6/27/15.
 */
public class MyObservables {

    public static Observable.OnSubscribe<String> getSearchObservable(EditText editText){
        return subscriber -> {
                   editText.addTextChangedListener(new TextWatcher() {
                       @Override
                       public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {

                       }

                       @Override
                       public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
                            subscriber.onNext(s.toString());
                       }

                       @Override
                       public void afterTextChanged(final Editable s) {

                       }
                   });
        };
    }
}
