package subtext.yuvallovenotes.backendless

import android.content.Context
import android.widget.Toast
import com.backendless.Backendless
import com.backendless.async.callback.AsyncCallback
import com.backendless.exceptions.BackendlessFault
import subtext.yuvallovenotes.R
import subtext.yuvallovenotes.YuvalLoveNotesApp.Companion.LOG_TAG
import subtext.yuvallovenotes.loveletters.LoveClosure
import subtext.yuvallovenotes.loveletters.LoveItem
import subtext.yuvallovenotes.loveletters.LoveOpener
import subtext.yuvallovenotes.loveletters.LovePhrase
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future

object LoveNotesBackendless {

    fun findAllLoveData(callback: AsyncCallback<List<LoveItem>>) {
        val taskExecutor: ExecutorService = Executors.newFixedThreadPool(3)
        val tasks: MutableList<out Callable<MutableList<out LoveItem>>> = generateCallables() // your tasks
        val futures: List<Future<*>> = taskExecutor.invokeAll(tasks)

        val finalResults: MutableList<LoveItem> = mutableListOf()
        futures.forEach {
            finalResults.addAll(it.get() as List<LoveItem>)
        }
        callback.handleResponse(finalResults)
        taskExecutor.shutdown()
    }

    private fun generateCallables(): MutableList<out Callable<MutableList<out LoveItem>>> {
        val result: MutableList<Callable<MutableList<out LoveItem>>> = mutableListOf()
        val loveOpenersCallable: Callable<MutableList<out LoveItem>> = Callable {
            Backendless.Data.of(LoveOpener::class.java).find()
        }

        val lovePhrasesCallable: Callable<MutableList<out LoveItem>> = Callable {
            Backendless.Data.of(LovePhrase::class.java).find()
        }

        val loveClosuresCallable: Callable<MutableList<out LoveItem>> = Callable {
            Backendless.Data.of(LoveClosure::class.java).find()
        }

        result.add(loveOpenersCallable)
        result.add(lovePhrasesCallable)
        result.add(loveClosuresCallable)
        return result
    }

    fun findLoveOpeners(callback: AsyncCallback<List<LoveOpener>>) {
        Thread {
            Backendless.Data.of(LoveOpener::class.java).find(callback)
        }.start()
    }

    fun findLovePhrases(callback: AsyncCallback<List<LoveOpener>>) {
        Thread {
            Backendless.Data.of(LoveOpener::class.java).find(callback)
        }.start()
    }

    fun findLoveClosures(callback: AsyncCallback<List<LoveOpener>>) {
        Thread {
            Backendless.Data.of(LoveOpener::class.java).find(callback)
        }.start()
    }

    fun saveLoveOpener(context: Context, opener: LoveOpener) {

        if (verifyItem(context, opener, ",")) {

            Thread {
                Backendless.Data.of(LoveOpener::class.java).save(opener, object : AsyncCallback<LoveOpener> {
                    override fun handleResponse(response: LoveOpener?) {
                        println("Backendless response ${response.toString()}")
                        Toast.makeText(context, R.string.success_message, Toast.LENGTH_LONG).show()
                    }

                    override fun handleFault(fault: BackendlessFault?) {
                        println("Backendless error ${fault.toString()}")
                    }
                })
            }.start()
        }
    }

    fun saveLovePhrase(context: Context, phrase: LovePhrase) {
        if (verifyItem(context, phrase, ".")) {
            Thread {
                Backendless.Data.of(LovePhrase::class.java).save(phrase, object : AsyncCallback<LovePhrase> {
                    override fun handleResponse(response: LovePhrase?) {
                        println("Backendless response ${response.toString()}")
                        Toast.makeText(context, R.string.success_message, Toast.LENGTH_LONG).show()
                    }

                    override fun handleFault(fault: BackendlessFault?) {
                        println("Backendless error ${fault.toString()}")
                    }
                })
            }.start()
        }
    }

    fun saveLoveClosure(context: Context, closure: LoveClosure) {
        if (!verifyItem(context, closure, ".")) {
            Thread {
                Backendless.Data.of(LoveClosure::class.java).save(closure, object : AsyncCallback<LoveClosure> {
                    override fun handleResponse(response: LoveClosure?) {
                        println("Backendless response ${response.toString()}")
                        Toast.makeText(context, R.string.success_message, Toast.LENGTH_LONG).show()
                    }

                    override fun handleFault(fault: BackendlessFault?) {
                        println("Backendless error ${fault.toString()}")
                    }
                })
            }.start()
        }
    }

    private fun verifyItem(context: Context, item: LoveItem, lastChar: String): Boolean {

        if (item.text.isEmpty()) {
            val errorMsg = "INVALID LOVE ITEM. empty or null text"
            println(LOG_TAG + errorMsg)
            Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
            return false
        }

        if (!item.text.takeLast(1).contentEquals(lastChar)) {
            item.text = item.text + lastChar
        }
        return true
    }

}