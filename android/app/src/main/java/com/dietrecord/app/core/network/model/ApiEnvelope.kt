package com.dietrecord.app.core.network.model

data class ApiEnvelope<T>(
    val code: Int,
    val msg: String,
    val data: T?
)
