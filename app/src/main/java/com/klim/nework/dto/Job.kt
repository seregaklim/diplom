package com.klim.nework.dto

import java.time.Instant


data class Job(
    val id: Long = 0L,
    val authorId: Long,
    val name: String = "",
    val position: String = "",
    val start:  Long = 0,
    val finish: Long =0,
    val link: String? = null,
)