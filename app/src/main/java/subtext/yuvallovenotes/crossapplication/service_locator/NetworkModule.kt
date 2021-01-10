package subtext.yuvallovenotes.crossapplication.service_locator
import org.koin.dsl.module
import subtext.yuvallovenotes.crossapplication.network.BackendlessNetworkServiceImpl

val networkModule = module {
    single { BackendlessNetworkServiceImpl }
}
