package config

import kotlinx.coroutines.flow.MutableStateFlow

val AppConfigSingleton = MutableStateFlow(DefaultAppConfig)
