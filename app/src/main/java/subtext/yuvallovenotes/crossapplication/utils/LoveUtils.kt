package subtext.yuvallovenotes.crossapplication.utils

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.telephony.TelephonyManager
import android.util.Log
import androidx.annotation.ArrayRes
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import subtext.yuvallovenotes.R
import subtext.yuvallovenotes.YuvalLoveNotesApp
import java.lang.reflect.InvocationTargetException
import java.util.*


object LoveUtils {

    private val TAG = LoveUtils::class.simpleName

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
        return false
    }


    fun getDeviceLocale(context: Context): String {
        var countryCode: String?

        // Try to get country code from TelephonyManager service
        val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        // Query first getSimCountryIso()
        countryCode = tm.simCountryIso
        if (countryCode != null && countryCode.length == 2) return countryCode.toUpperCase(Locale.getDefault())
        countryCode = if (tm.phoneType == TelephonyManager.PHONE_TYPE_CDMA) {
            // Special case for CDMA Devices
            getCDMACountryIso()
        } else {
            // For 3G devices (with SIM) query getNetworkCountryIso()
            tm.networkCountryIso
        }
        if (countryCode != null && countryCode.length == 2) return countryCode.toUpperCase(Locale.getDefault())

        // If network country not available (tablets maybe), get country code from Locale class
        countryCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.resources.configuration.locales[0].country
        } else {
            context.resources.configuration.locale.country
        }
        return if (countryCode != null && countryCode.length == 2) countryCode.toUpperCase(Locale.getDefault()) else context.getString(R.string.default_country_code)

        // General fallback to "israel"
    }

    @SuppressLint("PrivateApi")
    private fun getCDMACountryIso(): String? {
        try {
            // Try to get country code from SystemProperties private class
            val systemProperties = Class.forName("android.os.SystemProperties")
            val get = systemProperties.getMethod("get", String::class.java)

            // Get homeOperator that contain MCC + MNC
            val homeOperator = get.invoke(systemProperties,
                    "ro.cdma.home.operator.numeric") as String

            // First three characters (MCC) from homeOperator represents the country code
            val mcc = homeOperator.substring(0, 3).toInt()
            when (mcc) {
                330 -> return "PR"
                310 -> return "US"
                311 -> return "US"
                312 -> return "US"
                316 -> return "US"
                283 -> return "AM"
                460 -> return "CN"
                455 -> return "MO"
                414 -> return "MM"
                619 -> return "SL"
                450 -> return "KR"
                634 -> return "SD"
                434 -> return "UZ"
                232 -> return "AT"
                204 -> return "NL"
                262 -> return "DE"
                247 -> return "LV"
                255 -> return "UA"
            }
        } catch (ignored: ClassNotFoundException) {
        } catch (ignored: NoSuchMethodException) {
        } catch (ignored: IllegalAccessException) {
        } catch (ignored: InvocationTargetException) {
        } catch (ignored: NullPointerException) {
        }
        return null
    }

    fun setupFragmentDefaultToolbar(parentFragment: Fragment, toolbar: Toolbar) {
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_24);
        toolbar.setNavigationOnClickListener {
            Log.d(TAG, "Navigating to previous screen")
            parentFragment.findNavController().popBackStack()
        }
    }

    fun getAllItemsFromArrayFile(@ArrayRes resource : Int): MutableList<String> {
        return YuvalLoveNotesApp.context.resources.getStringArray(resource).toMutableList()
    }
}
