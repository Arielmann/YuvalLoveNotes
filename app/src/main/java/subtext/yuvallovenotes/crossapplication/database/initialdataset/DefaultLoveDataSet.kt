package subtext.yuvallovenotes.crossapplication.database.initialdataset

import android.content.SharedPreferences
import android.util.Log
import com.backendless.Backendless
import com.backendless.async.callback.AsyncCallback
import com.backendless.exceptions.BackendlessFault
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.get
import subtext.yuvallovenotes.R
import subtext.yuvallovenotes.YuvalLoveNotesApp
import subtext.yuvallovenotes.crossapplication.models.loveitems.LoveLetter
import subtext.yuvallovenotes.crossapplication.utils.LoveUtils
import java.util.*


object DefaultLoveDataSet : InitialLettersDataSet {


    private val TAG: String = DefaultLoveDataSet::class.java.simpleName
    private val shardPrefs = get(SharedPreferences::class.java)
    private val userName = shardPrefs.getString(YuvalLoveNotesApp.context.getString(R.string.pref_key_user_name), YuvalLoveNotesApp.context.getString(R.string.user_name_as_closure_fallback))
    private val loverNickname = shardPrefs.getString(YuvalLoveNotesApp.context.getString(R.string.pref_key_lover_nickname), YuvalLoveNotesApp.context.getString(R.string.lover_nickname_fallback))

    private val letters = generateLettersList()

    private fun generateLettersList(): MutableList<LoveLetter> {
      /*  val result = mutableListOf<LoveLetter>()
        val lettersTexts = LoveUtils.getAllItemsFromArrayFile(R.array.letters)
        lettersTexts.forEachIndexed { index, text ->
            val lines: List<String> = text.split(System.getProperty("line.separator"))
            var trimmedText = ""
            lines.forEach{
                trimmedText += if(it.isNotBlank()) {
                    it.trim() + "\n"
                }else{
                   "\n"
                }
            }
            val letter = LoveLetter(UUID.randomUUID().toString(), trimmedText, index)
//            Log.d(TAG, "letter: ${letter.text}")
            result.add(letter)
        }
        return result*/
        return mutableListOf()
    }

    override fun getLetters(): MutableList<LoveLetter> {
        return letters
    }

    private fun generateLetterWithLoveNickname(loveLetter: LoveLetter): LoveLetter {
        var text = ""

        text = text.plus(loverNickname?.trim() + "," + "\n\n")

        text = text.plus(loveLetter.text.trim() + "\n\n")

        val result = LoveLetter(loveLetter.id, text.trim())
        return result
    }

}
