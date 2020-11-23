package subtext.yuvallovenotes.crossapplication.service_locator

import subtext.yuvallovenotes.crossapplication.database.LoveItemsRepository
import org.koin.dsl.module

val repositoryModule = module {
    single { LoveItemsRepository() }
}