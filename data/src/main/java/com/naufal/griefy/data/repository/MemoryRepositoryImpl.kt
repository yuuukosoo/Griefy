package com.naufal.griefy.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.core.graphics.scale
import androidx.core.net.toUri
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import com.google.firebase.firestore.FirebaseFirestore
import com.naufal.griefy.data.local.MemoryDao
import com.naufal.griefy.data.remote.DeezerApi
import com.naufal.griefy.domain.model.Memory
import com.naufal.griefy.domain.repository.MemoryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.ByteArrayOutputStream
import javax.inject.Inject

class MemoryRepositoryImpl @Inject constructor(
    private val dao: MemoryDao,
    private val deezerApi: DeezerApi,
    private val context: Context,
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) : MemoryRepository {

    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    override fun getAllMemories(): Flow<List<Memory>> {
        val currentUserId = firebaseAuth.currentUser?.uid
        if (currentUserId != null) {
            repositoryScope.launch {
                syncUserMemoriesFromFirestore(currentUserId)
            }
        }
        return dao.getAllMemories().map { entities ->
            entities.filter {
                it.isPublic ||
                it.isSaved ||
                (currentUserId != null && it.userId == currentUserId) ||
                it.userName == Memory.DEFAULT_USERNAME ||
                it.userName.isNullOrEmpty()
            }.map { it.toDomain() }
        }
    }

    override fun getPublicMemories(): Flow<List<Memory>> = callbackFlow {
        // Real-time listener for Firestore public memories
        val listener = firestore.collection(COLLECTION_PUBLIC_MEMORIES)
            .whereEqualTo("isPublic", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e(TAG_FIRESTORE_ERROR, "Error fetching public memories: ${error.message}", error)
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val remoteMemories = snapshot.documents.mapNotNull { doc ->
                        try {
                            val title = doc.getString("title") ?: ""
                            val content = doc.getString("content") ?: ""
                            val createdAt = doc.getLong("createdAt") ?: 0L
                            val tags = (doc.get("tags") as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
                            val songTrackId = doc.getString("songTrackId")
                            val songTitle = doc.getString("songTitle")
                            val userName = doc.getString("userName")
                            val userAvatar = doc.getString("userAvatar")
                            val userId = doc.getString("userId")
                            val imageUris = (doc.get("imageUris") as? List<*>)?.mapNotNull { it as? String } ?: emptyList()

                            Memory(
                                id = doc.getLong("localId")?.toInt() ?: 0,
                                title = title,
                                content = content,
                                imageUris = imageUris,
                                createdAt = createdAt,
                                tags = tags,
                                isPublic = true,
                                songTrackId = songTrackId,
                                songTitle = songTitle,
                                isTrashed = false,
                                userName = userName,
                                userAvatar = userAvatar,
                                userId = userId
                            )
                        } catch (_: Exception) {
                            null
                        }
                    }

                    // Cache strategy: save remote memories to Room to allow offline viewing
                    repositoryScope.launch {
                        try {
                            val localMemories = dao.getAllMemoriesOnce()
                            for (remote in remoteMemories) {
                                val exists = localMemories.any { it.createdAt == remote.createdAt && it.title == remote.title }
                                if (!exists) {
                                    // Insert remote memory to Room database as cache
                                    dao.insertMemory(remote.toEntity())
                                }
                            }
                        } catch (e: Exception) {
                            android.util.Log.e(TAG_ROOM_CACHE_ERROR, "Gagal menyimpan cache ke Room: ${e.message}", e)
                        }
                    }

                    trySend(remoteMemories)
                }
            }
        awaitClose { listener.remove() }
    }

    override suspend fun getMemoryById(id: Int): Memory? {
        return dao.getMemoryById(id)?.toDomain()
    }

    override fun getMemoryByIdAsFlow(id: Int): Flow<Memory?> {
        return dao.getMemoryByIdAsFlow(id).map { it?.toDomain() }
    }

    override suspend fun addMemory(memory: Memory) {
        val currentUid = firebaseAuth.currentUser?.uid
        var processedMemory = memory.copy(userId = memory.userId ?: currentUid)
        if (processedMemory.imageUris.isNotEmpty()) {
            val base64Uris = processedMemory.imageUris.mapNotNull { getBase64FromUri(context, it) }
            processedMemory = processedMemory.copy(imageUris = base64Uris)
        }

        // Save locally and get the generated ID
        val localId = dao.insertMemory(processedMemory.toEntity()).toInt()
        val finalMemory = processedMemory.copy(id = localId)

        // Upload to Firestore if it belongs to the current user
        if (finalMemory.userId == currentUid) {
            uploadToFirestore(finalMemory)
        }
    }

    override suspend fun updateMemory(memory: Memory) {
        val currentUid = firebaseAuth.currentUser?.uid
        var processedMemory = memory.copy(userId = memory.userId ?: currentUid)
        if (processedMemory.imageUris.isNotEmpty()) {
            val base64Uris = processedMemory.imageUris.mapNotNull { getBase64FromUri(context, it) }
            processedMemory = processedMemory.copy(imageUris = base64Uris)
        }

        dao.updateMemory(processedMemory.toEntity())

        // Sync with Firestore if it belongs to the current user
        if (processedMemory.userId == currentUid) {
            uploadToFirestore(processedMemory)
        }

        // Sync bookmark/saved status to Firestore
        if (currentUid != null) {
            syncSavedStatusToFirestore(processedMemory, currentUid)
        }
    }

    private fun syncSavedStatusToFirestore(memory: Memory, currentUid: String) {
        val originalUserId = memory.userId ?: currentUid
        val originalDocId = "${originalUserId}_${memory.id}"
        val savedDocId = "${currentUid}_${originalDocId}"

        if (memory.isSaved) {
            val data = mapOf(
                "userId" to currentUid,
                "originalMemoryDocId" to originalDocId,
                "savedAt" to System.currentTimeMillis()
            )
            firestore.collection(COLLECTION_SAVED_MEMORIES)
                .document(savedDocId)
                .set(data)
                .addOnSuccessListener {
                    android.util.Log.d(TAG_FIRESTORE_SYNC, "Bookmark saved to Firestore")
                }
                .addOnFailureListener { e ->
                    android.util.Log.e(TAG_FIRESTORE_SYNC, "Failed to save bookmark to Firestore: ${e.message}")
                }
        } else {
            firestore.collection(COLLECTION_SAVED_MEMORIES)
                .document(savedDocId)
                .delete()
                .addOnSuccessListener {
                    android.util.Log.d(TAG_FIRESTORE_SYNC, "Bookmark deleted from Firestore")
                }
                .addOnFailureListener { e ->
                    android.util.Log.e(TAG_FIRESTORE_SYNC, "Failed to delete bookmark from Firestore: ${e.message}")
                }
        }
    }


    override suspend fun moveToTrash(id: Int) {
        val currentUid = firebaseAuth.currentUser?.uid
        val memory = dao.getMemoryById(id)?.toDomain()
        dao.moveToTrash(id)
        if (memory != null && (memory.isPublic || memory.userId == currentUid)) {
            val uid = memory.userId ?: currentUid ?: FALLBACK_USER_ID
            val docId = "${uid}_${memory.id}"
            firestore.collection(COLLECTION_PUBLIC_MEMORIES)
                .document(docId)
                .delete()
                .addOnSuccessListener {
                    android.util.Log.d(TAG_FIRESTORE_SYNC, "Memory deleted from Firestore (moved to trash)")
                }
                .addOnFailureListener { e ->
                    android.util.Log.e(TAG_FIRESTORE_SYNC, "Failed to delete memory from Firestore: ${e.message}")
                }
        }
    }

    override fun getTrashedMemories(): Flow<List<Memory>> {
        return dao.getTrashedMemories().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun restoreFromTrash(id: Int) {
        val currentUid = firebaseAuth.currentUser?.uid
        dao.restoreFromTrash(id)
        val memory = dao.getMemoryById(id)?.toDomain()
        if (memory != null && (memory.isPublic || memory.userId == currentUid)) {
            uploadToFirestore(memory)
        }
    }

    override suspend fun deletePermanently(id: Int) {
        val currentUid = firebaseAuth.currentUser?.uid
        val memory = dao.getMemoryById(id)?.toDomain()
        dao.deletePermanently(id)
        if (memory != null && (memory.isPublic || memory.userId == currentUid)) {
            val uid = memory.userId ?: currentUid ?: FALLBACK_USER_ID
            val docId = "${uid}_${memory.id}"
            firestore.collection(COLLECTION_PUBLIC_MEMORIES)
                .document(docId)
                .delete()
                .addOnSuccessListener {
                    android.util.Log.d(TAG_FIRESTORE_SYNC, "Memory deleted from Firestore permanently")
                }
                .addOnFailureListener { e ->
                    android.util.Log.e(TAG_FIRESTORE_SYNC, "Failed to delete memory from Firestore: ${e.message}")
                }
        }
    }

    override suspend fun searchSongs(query: String): List<com.naufal.griefy.domain.model.Song> {
        return try {
            val response = deezerApi.searchTracks(query = query)
            response.data.map { trackDto ->
                com.naufal.griefy.domain.model.Song(
                    trackId = trackDto.id.toString(),
                    title = trackDto.title,
                    artistName = trackDto.artist.name,
                    imageUrl = trackDto.album.coverMedium,
                    previewUrl = trackDto.preview
                )
            }
        } catch (e: Exception) {
            android.util.Log.e(TAG_DEEZER_ERROR, "Gagal ambil lagu dari Deezer: ${e.message}", e)
            emptyList()
        }
    }

    override suspend fun getSongDetails(trackId: String): com.naufal.griefy.domain.model.Song? {
        return try {
            val id = trackId.toLongOrNull() ?: return null
            val trackDto = deezerApi.getTrack(id)
            com.naufal.griefy.domain.model.Song(
                trackId = trackDto.id.toString(),
                title = trackDto.title,
                artistName = trackDto.artist.name,
                imageUrl = trackDto.album.coverMedium,
                previewUrl = trackDto.preview
            )
        } catch (e: Exception) {
            android.util.Log.e(TAG_DEEZER_ERROR, "Gagal ambil detail lagu dari Deezer: ${e.message}", e)
            null
        }
    }

    // Helper to compress local Uri to Base64 String
    private fun getBase64FromUri(context: Context, uriString: String): String? {
        if (uriString.startsWith(PREFIX_BASE64) || uriString.isBlank()) return uriString
        return try {
            val uri = uriString.toUri()
            val inputStream = context.contentResolver.openInputStream(uri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (originalBitmap == null) return null

            val maxDimension = 800
            val width = originalBitmap.width
            val height = originalBitmap.height
            val scaledBitmap = if (width > maxDimension || height > maxDimension) {
                val ratio = width.toFloat() / height.toFloat()
                val newWidth: Int
                val newHeight: Int
                if (ratio > 1) {
                    newWidth = maxDimension
                    newHeight = (maxDimension / ratio).toInt()
                } else {
                    newHeight = maxDimension
                    newWidth = (maxDimension * ratio).toInt()
                }
                originalBitmap.scale(newWidth, newHeight, true)
            } else {
                originalBitmap
            }

            val outputStream = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            val imageBytes = outputStream.toByteArray()
            outputStream.close()

            PREFIX_BASE64 + Base64.encodeToString(imageBytes, Base64.DEFAULT).trim()
        } catch (e: Exception) {
            android.util.Log.e(TAG_BASE64_ERROR, "Error converting image to Base64: ${e.message}", e)
            null
        }
    }

    // Helper to upload memory to Firestore
    private fun uploadToFirestore(memory: Memory) {
        val uid = memory.userId ?: firebaseAuth.currentUser?.uid ?: FALLBACK_USER_ID
        val docId = "${uid}_${memory.id}"

        val firestoreData = mapOf(
            "id" to docId,
            "localId" to memory.id,
            "userId" to uid,
            "title" to memory.title,
            "content" to memory.content,
            "createdAt" to memory.createdAt,
            "imageUris" to memory.imageUris,
            "tags" to memory.tags,
            "isPublic" to memory.isPublic,
            "songTrackId" to memory.songTrackId,
            "songTitle" to memory.songTitle,
            "userName" to (memory.userName ?: FALLBACK_USERNAME),
            "userAvatar" to memory.userAvatar
        )

        firestore.collection(COLLECTION_PUBLIC_MEMORIES)
            .document(docId)
            .set(firestoreData)
            .addOnSuccessListener {
                android.util.Log.d(TAG_FIRESTORE_SYNC, "Memori berhasil disinkronkan ke Firestore")
            }
            .addOnFailureListener { e ->
                android.util.Log.e(TAG_FIRESTORE_SYNC, "Gagal sinkronisasi ke Firestore: ${e.message}", e)
            }
    }

    override suspend fun clearAllLocalMemories() {
        dao.clearAllLocalMemories()
    }

    private suspend fun syncUserMemoriesFromFirestore(userId: String) {
        try {

            val snapshot = firestore.collection(COLLECTION_PUBLIC_MEMORIES)
                .whereEqualTo("userId", userId)
                .get()
                .await()

            val remoteMemories = snapshot.documents.mapNotNull { doc ->
                try {
                    val title = doc.getString("title") ?: ""
                    val content = doc.getString("content") ?: ""
                    val createdAt = doc.getLong("createdAt") ?: 0L
                    val tags = (doc.get("tags") as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
                    val songTrackId = doc.getString("songTrackId")
                    val songTitle = doc.getString("songTitle")
                    val userName = doc.getString("userName")
                    val userAvatar = doc.getString("userAvatar")
                    val imageUris = (doc.get("imageUris") as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
                    val isPublicVal = doc.getBoolean("isPublic") ?: false

                    Memory(
                        id = doc.getLong("localId")?.toInt() ?: 0,
                        title = title,
                        content = content,
                        imageUris = imageUris,
                        createdAt = createdAt,
                        tags = tags,
                        isPublic = isPublicVal,
                        songTrackId = songTrackId,
                        songTitle = songTitle,
                        isTrashed = false,
                        userName = userName,
                        userAvatar = userAvatar,
                        userId = userId
                    )
                } catch (_: Exception) {
                    null
                }
            }

            val localMemories = dao.getAllLocalMemoriesIncludingTrashed()
            for (remote in remoteMemories) {
                val local = if (remote.id != 0) {
                    localMemories.find { it.id == remote.id }
                } else {
                    localMemories.find { it.createdAt == remote.createdAt && it.title == remote.title }
                }

                if (local == null) {
                    dao.insertMemory(remote.toEntity())
                } else {
                    dao.updateMemory(remote.copy(
                        id = local.id,
                        isSaved = local.isSaved,
                        isTrashed = local.isTrashed
                    ).toEntity())
                }
            }


            val savedSnapshot = firestore.collection(COLLECTION_SAVED_MEMORIES)
                .whereEqualTo("userId", userId)
                .get()
                .await()

            val savedDocIds = savedSnapshot.documents.mapNotNull { it.getString("originalMemoryDocId") }
            val savedMemories = mutableListOf<Memory>()

            for (docId in savedDocIds) {
                try {
                    val memDoc = firestore.collection(COLLECTION_PUBLIC_MEMORIES)
                        .document(docId)
                        .get()
                        .await()

                    if (memDoc.exists()) {
                        val title = memDoc.getString("title") ?: ""
                        val content = memDoc.getString("content") ?: ""
                        val createdAt = memDoc.getLong("createdAt") ?: 0L
                        val tags = (memDoc.get("tags") as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
                        val songTrackId = memDoc.getString("songTrackId")
                        val songTitle = memDoc.getString("songTitle")
                        val userName = memDoc.getString("userName")
                        val userAvatar = memDoc.getString("userAvatar")
                        val originalUserId = memDoc.getString("userId")
                        val imageUris = (memDoc.get("imageUris") as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
                        val isPublicVal = memDoc.getBoolean("isPublic") ?: false
                        val localId = memDoc.getLong("localId")?.toInt() ?: 0

                        savedMemories.add(
                            Memory(
                                id = localId,
                                title = title,
                                content = content,
                                imageUris = imageUris,
                                createdAt = createdAt,
                                tags = tags,
                                isPublic = isPublicVal,
                                songTrackId = songTrackId,
                                songTitle = songTitle,
                                isTrashed = false,
                                isSaved = true,
                                userName = userName,
                                userAvatar = userAvatar,
                                userId = originalUserId
                            )
                        )
                    }
                } catch (e: Exception) {
                    android.util.Log.e(TAG_FIRESTORE_ERROR, "Gagal ambil detail memori yang di-save: ${e.message}")
                }
            }


            val updatedLocalMemories = dao.getAllLocalMemoriesIncludingTrashed()
            for (savedMem in savedMemories) {
                val local = updatedLocalMemories.find {
                    (savedMem.id != 0 && it.id == savedMem.id) ||
                    (it.createdAt == savedMem.createdAt && it.title == savedMem.title)
                }

                if (local == null) {
                    dao.insertMemory(savedMem.toEntity())
                } else {
                    dao.updateMemory(savedMem.copy(
                        id = local.id,
                        isSaved = true,
                        isTrashed = local.isTrashed
                    ).toEntity())
                }
            }


            val localSavedMemories = updatedLocalMemories.filter { it.isSaved }
            for (localSaved in localSavedMemories) {
                val originalUserId = localSaved.userId ?: userId
                val originalDocId = "${originalUserId}_${localSaved.id}"
                if (!savedDocIds.contains(originalDocId)) {
                    dao.updateMemory(localSaved.copy(isSaved = false))
                }
            }

        } catch (e: Exception) {
            android.util.Log.e(TAG_FIRESTORE_ERROR, "Gagal sinkronisasi data user dari Firestore: ${e.message}", e)
        }
    }

    private suspend fun <T> Task<T>.await(): T {
        if (isComplete) {
            val e = exception
            return if (e == null) {
                if (isCanceled) {
                    throw java.util.concurrent.CancellationException("Task was cancelled.")
                } else {
                    result as T
                }
            } else {
                throw e
            }
        }

        return suspendCancellableCoroutine { cont ->
            addOnCompleteListener { task ->
                val e = task.exception
                if (e == null) {
                    if (task.isCanceled) {
                        cont.cancel()
                    } else {
                        cont.resume(task.result as T)
                    }
                } else {
                    cont.resumeWithException(e)
                }
            }
        }
    }

    companion object {
        private const val COLLECTION_PUBLIC_MEMORIES = "public_memories"
        private const val COLLECTION_SAVED_MEMORIES = "saved_memories"
        private const val FALLBACK_USER_ID = "anonymous"
        private const val FALLBACK_USERNAME = "Anonim"
        private const val PREFIX_BASE64 = "base64:"

        private const val TAG_FIRESTORE_SYNC = "FIRESTORE_SYNC"
        private const val TAG_FIRESTORE_ERROR = "FIRESTORE_ERROR"
        private const val TAG_ROOM_CACHE_ERROR = "ROOM_CACHE_ERROR"
        private const val TAG_DEEZER_ERROR = "DEEZER_ERROR"
        private const val TAG_BASE64_ERROR = "BASE64_ERROR"
    }
}
