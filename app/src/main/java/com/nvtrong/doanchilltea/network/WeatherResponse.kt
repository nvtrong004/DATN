package com.nvtrong.doanchilltea.network

data class WeatherResponse(
    val main: Main,
    val name: String,
    val weather: List<WeatherInfo>? = null
)

data class Main(
    val temp: Double
)

data class WeatherInfo(
    val main: String? = null,
    val description: String? = null
)
