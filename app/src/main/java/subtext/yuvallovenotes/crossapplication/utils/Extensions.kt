package subtext.yuvallovenotes.crossapplication.utils

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

fun <E> MutableList<E>.swap(list: MutableList<E>, firstIndex: Int, secondIndex: Int) {
    val firstElement = list[firstIndex]
    list[firstIndex] = list[secondIndex]
    list[secondIndex] = firstElement
}

fun <T> LiveData<T>.observeOnce(lifecycleOwner: LifecycleOwner, observer: Observer<T>) {
    observe(lifecycleOwner, object : Observer<T> {
        override fun onChanged(t: T?) {
            observer.onChanged(t)
            removeObserver(this)
        }
    })
}
