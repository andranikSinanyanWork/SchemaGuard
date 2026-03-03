package com.example.schemaguard.di

import com.example.schemaguard.data.local.FileSystemDataSource
import com.example.schemaguard.data.local.SharedPrefsDataSource
import com.example.schemaguard.data.repository.FileTreeRepositoryImpl
import com.example.schemaguard.data.repository.SchemaRepositoryImpl
import com.example.schemaguard.data.repository.SettingsRepositoryImpl
import com.example.schemaguard.domain.repository.FileTreeRepository
import com.example.schemaguard.domain.repository.SchemaRepository
import com.example.schemaguard.domain.repository.SettingsRepository
import com.example.schemaguard.domain.usecase.GenerateSchemaUseCase
import com.example.schemaguard.domain.usecase.GetChangedFilesUseCase
import com.example.schemaguard.domain.usecase.GetFileTreeUseCase
import com.example.schemaguard.domain.usecase.SaveSettingsUseCase
import com.example.schemaguard.presentation.dashboard.DashboardViewModel
import com.example.schemaguard.presentation.filetree.FileTreeViewModel
import com.example.schemaguard.presentation.generator.GeneratorViewModel
import com.example.schemaguard.presentation.settings.SettingsViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    // Data Sources
    single { SharedPrefsDataSource(androidContext()) }
    single { FileSystemDataSource() }

    // Repositories
    single<FileTreeRepository> { FileTreeRepositoryImpl(get()) }
    single<SchemaRepository> { SchemaRepositoryImpl(get()) }
    single<SettingsRepository> { SettingsRepositoryImpl(get()) }

    // Use Cases
    factory { GetFileTreeUseCase(get()) }
    factory { GenerateSchemaUseCase(get()) }
    factory { GetChangedFilesUseCase(get()) }
    factory { SaveSettingsUseCase(get()) }

    // ViewModels
    viewModel { DashboardViewModel(get(), get()) }
    viewModel { FileTreeViewModel(get(), get()) }
    viewModel { GeneratorViewModel(get()) }
    viewModel { SettingsViewModel(get(), get()) }
}
