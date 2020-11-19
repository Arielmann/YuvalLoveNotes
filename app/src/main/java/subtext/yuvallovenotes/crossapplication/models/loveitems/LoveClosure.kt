package subtext.yuvallovenotes.crossapplication.models.loveitems

import androidx.room.Entity
import java.util.*

@Entity(tableName = "love_closure_table")
class LoveClosure(id: String = UUID.randomUUID().toString(), text: String = "") : LoveItem(id, text){
    var objectId: String? = null //Forced by backendless library
}