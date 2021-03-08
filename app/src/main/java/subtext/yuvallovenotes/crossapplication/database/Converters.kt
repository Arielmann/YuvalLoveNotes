package subtext.yuvallovenotes.crossapplication.database

import androidx.room.TypeConverter
import subtext.yuvallovenotes.crossapplication.models.loveitems.Tone

class Converters {

    @TypeConverter
    fun toTone(value: String) = enumValueOf<Tone>(value)

    @TypeConverter
    fun fromTone(value: Tone) = value.name

}