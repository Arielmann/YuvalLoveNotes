package subtext.yuvallovenotes.loveletters

import java.util.*

abstract class LoveItem {

    var objectId: String? = null
    var lastUsed : Date? = null
    var numOfUsages = 0
    abstract var text: String

    override fun toString(): String {
        return text + "\n\n" + "Last Used:" + lastUsed + "\n\n"
    }
}
