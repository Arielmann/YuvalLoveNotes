package subtext.yuvallovenotes.crossapplication.network

interface NetworkCallback<E> {

    fun onSuccess(response: E)

    fun onFailure(message: String)

}
