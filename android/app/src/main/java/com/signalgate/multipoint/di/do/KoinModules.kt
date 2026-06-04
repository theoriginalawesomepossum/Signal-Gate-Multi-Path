package com.signalgate.multipoint.di

import com.signalgate.multipoint.db.AppDatabase
import com.signalgate.multipoint.repository.DataSourceRepository
import com.signalgate.multipoint.ui.BlockedNumbersViewModel
import com.signalgate.multipoint.ui.RecentCallsViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    single { AppDatabase.getDatabase(androidContext()) }

    single { DataSourceRepository(get()) }   // Injects PhoneEntryDao

    viewModel { BlockedNumbersViewModel(get()) }
    viewModel { RecentCallsViewModel(get()) }
}
