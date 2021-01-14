package subtext.yuvallovenotes.crossapplication.utils

/**
 * An interface to determine for each model in the app what its unique ID.
 * This is used for comparing the unique ID for each model for abstracting the DiffUtil Callback.
 */
interface IDFetcher {
        fun fetch(): String?
}