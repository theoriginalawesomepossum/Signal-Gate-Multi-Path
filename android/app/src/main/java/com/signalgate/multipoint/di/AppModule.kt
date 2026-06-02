package com.signalgate.multipoint.di

import androidx.room.Room
import com.signalgate.multipoint.database.SignalGateDatabase
import com.signalgate.multipoint.database.repositories.DataSourceRepository
import com.signalgate.multipoint.ui.overlay.CallOverlayViewModel
import com.signalgate.multipoint.ui.dashboard.DashboardViewModel
import com.signalgate.multipoint.logic.CallScreeningEngine
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val databaseModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            SignalGateDatabase::class.java,
            "signalgate_multiport.db"
        ).build()
    }
    single { get<SignalGateDatabase>().sourceDao() }
    single { get<SignalGateDatabase>().unifiedEntryDao() }
    single { get<SignalGateDatabase>().callLogDao() }
    single { get<SignalGateDatabase>().settingDao() }
    single { get<SignalGateDatabase>().syncHistoryDao() }
}

val repositoryModule = module {
    single { DataSourceRepository(get(), get()) }
}

val logicModule = module {
    single { CallScreeningEngine(get()) }
}

val viewModelModule = module {
    viewModel { CallOverlayViewModel() }
    viewModel { DashboardViewModel(get()) }
}

val appModule = listOf(databaseModule, repositoryModule, logicModule, viewModelModule)
