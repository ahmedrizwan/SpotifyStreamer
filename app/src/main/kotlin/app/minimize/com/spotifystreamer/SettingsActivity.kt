package app.minimize.com.spotifystreamer

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Request
import org.jsoup.Jsoup

/**
 * Created by ahmedrizwan on 7/2/15.
 */
public class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme_GreenTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        val mainToolbar = findViewById(R.id.mainToolbar) as Toolbar

        //ActionBar
        setSupportActionBar(mainToolbar)

        getFragmentManager().beginTransaction().add(R.id.container, MyPreferenceFragment()).commit()
        val supportActionBar = getSupportActionBar()
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true)
            supportActionBar.setTitle(getString(R.string.action_settings))
        }
        val client = OkHttpClient()

       Utility.runOnWorkerThread {
           val html: String
           val request = Request.Builder().url("https://www.spotify.com/us/select-your-country/").build()
           val response = client.newCall(request).execute()
           html = response.body().string()
           val doc = Jsoup.parse(html, "UTF-8")
           val content = doc.getElementById("content-main")
           val links = content.getElementsByTag("a")
           for (link in links) {
//               val linkRel = link.attr("rel")
//               val linkText = link.text()
           }
       }
    }

}
