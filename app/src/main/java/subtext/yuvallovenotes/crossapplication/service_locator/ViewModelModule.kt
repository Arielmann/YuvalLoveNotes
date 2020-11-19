package subtext.yuvallovenotes.crossapplication.service_locator

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import subtext.yuvallovenotes.lovelettersgenerator.viewmodel.LoveItemsViewModel

val viewModelModule = module {
    viewModel { LoveItemsViewModel(get()) }
}