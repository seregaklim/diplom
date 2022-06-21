package com.klim.nework.model

class FeedModel (
    val isLoading: Boolean = false,
    val hasError: Boolean = false,
    val isRefreshing: Boolean = false,

    val errorMessage: Int? = null,
        )