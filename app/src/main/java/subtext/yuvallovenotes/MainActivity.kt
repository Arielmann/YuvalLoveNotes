package subtext.yuvallovenotes

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.startapp.sdk.ads.banner.Banner
import weborb.util.ThreadContext.context


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /*todo: set style to blank screen or it will become a graphic glitch when keyboard closes*/
        setTheme(R.style.BlankScreen)
        setContentView(R.layout.activity_main)
    }


}