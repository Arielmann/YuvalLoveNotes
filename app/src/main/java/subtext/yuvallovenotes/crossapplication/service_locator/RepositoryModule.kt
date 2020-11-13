package subtext.yuvallovenotes.crossapplication.service_locator

import subtext.yuvallovenotes.crossapplication.database.LoveRepository
import org.koin.dsl.module

val repositoryModule = module {
    single { LoveRepository(get()) }
}