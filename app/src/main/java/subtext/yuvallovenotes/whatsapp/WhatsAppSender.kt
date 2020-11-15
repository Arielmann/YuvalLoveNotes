package subtext.yuvallovenotes.whatsapp

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import subtext.yuvallovenotes.R

class WhatsAppSender {
    fun send(context: Context, mobileNumber: String, msg: String) {
        //NOTE : please use with country code first 2digits without plus signed
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=$mobileNumber&text=$msg")))
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, context.getString(R.string.error_msg_whatsapp_not_installed), LENGTH_LONG).show()
        }
    }
}
