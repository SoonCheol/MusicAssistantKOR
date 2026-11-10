package io.music_assistant.client.ui.compose.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import io.music_assistant.client.api.Request
import io.music_assistant.client.data.MainDataSource
import io.music_assistant.client.data.model.client.items.AppMediaItem
import io.music_assistant.client.data.model.client.items.Genre
import io.music_assistant.client.data.play.PlayDispatcher
import io.music_assistant.client.data.play.PlayMode
import io.music_assistant.client.data.play.PlayRequest
import io.music_assistant.client.data.repository.MediaItemRepository
import io.music_assistant.client.ui.compose.common.DataState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Backs one level of the folder-style Browse screen. [path] is the server browse path for this
 * level (null = provider root); a folder's [AppMediaItem.uri] becomes the next level's path.
 * The server returns the full level in one shot, so there's no pagination/sort/favorites here.
 */
class BrowseViewModel(
    private val path: String?,
    private val playDispatcher: PlayDispatcher,
    private val mainDataSource: MainDataSource,
    private val mediaItemRepository: MediaItemRepository,
) : ViewModel() {
    private val _state = MutableStateFlow<DataState<List<AppMediaItem>>>(DataState.Loading())
    val state = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.update { DataState.Loading() }
            // KOR convenience: from the Browse entry, skip the provider picker when a
            // single Filesystem source can be resolved from the live server response.
            // Never guess a provider ID or hard-code a server browse URI.
            val rootResult = mediaItemRepository.fetchMediaItems(Request.Browse.atPath(path))
            val rootItems = rootResult.getOrNull()
            val filesystemRoots = if (path == null) {
                rootItems.orEmpty().filterIsInstance<io.music_assistant.client.data.model.client.items.RecommendationFolder>()
                    .filter { folder ->
                        !folder.isParentLink &&
                            folder.displayName.startsWith("Filesystem", ignoreCase = true) &&
                            !folder.path.isNullOrBlank()
                    }
            } else {
                emptyList()
            }
            val result = if (filesystemRoots.size == 1) {
                val folderPath = requireNotNull(filesystemRoots.single().path)
                val filesystemResult = mediaItemRepository.fetchMediaItems(Request.Browse.atPath(folderPath))
                // If the source is unavailable, retain the ordinary provider list.
                if (filesystemResult.isSuccess) filesystemResult else rootResult
            } else {
                rootResult
            }
            result.getOrNull()
                ?.let { items -> _state.update { DataState.Data(items) } }
                ?: run {
                    Logger.e("Error browsing path=$path:", result.exceptionOrNull())
                    _state.update { DataState.Error() }
                }
        }
    }

    // Mirrors ItemListViewModel.onPlayClick — play a browsed item on the selected player.
    fun onPlayClick(item: AppMediaItem, mode: PlayMode) {
        val queueId = mainDataSource.selectedPlayer?.queueOrPlayerId ?: return
        item.mediaUri?.let { mediaUri ->
            Logger.withTag("PlayDispatch")
                .i { "BrowseViewModel: uri=$mediaUri mode=$mode queue=$queueId" }
            playDispatcher.play(
                PlayRequest(
                    queueOrPlayerId = queueId,
                    media = listOf(mediaUri),
                    option = mode.option,
                    endlessMixMode = mode.endlessMix && item !is Genre,
                    shuffle = mode.shuffle,
                    startFromBeginning = mode.startFromBeginning,
                ),
            )
        }
    }
}
