package com.naufal.griefy.domain

import com.naufal.griefy.domain.model.Memory
import com.naufal.griefy.domain.model.RemembranceDay
import com.naufal.griefy.domain.model.Song
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DomainModelTest {

    @Test
    fun testMemoryDefaultParameters() {
        val memory = Memory(
            title = "Kenangan Pertama",
            content = "Ini adalah catatan kenangan indah.",
            imageUris = listOf("uri1", "uri2"),
            createdAt = 1686960000000L,
            tags = listOf("Keluarga", "Rindu")
        )

        assertEquals(0, memory.id)
        assertEquals("Kenangan Pertama", memory.title)
        assertEquals("Ini adalah catatan kenangan indah.", memory.content)
        assertEquals(2, memory.imageUris.size)
        assertEquals("uri1", memory.imageUris[0])
        assertEquals("uri2", memory.imageUris[1])
        assertEquals(1686960000000L, memory.createdAt)
        assertEquals(2, memory.tags.size)
        assertEquals("Keluarga", memory.tags[0])
        assertFalse(memory.isPublic)
        assertNull(memory.songTrackId)
        assertNull(memory.songTitle)
        assertFalse(memory.isTrashed)
    }

    @Test
    fun testMemoryCustomParameters() {
        val memory = Memory(
            id = 42,
            title = "Piknik Bersama",
            content = "Hari yang sangat menyenangkan di taman.",
            imageUris = listOf("uri_park"),
            createdAt = 1687000000000L,
            tags = listOf("Teman"),
            isPublic = true,
            songTrackId = "track_spotify_id_123",
            songTitle = "Fix You",
            isTrashed = true
        )

        assertEquals(42, memory.id)
        assertEquals("Piknik Bersama", memory.title)
        assertEquals("Hari yang sangat menyenangkan di taman.", memory.content)
        assertEquals(1, memory.imageUris.size)
        assertEquals(1687000000000L, memory.createdAt)
        assertTrue(memory.isPublic)
        assertEquals("track_spotify_id_123", memory.songTrackId)
        assertEquals("Fix You", memory.songTitle)
        assertTrue(memory.isTrashed)
    }

    @Test
    fun testMemoryCopyOperation() {
        val original = Memory(
            id = 1,
            title = "Awal Mula",
            content = "Cerita awal mula.",
            imageUris = emptyList(),
            createdAt = 1686960000000L,
            tags = emptyList(),
            isPublic = false,
            songTrackId = null,
            songTitle = null,
            isTrashed = false
        )

        val updated = original.copy(
            title = "Awal Mula yang Baru",
            isPublic = true,
            isTrashed = true
        )

        assertEquals(1, updated.id)
        assertEquals("Awal Mula yang Baru", updated.title)
        assertEquals("Cerita awal mula.", updated.content)
        assertTrue(updated.isPublic)
        assertTrue(updated.isTrashed)
    }

    @Test
    fun testRemembranceDayInstantiation() {
        val day = RemembranceDay(
            id = 5,
            title = "Ulang Tahun Ibu",
            description = "Mengingat hari lahir ibu tercinta.",
            dateTime = 1687100000000L
        )

        assertEquals(5, day.id)
        assertEquals("Ulang Tahun Ibu", day.title)
        assertEquals("Mengingat hari lahir ibu tercinta.", day.description)
        assertEquals(1687100000000L, day.dateTime)
    }

    @Test
    fun testRemembranceDayDefaultDescription() {
        val day = RemembranceDay(
            title = "Mengenang Ayah",
            dateTime = 1687200000000L
        )

        assertEquals(0, day.id)
        assertEquals("Mengenang Ayah", day.title)
        assertEquals("", day.description)
        assertEquals(1687200000000L, day.dateTime)
    }

    @Test
    fun testSongInstantiation() {
        val song = Song(
            trackId = "track_123",
            title = "Fix You",
            artistName = "Coldplay",
            imageUrl = "http://cover-url.com/fixyou.jpg",
            previewUrl = "http://preview-url.com/fixyou.mp3"
        )

        assertEquals("track_123", song.trackId)
        assertEquals("Fix You", song.title)
        assertEquals("Coldplay", song.artistName)
        assertEquals("http://cover-url.com/fixyou.jpg", song.imageUrl)
        assertEquals("http://preview-url.com/fixyou.mp3", song.previewUrl)
    }
}
