package subtext.yuvallovenotes.loveitems

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import subtext.yuvallovenotes.crossapplication.models.IDFetcher
import java.util.*

//Todo: add isFavourite
@Entity(tableName = "love_item_table")
open class LoveItem(@PrimaryKey var id: String = UUID.randomUUID().toString(),
                    @ColumnInfo(name = "text") var text: String = "",
                    @ColumnInfo(name = "isPreset") var isPreset: Boolean = false) : IDFetcher {

    override fun toString(): String {
        return "$text\n\n"
    }

    override fun fetch(): String? {
        return id
    }
}
