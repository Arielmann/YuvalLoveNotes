package subtext.yuvallovenotes.crossapplication.utils

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG

object LoveUtils {

    fun getFunctionName(): String {
        return object {}.javaClass.enclosingMethod.name
    }

    fun isNetworkAvailable(context: Context?): Boolean {
        if (context == null) return false
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null) {
                when {
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                        return true
                    }
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                        return true
                    }
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                        return true
                    }
                }
            }
        } else {
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            if (activeNetworkInfo != null && activeNetworkInfo.isConnected) {
                return true
            }
        }
        Toast.makeText(context, "No internet connection", LENGTH_LONG).show()
        return false
    }

    @SuppressLint("ClickableViewAccessibility")
    fun newOnTouchListenerWithViewClickUIBehaviour(): View.OnTouchListener {
      return View.OnTouchListener { view: View, motionEvent: MotionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
//                println("${YuvalLoveNotesApp.LOG_TAG} Action Down")
                    view.alpha = 0.5f
                }
                MotionEvent.ACTION_UP -> {
//                println("${YuvalLoveNotesApp.LOG_TAG} Action Up")
                    view.alpha = 1f
                }

                MotionEvent.ACTION_CANCEL -> {
                    view.alpha = 1f
                }
            }
            false
        }
    }

}
