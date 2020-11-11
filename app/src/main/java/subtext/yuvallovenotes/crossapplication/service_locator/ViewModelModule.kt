package subtext.yuvallovenotes.crossapplication.service_locator

import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module
import subtext.yuvallovenotes.lovewritingtabs.ui.main.LoveViewModel

val viewModelModule = module {
    viewModel { LoveViewModel(get()) }
}