package subtext.yuvallovenotes

import android.os.Bundle
import android.util.Log.d
import androidx.appcompat.app.AppCompatActivity
import subtext.yuvallovenotes.registration.ui.EnterLoverPhoneNumberFragment


class MainActivity : AppCompatActivity() {

    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /*todo: set style to blank screen or it will become a graphic glitch when keyboard closes*/
        setTheme(R.style.AppTheme_NoActionBar)
        setContentView(R.layout.activity_main)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        d(TAG, "$TAG onRequestPermissionsResult")
    }
}