package subtext.yuvallovenotes.utils

object LoveUtils {
    fun getFunctionName(): String {
        return object{}.javaClass.enclosingMethod.name
    }

}
