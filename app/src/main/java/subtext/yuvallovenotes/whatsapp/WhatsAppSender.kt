package subtext.yuvallovenotes.whatsapp

import android.content.Context
import android.content.Intent
import android.net.Uri

class WhatsAppSender {
    fun send(context: Context?, mobileNumber: String?, msg: String) {
        //NOTE : please use with country code first 2digits without plus signed

        //NOTE : please use with country code first 2digits without plus signed
        try {
            context?.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=$mobileNumber&text=$msg")))
        } catch (e: Exception) {
            e.printStackTrace()
            //whatsapp app not install
        }
    }

}
