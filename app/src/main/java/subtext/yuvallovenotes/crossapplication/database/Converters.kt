package subtext.yuvallovenotes.crossapplication.database

import androidx.room.TypeConverter
import subtext.yuvallovenotes.crossapplication.models.loveitems.Tone
import subtext.yuvallovenotes.crossapplication.models.users.Gender

class Converters {

    @TypeConverter
    fun toTone(value: String) = enumValueOf<Tone>(value)

    @TypeConverter
    fun fromTone(value: Tone) = value.name

    @TypeConverter
    fun toGender(value: String) = enumValueOf<Gender>(value)

    @TypeConverter
    fun fromGender(value: Gender) = value.name

}