package subtext.yuvallovenotes

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /*todo: set style to blank screen or it will become a graphic glitch when keyboard closes*/
        setTheme(R.style.AppTheme_NoActionBar)
        setContentView(R.layout.activity_main)
    }


}