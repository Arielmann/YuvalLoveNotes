package subtext.yuvallovenotes.backendless

import android.content.Context
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import com.backendless.Backendless
import com.backendless.async.callback.AsyncCallback
import com.backendless.exceptions.BackendlessFault
import com.backendless.persistence.DataQueryBuilder
import subtext.yuvallovenotes.YuvalLoveNotesApp.Companion.LOG_TAG
import subtext.yuvallovenotes.loveletters.*
import subtext.yuvallovenotes.utils.LoveUtils
import java.lang.ref.WeakReference
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future


class LoveNotesBackendless(val context: Context?) {

    companion object {
        private const val DEFAULT_PAGE_SIZE: Int = 100
        private const val DEFAULT_OFFSET: Int = 0
        private const val OFFSET_GAP: Int = 100
    }

    private val contextWeakReference: WeakReference<Context?> = WeakReference(context)

    private fun findLoveOpenerTableObjectCount(): Int {
        return Backendless.Data.of(LoveOpener::class.java).objectCount
    }

    private fun findLovePhraseTableObjectCount(): Int {
        return Backendless.Data.of(LovePhrase::class.java).objectCount
    }

    private fun findLoveClosureTableObjectCount(): Int {
        return Backendless.Data.of(LoveClosure::class.java).objectCount
    }

    private fun generateLoveItemsPossibleOffsetsMap(): Map<LoveItemType, Int> {
        val taskExecutor: ExecutorService = Executors.newFixedThreadPool(3)
        val tasks: MutableList<Callable<Pair<LoveItemType, Int>>> = generateObjectCountCallables()
        val futures: List<Future<*>> = taskExecutor.invokeAll(tasks)

        val finalResults: MutableMap<LoveItemType, Int> = EnumMap(LoveItemType::class.java)
        futures.forEach {
            val loveTypeToObjectCountPair: Pair<LoveItemType, Int> = it.get() as Pair<LoveItemType, Int>
            //Setting a random offset if object count in table is higher than 100
            finalResults[loveTypeToObjectCountPair.first] = if (loveTypeToObjectCountPair.second > 100) loveTypeToObjectCountPair.second - OFFSET_GAP else DEFAULT_OFFSET
        }
        taskExecutor.shutdown()
        return finalResults
    }

    private fun generateObjectCountCallables(): MutableList<Callable<Pair<LoveItemType, Int>>> {
        val result: MutableList<Callable<Pair<LoveItemType, Int>>> = mutableListOf()
        val loveOpenerObjectCountCallable: Callable<Pair<LoveItemType, Int>> = Callable {
            Pair(LoveItemType.OPENER, findLoveOpenerTableObjectCount())
        }
        val lovePhraseObjectCountCallable: Callable<Pair<LoveItemType, Int>> = Callable {
            Pair(LoveItemType.PHRASE, findLovePhraseTableObjectCount())
        }
        val loveOpenersObjectCountCallable: Callable<Pair<LoveItemType, Int>> = Callable {
            Pair(LoveItemType.CLOSURE, findLoveClosureTableObjectCount())
        }
        result.add(loveOpenerObjectCountCallable)
        result.add(lovePhraseObjectCountCallable)
        result.add(loveOpenersObjectCountCallable)
        return result
    }

    fun findAllLoveData(callback: AsyncCallback<List<LoveItem>>) {
        if (!LoveUtils.isNetworkAvailable(contextWeakReference.get())) return
        val taskExecutor: ExecutorService = Executors.newFixedThreadPool(3)
        val tasks: MutableList<out Callable<MutableList<out LoveItem>>> = generateLoveItemsMutableListsCallables(generateLoveItemsPossibleOffsetsMap()) // your tasks
        val futures: List<Future<*>> = taskExecutor.invokeAll(tasks)

        val finalResults: MutableList<LoveItem> = mutableListOf()
        futures.forEach {
            finalResults.addAll(it.get() as List<LoveItem>)
        }
        callback.handleResponse(finalResults)
        taskExecutor.shutdown()
    }


    private fun generateLoveItemsMutableListsCallables(possibleOffsetsMap: Map<LoveItemType, Int>): MutableList<out Callable<MutableList<out LoveItem>>> {
        if (!LoveUtils.isNetworkAvailable(contextWeakReference.get())) return mutableListOf()
        val queryBuilder = DataQueryBuilder.create()
        queryBuilder.setPageSize(DEFAULT_PAGE_SIZE)
        val result: MutableList<Callable<MutableList<out LoveItem>>> = mutableListOf()
        val loveOpenersCallable: Callable<MutableList<out LoveItem>> = Callable {
            queryBuilder.setOffset(randomOffset(LoveItemType.OPENER, possibleOffsetsMap))
            Backendless.Data.of(LoveOpener::class.java).find(queryBuilder)
        }

        val lovePhrasesCallable: Callable<MutableList<out LoveItem>> = Callable {
            queryBuilder.setOffset(randomOffset(LoveItemType.PHRASE, possibleOffsetsMap))
            Backendless.Data.of(LovePhrase::class.java).find(queryBuilder)
//            Backendless.Data.of(LovePhrase::class.java).find()
        }

        val loveClosuresCallable: Callable<MutableList<out LoveItem>> = Callable {
            queryBuilder.setOffset(randomOffset(LoveItemType.CLOSURE, possibleOffsetsMap))
            Backendless.Data.of(LoveClosure::class.java).find(queryBuilder)
        }

        result.add(loveOpenersCallable)
        result.add(lovePhrasesCallable)
        result.add(loveClosuresCallable)
        return result
    }

    private fun randomOffset(type: LoveItemType, possibleOffsetsMap: Map<LoveItemType, Int>): Int {
        @Suppress("UnnecessaryVariable")
        var offset = possibleOffsetsMap[type] ?: error {
            Toast.makeText(contextWeakReference.get(), "Invalid offset of love item type ${type.name}, using $DEFAULT_OFFSET instead", LENGTH_LONG).show()
            DEFAULT_OFFSET
        }
        offset = (0..offset).random()
        return offset
    }

    fun saveLoveOpener(opener: LoveOpener, callback: AsyncCallback<LoveOpener>) {
        if (verifyItemSaveOperation(opener, ",", callback)) {
            Thread {
                Backendless.Data.of(LoveOpener::class.java).save(opener, callback)
            }.start()
        }
    }

    fun saveLovePhrase(phrase: LovePhrase, callback: AsyncCallback<LovePhrase>) {
        if (verifyItemSaveOperation(phrase, ".", callback)) {
            Thread {
                Backendless.Data.of(LovePhrase::class.java).save(phrase, callback)
            }.start()
        }
    }

    private fun verifyItemSaveOperation(item: LoveItem, lastChar: String, callback: AsyncCallback<out LoveItem>): Boolean {
        if (!LoveUtils.isNetworkAvailable(contextWeakReference.get())) return false
        if (item.text.isEmpty()) {
            val errorMsg = "INVALID LOVE ITEM. empty or null text"
            println(LOG_TAG + errorMsg)
            Toast.makeText(contextWeakReference.get(), errorMsg, Toast.LENGTH_LONG).show()
            callback.handleFault(BackendlessFault("INVALID LOVE ITEM. empty or null text"))
            return false
        }

        if (!item.text.takeLast(1).contentEquals(lastChar)) {
            item.text = item.text + lastChar
        }
        return true
    }

    fun saveLoveClosure(closure: LoveClosure, callback: AsyncCallback<LoveClosure>) {
        if (verifyItemSaveOperation(closure, ".", callback)) {
            Thread {
                Backendless.Data.of(LoveClosure::class.java).save(closure, callback)
            }.start()
        }
    }

}
