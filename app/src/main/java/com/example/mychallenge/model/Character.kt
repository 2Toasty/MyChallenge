package com.example.mychallenge.model

data class Character(
    val id: Int = 0,
    val name: String = "",
    val origin: Origin = Origin(),
    val image: String = ""
)

data class Origin(val name: String = "")
