package subtext.yuvallovenotes.crossapplication.models.loveitems

import androidx.room.Entity
import java.util.*

@Entity(tableName = "love_phrase_table")
class LovePhrase(id: String = "**phrase**".plus(UUID.randomUUID().toString()).plus("**phrase**"), text: String = "") : LoveItem(id, text){

    var objectId: String? = null //Forced by backendless library
}