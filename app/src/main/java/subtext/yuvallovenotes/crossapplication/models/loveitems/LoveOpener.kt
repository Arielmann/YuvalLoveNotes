package subtext.yuvallovenotes.crossapplication.models.loveitems

import androidx.room.Entity
import java.util.*

@Entity(tableName = "love_opener_table")
class LoveOpener(id: String = "**opener**".plus(UUID.randomUUID().toString()).plus("**opener**"), text: String = "") : LoveItem(id, text){
    var objectId: String? = null //Forced by backendless library
}