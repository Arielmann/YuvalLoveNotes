package subtext.yuvallovenotes.login.network

interface UserRegistrationCallback{
        fun onSuccess()
        fun onError(error: String)
    }