package com.example.mychallenge.model

data class ApiResponse(
    val results: List<Character>,
    val info: Info
)

data class Info(
    val count: Int,
    val pages: Int,
    val next: String?,
    val prev: String?
)


