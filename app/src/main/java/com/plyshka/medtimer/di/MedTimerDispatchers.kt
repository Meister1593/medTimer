package com.plyshka.medtimer.di

import javax.inject.Qualifier

enum class MedTimerDispatchers {
    IO,
    Default,
    Main
}

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class Dispatcher(val dispatcher: MedTimerDispatchers)
