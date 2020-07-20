package subtext.yuvallovenotes.backendless

import com.backendless.Backendless
import com.backendless.async.callback.AsyncCallback
import com.backendless.exceptions.BackendlessFault
import subtext.yuvallovenotes.loveletters.AbstractLoveItem
import subtext.yuvallovenotes.loveletters.LoveOpener
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future

object LocalBackendless {

    fun findAllLoveData(callback: AsyncCallback<List<LoveOpener>>) {
        Thread {
   /*         Backendless.Data.of(LoveOpener::class.java).find(callback)
            val taskExecutor: ExecutorService = Executors.newFixedThreadPool(3)
            taskExecutor.submit(Callable { 42 })
            var tasks: List<Callable<AbstractLoveItem>?> = generateCallables() // your tasks

            val futures: List<Future<*>> = taskExecutor.invokeAll(tasks)*/
        }.start()

    }

/*    private fun generateCallables(): List<Callable<Any>?> {
       val result : List<Callable<Any>> = listOf(Callable {
           Backendless.Data.of(LoveOpener::class.java).find()
       },
       Callable { 42 })
        return result
    }*/

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

                }

                override fun handleFault(fault: BackendlessFault?) {
                    println("Backendless error ${fault.toString()}")
                }
            })
        }.start()
    }
}