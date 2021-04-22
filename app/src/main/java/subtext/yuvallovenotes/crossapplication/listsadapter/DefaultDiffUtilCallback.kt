package subtext.yuvallovenotes.crossapplication.listsadapter

import android.annotation.SuppressLint
import androidx.recyclerview.widget.DiffUtil
import subtext.yuvallovenotes.crossapplication.utils.IDFetcher


/**
 * Default DiffUtil callback for lists adapters.
 * The adapter utilizes the fact that all models in the app implement the [IDFetcher] interfaces, so
 * it uses it in order to compare the unique ID of each model for `areItemsTheSame` function.
 * As for areContentsTheSame we utilize the fact that Kotlin Data Class implements for us the equals between
 * all fields, so use the equals() method to compare one object to another.
 */
@Suppress("UnnecessaryVariable") class DefaultDiffUtilCallback<T : IDFetcher> : DiffUtil.ItemCallback<T>() {

    companion object {
        private val TAG = DefaultDiffUtilCallback::class.qualifiedName
    }

    override fun areItemsTheSame(oldItem: T, newItem: T): Boolean {
        val areSame = oldItem.fetch() == newItem.fetch()
//        Log.d(TAG, "areItemsTheSame? ".plus(areSame))
        return areSame
    }

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: T, newItem: T): Boolean {
        val areSame = oldItem == newItem
//        Log.d(TAG, "areContentsTheSame? ".plus(areSame))
        return areSame
    }
}