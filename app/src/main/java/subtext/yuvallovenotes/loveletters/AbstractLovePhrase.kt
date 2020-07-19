package subtext.yuvallovenotes.loveletters

import java.util.*

abstract class AbstractLovePhrase {
   abstract var text : String
    var lastUsed : Date = Date()
    var numOfUseages = 0

}
