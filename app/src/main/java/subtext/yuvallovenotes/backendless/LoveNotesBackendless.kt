package subtext.yuvallovenotes.backendless

import com.backendless.Backendless
import com.backendless.async.callback.AsyncCallback
import com.backendless.exceptions.BackendlessFault
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
        Thread {
            val taskExecutor: ExecutorService = Executors.newFixedThreadPool(3)
            val tasks: MutableList<out Callable<MutableList<out LoveItem>>> = generateCallables() // your tasks
            val futures: List<Future<*>> = taskExecutor.invokeAll(tasks)
            futures.forEach {
                if(!taskExecutor.isShutdown){
                    taskExecutor.shutdown()
                }
                println("The element is ${it.get()}")
            }
        }.start()

    }

    private fun generateCallables(): MutableList<out Callable<MutableList<out LoveItem>>> {
        val result : MutableList<Callable<MutableList<out LoveItem>>> = mutableListOf()
       val loveOpenersCallable : Callable<MutableList<out LoveItem>> = Callable {
           Backendless.Data.of(LoveOpener::class.java).find()
       }

        val lovePhrasesCallable : Callable<MutableList<out LoveItem>> = Callable {
           Backendless.Data.of(LovePhrase::class.java).find()
       }

        val loveClosuresCallable : Callable<MutableList<out LoveItem>> = Callable {
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

    fun saveLoveOpener(loveOpener: LoveOpener) {
        Thread {

            Backendless.Data.of(LoveOpener::class.java).save(loveOpener, object : AsyncCallback<LoveOpener> {
                override fun handleResponse(response: LoveOpener?) {
                    println("Backendless response ${response.toString()}")

                }

                override fun handleFault(fault: BackendlessFault?) {
                    println("Backendless error ${fault.toString()}")
                }
            })
        }.start()
    }
}