package com.maxrave.data.lyrics

import com.maxrave.domain.data.model.lyrics.RomanizationDictionaryState
import com.maxrave.domain.data.model.lyrics.RomanizationLanguage
import com.maxrave.domain.repository.LyricsRomanizerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * The XYZ build has no lyrics, so it ships no romanizers either (and no kuromoji or pinyin4j): every line
 * comes back as nothing to show, and the Japanese dictionary is never fetched.
 */
class LyricsRomanizerRepositoryImpl : LyricsRomanizerRepository {
    override fun romanize(
        line: String,
        enabled: Set<RomanizationLanguage>,
    ): String? = null

    private val state = MutableStateFlow(RomanizationDictionaryState.NOT_DOWNLOADED)
    override val japaneseDictionaryState: StateFlow<RomanizationDictionaryState> = state.asStateFlow()

    override suspend fun downloadJapaneseDictionary(): RomanizationDictionaryState = state.value

    override fun ensureJapaneseDictionary() = Unit
}
