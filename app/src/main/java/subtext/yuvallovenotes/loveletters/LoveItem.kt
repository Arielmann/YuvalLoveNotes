package subtext.yuvallovenotes.loveletters

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import subtext.yuvallovenotes.crossapplication.models.IDFetcher
import java.util.*

@Entity(tableName = "love_item_table")
open class LoveItem() : IDFetcher {

    @PrimaryKey var objectId: String = ""
    @ColumnInfo(name = "text") var text: String = ""

    override fun toString(): String {
        return "$text\n\n"
    }

    override fun fetch(): String? {
        return objectId
    }
}
