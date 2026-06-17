package com.naufal.griefy.data.remote

import com.google.gson.annotations.SerializedName

data class DeezerSearchResponse(
    @SerializedName("data") val data: List<DeezerTrackDto>
)

data class DeezerTrackDto(
    @SerializedName("id") val id: Long,
    @SerializedName("title") val title: String,
    @SerializedName("preview") val preview: String?,
    @SerializedName("artist") val artist: DeezerArtistDto,
    @SerializedName("album") val album: DeezerAlbumDto
)

data class DeezerArtistDto(
    @SerializedName("name") val name: String
)

data class DeezerAlbumDto(
    @SerializedName("cover_medium") val coverMedium: String
)
