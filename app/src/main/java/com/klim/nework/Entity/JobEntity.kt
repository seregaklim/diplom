package com.klim.nework.Entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.klim.nework.dto.Job

@Entity
data class JobEntity(
    @PrimaryKey(autoGenerate = true)
    var id: Long,
    var authorId: Long,
    var name: String,
    var position: String,
    var start: Long,
    var finish: Long? = null,
    var link: String? = null,
) {
    fun toDto() = Job(id, authorId, name, position, start, finish, link)

    companion object {
        fun fromDto(dto: Job) = JobEntity(
            dto.id,
            dto.authorId,
            dto.name,
            dto.position,
            dto.start,
            dto.finish,
            dto.link,
        )
    }
}

fun List<JobEntity>.toDto(): List<Job> = map(JobEntity::toDto)
fun List<Job>.toEntity(): List<JobEntity> = map(JobEntity::fromDto)