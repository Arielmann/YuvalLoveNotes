package subtext.yuvallovenotes.crossapplication.service_locator

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import subtext.yuvallovenotes.lovetabs.viewmodel.LetterViewModel

val viewModelModule = module {
    viewModel { LetterViewModel(get()) }
}