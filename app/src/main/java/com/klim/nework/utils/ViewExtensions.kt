package com.klim.nework.utils

import android.widget.ImageView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.klim.nework.R

fun ImageView.loadAvatar(url: String, vararg transforms: BitmapTransformation = emptyArray()) =

    Glide.with(this)
        .load(url)
        .placeholder(CircularProgressDrawable(this.context).apply {
            strokeWidth = 5f
            centerRadius = 30f
            start()
        })
        .timeout(10_000)
        .error(R.drawable.ic_no_avatar_user)
        .transform(*transforms)
        .into(this)

fun ImageView.loadImage(url: String, vararg transforms: BitmapTransformation = emptyArray()) =

    Glide.with(this)
        .load(url)
        .placeholder(CircularProgressDrawable(this.context).apply {
            strokeWidth = 5f
            centerRadius = 30f
            start()
        })
        .timeout(10_000)
        .error(R.drawable.ic_broken_file)
        .transform(*transforms)
        .into(this)

fun ImageView.loadCircleCrop(url: String, vararg transforms: BitmapTransformation = emptyArray()) =
    loadAvatar(url, CircleCrop(), *transforms)