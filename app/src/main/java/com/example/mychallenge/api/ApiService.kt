package com.example.mychallenge.api

import com.example.mychallenge.model.ApiResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("character")
    suspend fun getCharacters(@Query("page") page: Int): ApiResponse

    @GET("character")
    suspend fun searchCharacters(@Query("name") name: String): ApiResponse
}
