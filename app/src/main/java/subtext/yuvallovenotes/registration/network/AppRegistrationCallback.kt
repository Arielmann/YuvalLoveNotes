package subtext.yuvallovenotes.registration.network

interface AppRegistrationCallback{
        fun onSuccess()
        fun onError(error: String)
    }