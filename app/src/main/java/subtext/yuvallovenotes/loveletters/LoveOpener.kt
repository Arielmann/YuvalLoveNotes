package subtext.yuvallovenotes.loveletters

class LoveOpener : AbstractLovePhrase(){

    override var text: String = ""

    override fun toString(): String {
        return text + "\n\n" + "Last Used:" + lastUsed + "\n\n"
    }




}