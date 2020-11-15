package subtext.yuvallovenotes.loveitems

import androidx.room.Entity
import java.util.*

@Entity(tableName = "love_phrase_table")
class LovePhrase(id: String = UUID.randomUUID().toString(), text: String = "") : LoveItem(id, text){
    var objectId: String? = null //Forced by backendless library
}