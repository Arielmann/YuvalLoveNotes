package subtext.yuvallovenotes.crossapplication.service_locator

import subtext.yuvallovenotes.crossapplication.database.LoveItemsRepository
import org.koin.dsl.module
import subtext.yuvallovenotes.login.network.LoginRepository

val repositoryModule = module {
    single { LoveItemsRepository() }
    single { LoginRepository() }
}