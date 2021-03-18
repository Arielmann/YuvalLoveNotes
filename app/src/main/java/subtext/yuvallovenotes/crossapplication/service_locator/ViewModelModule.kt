package subtext.yuvallovenotes.crossapplication.service_locator

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import subtext.yuvallovenotes.crossapplication.viewmodel.LoveItemsViewModel
import subtext.yuvallovenotes.registration.viewmodel.RegistrationViewModel

val viewModelModule = module {
    viewModel { LoveItemsViewModel() } //todo: remove
    viewModel { RegistrationViewModel() }
}