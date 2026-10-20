package com.maxrave.data.listentogether

import com.maxrave.domain.data.model.listentogether.ListenTogetherRoom
import com.maxrave.domain.data.model.listentogether.RoomTrack
import com.maxrave.domain.repository.ListenTogetherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * The XYZ build ships no Listen Together client: the room never connects, and every command is dropped.
 * Nothing in the XYZ shell opens the screen; this keeps the stock one compiling.
 */
class ListenTogetherRepositoryImpl : ListenTogetherRepository {
    private val state = MutableStateFlow(ListenTogetherRoom())
    override val room: StateFlow<ListenTogetherRoom> = state.asStateFlow()

    override var autoApproveJoins: Boolean = false
    override var autoApproveSuggestions: Boolean = false

    override fun connect() = Unit

    override fun disconnect() = Unit

    override fun createRoom(username: String) = Unit

    override fun joinRoom(
        roomCode: String,
        username: String,
    ) = Unit

    override fun cancelJoin() = Unit

    override fun leaveRoom() = Unit

    override fun approveJoin(userId: String) = Unit

    override fun rejectJoin(userId: String) = Unit

    override fun approveSuggestion(suggestionId: String) = Unit

    override fun rejectSuggestion(suggestionId: String) = Unit

    override fun kickUser(userId: String) = Unit

    override fun transferHost(userId: String) = Unit

    override fun suggestTrack(track: RoomTrack) = Unit

    override fun requestSync() = Unit

    override fun clearError() = Unit
}
