package com.klim.nework.model

import com.google.gson.annotations.SerializedName

class AuthModel (
    @SerializedName("id")
    val userId: Long?,
    @SerializedName("token")
    val token: String?
)