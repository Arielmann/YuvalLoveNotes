package subtext.yuvallovenotes.crossapplication.events


class LoveLetterEvent<T>(val content: T) {

    var isFirstTimeFired = true
        private set

    fun getContentIfNotFirstTimeFired(): T? {
        if (!isFirstTimeFired) { return content }
        isFirstTimeFired = false
        return null
    }
}
