package com.maxrave.data.repository

import com.maxrave.domain.data.model.taste.TasteException
import com.maxrave.domain.data.model.taste.TasteInput
import com.maxrave.domain.data.model.taste.TasteProfile
import com.maxrave.domain.manager.DataStoreManager
import com.maxrave.domain.repository.TasteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

/** The XYZ build ships no AI client, so a stored reading is still shown but no new one can be made. */
internal class TasteRepositoryImpl(
    private val dataStoreManager: DataStoreManager,
) : TasteRepository {
    private val json = Json { ignoreUnknownKeys = true }

    // A stored reading that no longer decodes (a field a later version renamed) counts as none, so
    // the card offers a fresh one instead of failing.
    override val tasteProfile: Flow<TasteProfile?> =
        dataStoreManager.tasteProfile.map { stored ->
            stored?.let { runCatching { json.decodeFromString<TasteProfile>(it) }.getOrNull() }
        }

    override suspend fun describeTaste(
        input: TasteInput,
        artistImages: List<String>,
    ): Result<TasteProfile> = Result.failure(TasteException(TasteException.Kind.NOT_CONFIGURED))
}
