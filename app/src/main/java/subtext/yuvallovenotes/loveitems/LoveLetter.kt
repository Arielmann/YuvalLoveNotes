package subtext.yuvallovenotes.loveitems

import androidx.room.Entity
import java.util.UUID

@Entity(tableName = "love_letter_table")
class LoveLetter(id: String = UUID.randomUUID().toString(), text: String = "") : LoveItem(id, text){
    var objectId: String? = null //Forced by backendless library
}
