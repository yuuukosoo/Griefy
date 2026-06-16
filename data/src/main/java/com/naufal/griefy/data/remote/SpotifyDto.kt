package com.naufal.griefy.data.remote



data class SpotifySearchResponse(
    val tracks: TracksDto
)

data class TracksDto(
    val items: List<TrackDto>
)

data class TrackDto(
    val id: String,
    val name: String,
    val artists: List<ArtistDto>,
    val album: AlbumDto,
    val preview_url: String?
)

data class ArtistDto(
    val name: String
)

data class AlbumDto(
    val images: List<ImageDto>
)

data class ImageDto(
    val url: String
)