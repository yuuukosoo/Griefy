package com.naufal.griefy.data.repository

import com.naufal.griefy.data.local.RemembranceDayEntity
import com.naufal.griefy.domain.model.RemembranceDay

fun RemembranceDayEntity.toDomain(): RemembranceDay {
    return RemembranceDay(
        id = id,
        title = title,
        description = description,
        dateTime = dateTime,
        memoryId = memoryId
    )
}

fun RemembranceDay.toEntity(): RemembranceDayEntity {
    return RemembranceDayEntity(
        id = id,
        title = title,
        description = description,
        dateTime = dateTime,
        memoryId = memoryId
    )
}
