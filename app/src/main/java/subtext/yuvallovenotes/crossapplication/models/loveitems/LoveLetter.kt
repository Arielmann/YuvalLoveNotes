package subtext.yuvallovenotes.crossapplication.models.loveitems

import androidx.room.Entity
import androidx.room.TypeConverter
import subtext.yuvallovenotes.crossapplication.models.users.Gender
import java.util.UUID

@Entity(tableName = "love_letter_table")
class LoveLetter(id: String = UUID.randomUUID().toString(), text: String = "", var sernum: Int = 0) : LoveItem(id, text){

    var senderGender: Gender = Gender.MALE
    var receiverGender: Gender = Gender.FEMALE
    var tone: Tone = Tone.FUNNY
    var autoInsertLoverNicknameAsOpener: Boolean = false
    var objectId: String? = null //Forced by backendless library


}

enum class Tone {
    CASUAL, FUNNY, DEEP, EROTIC
}
