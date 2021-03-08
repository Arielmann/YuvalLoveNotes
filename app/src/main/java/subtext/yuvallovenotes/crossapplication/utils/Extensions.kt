package subtext.yuvallovenotes.crossapplication.utils

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import android.util.Log
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.Phonenumber
import subtext.yuvallovenotes.R
import java.lang.Exception


private const val TAG: String = "LoveLetter Extensions"

fun <T> LiveData<T?>.observeOnce(lifecycleOwner: LifecycleOwner, observer: Observer<T>) {
    observe(lifecycleOwner, object : Observer<T?> {
        override fun onChanged(t: T?) {
            observer.onChanged(t)
            removeObserver(this)
        }
    })
}

/**
 * Returns the international prefix required to dial this number from around the world, with the '+' char.
 * Example: for a device with us locale, the result will be '+1'
 */
fun PhoneNumberUtil.getDeviceDefaultCountryCode(): String {
    val localeString = LoveUtils.getDeviceCountryCode()
    val countyCode = PhoneNumberUtil.getInstance().getCountryCodeForRegion(localeString)
    return "+$countyCode"
}

fun PhoneNumberUtil.parse(number: String): Phonenumber.PhoneNumber? {
    var result: Phonenumber.PhoneNumber? = null
    return try {
        return PhoneNumberUtil.getInstance().parse(number, "")
        /*      if (PhoneNumberUtil.getInstance().isValidNumber(result)) {
                  result
              } else {
                  null
              }*/
    } catch (e: NumberParseException) {
        e.printStackTrace()
        result
    } catch (e: NumberFormatException) {
        e.printStackTrace()
        result
    }
}

fun PhoneNumberUtil.isPhoneNumberValid(number: String): Boolean {
    return try {
        val phoneNumber = PhoneNumberUtil.getInstance().parse(number, "")
        PhoneNumberUtil.getInstance().isValidNumber(phoneNumber)
    } catch (e: NumberParseException) {
        e.printStackTrace()
        false
    } catch (e: NumberFormatException) {
        e.printStackTrace()
        false
    }
}

fun PhoneNumberUtil.isPhoneNumberValid(countryCode: String, number: String): Boolean {
    if (!countryCode.contains("+")) {
        Log.w(TAG, "County code does not contain + character and therefore is invalid")
        return false
    }

    return isPhoneNumberValid(countryCode + number)
}

/**
 * Returns a phone number of a contact picked by user through the
 * ACTION_PICK activity for result.
 * It favours Phone.TYPE_MOBILE rather than Phone.TYPE_HOME or Phone.TYPE_WORK numbers.
 * Method does not guarantee that the returned number would be valid
 * returns empty string if number wasn't found
 */
private fun PhoneNumberUtil.findRawPhoneNumberFromContactURI(context: Context, data: Uri): String {
    val cr: ContentResolver = context.contentResolver
    var number = ""
    val cursor = cr.query(data, null, null, null, null)
    try {
        if (cursor != null && cursor.moveToFirst()) {
            val contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID))
            val phones = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId, null, null)
            if (phones != null) {
                while (phones.moveToNext()) {
                    number = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                    val type = phones.getInt(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE))
                    when (type) {
                        ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE -> {
                            return number
                        }
                    }
                }
                phones.close()
            }
        } else {
            Log.e(TAG, "Contact picking process failed")
        }
        cursor?.close()
    } catch (e: IllegalStateException) {
        e.printStackTrace()
        cursor?.close()
    } catch (e: Exception) {
        //This feature allows user to access files from multiple platforms, therefore be prepared for anything
        e.printStackTrace()
        cursor?.close()
    }
    return number
}

/**
 *  Parsing a contact URI input. When done calling a completion block containing:
 *  1. The contact's region (null if does not exists in contact raw number)
 *  2. The contact's local number
 */
fun PhoneNumberUtil.getPhoneNumberFromUserContactChoice(context: Context, data: Intent, onCompletion: (String?, String) -> Unit) {

    if (data.data != null) {
        var nationalNumber = PhoneNumberUtil.getInstance().findRawPhoneNumberFromContactURI(context, data.data!!)
        var countryCode: String? = null
        if (nationalNumber.contains("+")) { //Did user chose an international number?
            val interNationalPhoneNumber: Phonenumber.PhoneNumber? = PhoneNumberUtil.getInstance().parse(nationalNumber)
            nationalNumber = interNationalPhoneNumber?.nationalNumber.toString()
            countryCode = "+".plus(interNationalPhoneNumber?.countryCode.toString())
        } else {
            if (nationalNumber.isNotBlank()) {
                nationalNumber = nationalNumber.replace("[^0-9]".toRegex(), "")
                nationalNumber = nationalNumber.removeRange(0..0)
            }
        }
        onCompletion.invoke(countryCode, nationalNumber)
    } else {
        Toast.makeText(context, context.getString(R.string.error_parsing_contact_number), LENGTH_LONG).show()
    }
}


