package subtext.yuvallovenotes.crossapplication.service_locator

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import subtext.yuvallovenotes.crossapplication.viewmodel.LoveItemsViewModel
import subtext.yuvallovenotes.login.viewmodel.LoginViewModel

val viewModelModule = module {
    viewModel { LoveItemsViewModel(get()) }
    viewModel { LoginViewModel() }
}