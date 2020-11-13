package subtext.yuvallovenotes.loveitems

import androidx.room.Entity
import java.util.*

@Entity(tableName = "love_opener_table")
class LoveOpener(id: String = UUID.randomUUID().toString(), text: String = "") : LoveItem(id, text)