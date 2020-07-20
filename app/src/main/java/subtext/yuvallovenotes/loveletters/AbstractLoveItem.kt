package subtext.yuvallovenotes.loveletters

import java.util.*

abstract class AbstractLoveItem {
   abstract var text : String
    var lastUsed : Date = Date()
    var numOfUseages = 0

    override fun toString(): String {
        return text + "\n\n" + "Last Used:" + lastUsed + "\n\n"
    }
}
