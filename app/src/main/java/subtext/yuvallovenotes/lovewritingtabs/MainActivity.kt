package subtext.yuvallovenotes.lovewritingtabs

import android.os.Bundle
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import androidx.viewpager.widget.ViewPager
import androidx.appcompat.app.AppCompatActivity
import com.backendless.Backendless
import com.backendless.async.callback.AsyncCallback
import com.backendless.exceptions.BackendlessFault
import subtext.yuvallovenotes.BuildConfig
import subtext.yuvallovenotes.R
import subtext.yuvallovenotes.WhatsAppSender
import subtext.yuvallovenotes.backendless.LocalBackendless
import subtext.yuvallovenotes.loveletters.LoveOpener
import subtext.yuvallovenotes.lovewritingtabs.ui.main.SectionsPagerAdapter

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)
        val viewPager: ViewPager = findViewById(R.id.view_pager)
        viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = findViewById(R.id.tabs)
        tabs.setupWithViewPager(viewPager)
        val fab: FloatingActionButton = findViewById(R.id.fab)

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show()
            LocalBackendless.findLoveOpeners(object : AsyncCallback<List<LoveOpener>> {

                override fun handleResponse(response: List<LoveOpener>?) {
                    return response!!.toMutableList().shuffle()

//                    val sendWhatsapp = WhatsAppSender()
//                    sendWhatsapp.send(this@MainActivity, BuildConfig.MOBILE_NUMBER, text)

                }

                override fun handleFault(fault: BackendlessFault?) {
                    println("Backendless Error: ${fault.toString()}")
                }})
        }


    }

    fun retrieveData() {
        Backendless.Data.of(LoveOpener::class.java).find(object : AsyncCallback<List<LoveOpener>> {

            override fun handleResponse(response: List<LoveOpener>?) {
                val text = response!!.toMutableList().shuffle().toString()
                val sendWhatsapp = WhatsAppSender()
                sendWhatsapp.send(this@MainActivity, BuildConfig.MOBILE_NUMBER, text)

            }

            override fun handleFault(fault: BackendlessFault?) {
                println("Backendless Error: ${fault.toString()}")
            }
        })
    }
}