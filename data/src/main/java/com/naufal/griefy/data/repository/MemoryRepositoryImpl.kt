package com.naufal.griefy.data.repository

import android.content.Context
import android.util.Base64
import androidx.core.net.toUri
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import com.google.firebase.firestore.FirebaseFirestore
import com.naufal.griefy.data.local.MemoryDao
import com.naufal.griefy.data.remote.CloudinaryUploader
import com.naufal.griefy.data.remote.DeezerApi
import com.naufal.griefy.domain.model.Memory
import com.naufal.griefy.domain.repository.MemoryRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.io.IOException
import java.net.SocketTimeoutException
import javax.inject.Inject

class MemoryRepositoryImpl @Inject constructor(
    private val dao: MemoryDao,
    private val deezerApi: DeezerApi,
    @param:ApplicationContext private val context: Context,
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
    private val cloudinaryUploader: CloudinaryUploader
) : MemoryRepository {

    private val repositoryScope = CoroutineScope(Dispatchers.IO)
    private val songDetailsCache = mutableMapOf<String, com.naufal.griefy.domain.model.Song>()

    override fun getAllMemories(): Flow<List<Memory>> {
        val currentUserId = firebaseAuth.currentUser?.uid
        if (currentUserId != null) {
            repositoryScope.launch {
                syncUserMemoriesFromFirestore(currentUserId)
            }
        }
        return dao.getAllMemories().map { entities ->
            entities.map { it.toDomain() }
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
                            val updatedAt = doc.getLong("updatedAt") ?: createdAt
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
                                updatedAt = updatedAt,
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

                    // Cache strategy: save/update remote memories to Room to allow offline viewing
                    repositoryScope.launch {
                        try {
                            val localMemories = dao.getAllMemoryKeysOnce()
                            for (remote in remoteMemories) {
                                val local = localMemories.find {
                                    (remote.id != 0 && it.id == remote.id) ||
                                    (it.createdAt == remote.createdAt && it.userId == remote.userId)
                                }
                                if (local == null) {
                                    // Insert remote memory to Room database as cache
                                    dao.insertMemory(remote.toEntity())
                                } else {
                                    // Update existing cached memory with latest data from Firestore
                                    dao.updateMemory(remote.copy(
                                        id = local.id,
                                        isSaved = local.isSaved,
                                        isTrashed = local.isTrashed
                                    ).toEntity())
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
        val userId = memory.userId ?: currentUid

        val localImageUris = memory.imageUris.mapIndexedNotNull { index, uri ->
            saveUriToLocalFile(context, uri, "${userId ?: "guest"}_add_${index}")
        }
        val localMemory = memory.copy(userId = userId, imageUris = localImageUris)

        // Save locally and get the generated ID
        val localId = dao.insertMemory(localMemory.toEntity()).toInt()
        val finalLocalMemory = localMemory.copy(id = localId)

        // Upload to Firestore if it belongs to the current user
        if (userId == currentUid && currentUid != null) {
            val cloudinaryUris = localImageUris.mapNotNull { cloudinaryUploader.uploadImage(it) }
            val uploadMemory = finalLocalMemory.copy(imageUris = cloudinaryUris)
            uploadToFirestore(uploadMemory)
        }
    }

    override suspend fun updateMemory(memory: Memory) {
        val currentUid = firebaseAuth.currentUser?.uid
        val userId = memory.userId ?: currentUid

        val localImageUris = memory.imageUris.mapIndexedNotNull { index, uri ->
            saveUriToLocalFile(context, uri, "${userId ?: "guest"}_update_${index}")
        }
        val localMemory = memory.copy(userId = userId, imageUris = localImageUris, updatedAt = System.currentTimeMillis())

        dao.updateMemory(localMemory.toEntity())

        // Sync with Firestore if it belongs to the current user
        if (userId == currentUid && currentUid != null) {
            val cloudinaryUris = localImageUris.mapNotNull { cloudinaryUploader.uploadImage(it) }
            val uploadMemory = localMemory.copy(imageUris = cloudinaryUris)
            uploadToFirestore(uploadMemory)
        }

        // Sync bookmark/saved status to Firestore
        if (currentUid != null) {
            syncSavedStatusToFirestore(localMemory, currentUid)
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
            val cloudinaryUris = memory.imageUris.mapNotNull { cloudinaryUploader.uploadImage(it) }
            val uploadMemory = memory.copy(imageUris = cloudinaryUris)
            uploadToFirestore(uploadMemory)
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
        songDetailsCache[trackId]?.let { return it }

        val id = trackId.toLongOrNull() ?: return null
        var lastError: Exception? = null

        repeat(MAX_DEEZER_RETRIES) { attempt ->
            try {
                val trackDto = deezerApi.getTrack(id)
                val song = com.naufal.griefy.domain.model.Song(
                    trackId = trackDto.id.toString(),
                    title = trackDto.title,
                    artistName = trackDto.artist.name,
                    imageUrl = trackDto.album.coverMedium,
                    previewUrl = trackDto.preview
                )
                songDetailsCache[trackId] = song
                return song
            } catch (e: Exception) {
                lastError = e
                if (attempt < MAX_DEEZER_RETRIES - 1 && isDeezerRetryable(e)) {
                    delay(DEEZER_RETRY_DELAY_MS * (attempt + 1))
                }
            }
        }

        android.util.Log.e(
            TAG_DEEZER_ERROR,
            "Gagal ambil detail lagu dari Deezer setelah $MAX_DEEZER_RETRIES percobaan: ${lastError?.message}",
            lastError
        )
        return songDetailsCache[trackId]
    }

    private fun isDeezerRetryable(error: Exception): Boolean {
        return error is SocketTimeoutException ||
            (error is IOException && error.message?.contains("timeout", ignoreCase = true) == true)
    }

    private fun saveUriToLocalFile(context: Context, uriString: String, id: String): String? {
        if (uriString.startsWith("file:/") || uriString.startsWith("http://") || uriString.startsWith("https://")) {
            return uriString // Sudah berupa file lokal atau url remote, tidak perlu disalin ulang
        }
        return try {
            val dir = File(context.filesDir, "memory_images")
            if (!dir.exists()) {
                dir.mkdirs()
            }
            val file = File(dir, "img_${id}_${System.currentTimeMillis()}.jpg")
            val inputStream = if (uriString.startsWith(PREFIX_BASE64)) {
                val base64Data = uriString.substringAfter(PREFIX_BASE64)
                val bytes = Base64.decode(base64Data, Base64.DEFAULT)
                bytes.inputStream()
            } else {
                context.contentResolver.openInputStream(uriString.toUri())
            }

            inputStream?.use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            file.toURI().toString()
        } catch (e: Exception) {
            android.util.Log.e("LOCAL_IMAGE_SAVE", "Gagal menyimpan file gambar lokal: ${e.message}", e)
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
            "updatedAt" to memory.updatedAt,
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

            val localMemories = dao.getAllLocalMemoryKeysIncludingTrashed()
            for (remote in remoteMemories) {
                val resolvedUris = remote.imageUris.mapIndexedNotNull { index, uri ->
                    if (uri.startsWith("https://")) uri
                    else saveUriToLocalFile(context, uri, "${userId}_sync_${remote.createdAt}_$index")
                }
                val remoteWithLocalImages = remote.copy(imageUris = resolvedUris)

                val local = if (remoteWithLocalImages.id != 0) {
                    localMemories.find { it.id == remoteWithLocalImages.id }
                } else {
                    localMemories.find { it.createdAt == remoteWithLocalImages.createdAt && it.title == remoteWithLocalImages.title }
                }

                if (local == null) {
                    dao.insertMemory(remoteWithLocalImages.toEntity())
                } else {
                    dao.updateMemory(remoteWithLocalImages.copy(
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


            val updatedLocalMemories = dao.getAllLocalMemoryKeysIncludingTrashed()
            for (savedMem in savedMemories) {
                val resolvedUris = savedMem.imageUris.mapIndexedNotNull { index, uri ->
                    if (uri.startsWith("https://")) uri
                    else saveUriToLocalFile(context, uri, "${userId}_syncSaved_${savedMem.createdAt}_$index")
                }
                val savedMemWithLocalImages = savedMem.copy(imageUris = resolvedUris)

                val local = updatedLocalMemories.find {
                    (savedMemWithLocalImages.id != 0 && it.id == savedMemWithLocalImages.id) ||
                    (it.createdAt == savedMemWithLocalImages.createdAt && it.title == savedMemWithLocalImages.title)
                }

                if (local == null) {
                    dao.insertMemory(savedMemWithLocalImages.toEntity())
                } else {
                    dao.updateMemory(savedMemWithLocalImages.copy(
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
                    dao.updateSavedStatus(localSaved.id, false)
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

    override fun getMemoryCount(userId: String): Flow<Int> = callbackFlow {
        val listener = firestore.collection(COLLECTION_PUBLIC_MEMORIES)
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    trySend(snapshot.size())
                }
            }
        awaitClose { listener.remove() }
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
        private const val MAX_DEEZER_RETRIES = 3
        private const val DEEZER_RETRY_DELAY_MS = 500L
    }
}
