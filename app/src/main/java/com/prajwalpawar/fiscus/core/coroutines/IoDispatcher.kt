package com.prajwalpawar.fiscus.core.coroutines

import javax.inject.Qualifier

/** Injects the dispatcher for disk work. Never hardcode `Dispatchers.IO` in logic. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher
