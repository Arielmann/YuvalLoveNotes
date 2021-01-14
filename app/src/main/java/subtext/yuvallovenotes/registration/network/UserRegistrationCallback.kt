package subtext.yuvallovenotes.registration.network

interface UserRegistrationCallback{
        fun onSuccess()
        fun onError(error: String)
    }