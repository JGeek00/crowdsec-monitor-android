package com.jgeek00.crowdsecmonitor.di

import com.jgeek00.crowdsecmonitor.viewmodel.OnboardingStorage
import com.jgeek00.crowdsecmonitor.viewmodel.SharedPreferencesOnboardingStorage
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class OnboardingModule {

    @Binds
    @Singleton
    abstract fun bindOnboardingStorage(impl: SharedPreferencesOnboardingStorage): OnboardingStorage
}
