package subtext.yuvallovenotes.crossapplication.models.loveitems

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import subtext.yuvallovenotes.crossapplication.models.localization.inferLanguageFromLocale
import subtext.yuvallovenotes.crossapplication.utils.IDFetcher
import java.util.*

//Todo: add isFavourite
@Entity(tableName = "love_item_table")
open class LoveItem(@PrimaryKey var id: String = UUID.randomUUID().toString(),
                    @ColumnInfo(name = "text") var text: String = "",
                    @ColumnInfo(name = "language") var language: String = inferLanguageFromLocale().fieldName,
                    @ColumnInfo(name = "isCreatedByUser") var isCreatedByUser: Boolean = false,
                    @ColumnInfo(name = "isDisabled") var isDisabled: Boolean = false) : IDFetcher {

    override fun toString(): String {
        return "$text\n\n"
    }

    override fun fetch(): String? {
        return id
    }
}
