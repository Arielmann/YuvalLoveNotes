package subtext.yuvallovenotes

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.backendless.Backendless
import com.backendless.async.callback.AsyncCallback
import com.backendless.exceptions.BackendlessFault


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Backendless.initApp(this, getString(R.string.ln_backendless_app_id), getString(R.string.ln_backendless_android_api_key))

        Thread {
            val lovePhrase = LoveOpener();
            lovePhrase.text = "אהובתי יפתי"

            Backendless.Data.of(LoveOpener::class.java).save(lovePhrase, object : AsyncCallback<LoveOpener> {
                override fun handleResponse(response: LoveOpener?) {
                    retrieveData()
                }

                override fun handleFault(fault: BackendlessFault?) {
                    println("Backendless error ${fault.toString()}")
                }
            })
        }.start()
    }

    fun retrieveData() {
        Backendless.Data.of(LoveOpener::class.java).find(object : AsyncCallback<List<LoveOpener>> {

            override fun handleResponse(response: List<LoveOpener>?) {
                println("ariel mann ${response.toString()}")
            }

            override fun handleFault(fault: BackendlessFault?) {
            }


        })
    }
}