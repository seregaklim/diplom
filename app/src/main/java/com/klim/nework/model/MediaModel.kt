package com.klim.nework.model

import android.net.Uri
import com.klim.nework.dto.AttachmentType
import java.io.File

data class MediaModel(
    val uri: Uri? = null,
    val file: File? = null,
    val type: AttachmentType? = null

)
