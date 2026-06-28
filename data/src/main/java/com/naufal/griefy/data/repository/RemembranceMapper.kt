package com.naufal.griefy.data.repository

import com.naufal.griefy.data.local.reminder.RemembranceDayEntity
import com.naufal.griefy.domain.model.RemembranceDay

fun RemembranceDayEntity.toDomain(): RemembranceDay {
    return RemembranceDay(
        id = id,
        title = title,
        description = description,
        dateTime = dateTime,
        memoryId = memoryId,
        userId = userId
    )
}

fun RemembranceDay.toEntity(): RemembranceDayEntity {
    return RemembranceDayEntity(
        id = id,
        title = title,
        description = description,
        dateTime = dateTime,
        memoryId = memoryId,
        userId = userId
    )
}
