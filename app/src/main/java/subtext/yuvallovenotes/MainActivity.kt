package subtext.yuvallovenotes

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.backendless.Backendless

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Backendless.initApp(this, getString(R.string.ln_backendless_app_id), getString(R.string.ln_backendless_android_api_key))

        new Thread(new Runnable() {
            public void run() {
                // synchronous backendless API call here:
                Backendless.XXXX()
            }
        }).start();

    }
}