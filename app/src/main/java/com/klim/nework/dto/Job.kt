package com.klim.nework.dto



data class Job(
    val id: Long = 0L,
    val authorId: Long,
    val name: String = "",
    val position: String = "",
    val start: Long = 0L,
    val finish: Long? = null,
    val link: String? = null,
)