package subtext.yuvallovenotes.crossapplication.database.initialdataset

import android.content.SharedPreferences
import org.koin.java.KoinJavaComponent.get
import subtext.yuvallovenotes.R
import subtext.yuvallovenotes.YuvalLoveNotesApp
import subtext.yuvallovenotes.crossapplication.models.loveitems.LoveClosure
import subtext.yuvallovenotes.crossapplication.models.loveitems.LoveOpener
import subtext.yuvallovenotes.crossapplication.models.loveitems.LovePhrase
import subtext.yuvallovenotes.crossapplication.utils.LoveUtils
import java.util.*

object DefaultLoveDataSet : InitialLettersDataSet{

    private val shardPrefs = get(SharedPreferences::class.java)
    private val userName = shardPrefs.getString(YuvalLoveNotesApp.context.getString(R.string.pref_key_user_name), YuvalLoveNotesApp.context.getString(R.string.user_name_as_closure_fallback))
    private val loverName = shardPrefs.getString(YuvalLoveNotesApp.context.getString(R.string.pref_key_lover_nickname), YuvalLoveNotesApp.context.getString(R.string.lover_name_fallback))

   private val openers = generateOpenersList()
   private val phrases = generatePhrasesList()
   private val closures = generateClosuresList()

    private fun generateOpenersList(): MutableList<LoveOpener> {
        val result = mutableListOf<LoveOpener>()
        val openersTexts = LoveUtils.getAllItemsFromArrayFile(R.array.openers)
        result.add(LoveOpener(UUID.randomUUID().toString(), loverName!!))
        openersTexts.forEach { text ->
            result.add(LoveOpener(UUID.randomUUID().toString(), text))
        }
        return result
    }

    private fun generatePhrasesList(): MutableList<LovePhrase> {
        val result = mutableListOf<LovePhrase>()
        val phrasesTexts = LoveUtils.getAllItemsFromArrayFile(R.array.phrases)
        phrasesTexts.forEach { text ->
            result.add(LovePhrase(UUID.randomUUID().toString(), text))
        }
        return result
    }

    private fun generateClosuresList(): MutableList<LoveClosure> {
        val result = mutableListOf<LoveClosure>()
        val closuresTexts = LoveUtils.getAllItemsFromArrayFile(R.array.closures)
        result.add(LoveClosure(UUID.randomUUID().toString(), userName!!))
        closuresTexts.forEach { text ->
            result.add(LoveClosure(UUID.randomUUID().toString(), text))
        }
        return result
    }

    override fun getOpeners(): MutableList<LoveOpener> {
        return openers
    }

    override fun getPhrases(): MutableList<LovePhrase> {
        return phrases
    }

    override fun getClosures(): MutableList<LoveClosure> {
        return closures
    }

}
