package com.lavdevapp.chillax

import android.app.Application
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

class AppViewModel(
    application: Application,
    private val stateHandle: SavedStateHandle
) : AndroidViewModel(application) {
    private val moshi by lazy { Moshi.Builder().build() }
    private val preferences = application.getSharedPreferences(
        PREFERENCES_NAME,
        AppCompatActivity.MODE_PRIVATE
    )
    private val _tracksListState = stateHandle.getLiveData<List<Track>>(STATE_HANDLE_STATES_KEY)
    val tracksListState: LiveData<List<Track>>
        get() = _tracksListState
    private val _mainSwitchState = MutableLiveData(false)
    val mainSwitchState: LiveData<Boolean>
        get() = _mainSwitchState
    // TODO: try to add timer button state
    private val _timerStatus = MutableLiveData<PlayersService.TimerStatus>()
    val timerStatus: LiveData<PlayersService.TimerStatus>
        get() = _timerStatus
    private var saveRequired = false

    init {
        loadData()
    }

    private fun loadData() {
        if (_tracksListState.value == null) {
            if (preferences.contains(PREFERENCES_STATES_KEY)) {
                loadFromPrefs()
            } else {
                loadFromDefaultStates()
            }
        }
    }

    fun setItemChecked(item: Track, isChecked: Boolean) {
        val newList = mutableListOf<Track>()
        _tracksListState.value?.forEach {
            if (it.trackName == item.trackName) {
                newList.add(item.copy(switchState = isChecked))
            } else {
                newList.add(it.copy())
            }
        }
        stateHandle[STATE_HANDLE_STATES_KEY] = newList
        saveRequired = true
    }

    fun setMainSwitchState(state: Boolean) {
        _mainSwitchState.value = state
        setItemsEnabled(state)
    }

    fun setTimerStatus(value: PlayersService.TimerStatus) {
        _timerStatus.value = value
    }

    fun saveData() {
        if (saveRequired) {
            _tracksListState.value?.let {
                val json = serialize(it)
                preferences.edit().putString(PREFERENCES_STATES_KEY, json).apply()
            }
            saveRequired = false
        }
    }

    private fun setItemsEnabled(areEnabled: Boolean) {
        val newList = mutableListOf<Track>()
        _tracksListState.value?.forEach {
            newList.add(it.copy(switchEnabled = areEnabled))
        }
        stateHandle[STATE_HANDLE_STATES_KEY] = newList
    }

    private fun loadFromPrefs() {
        val json = preferences.getString(PREFERENCES_STATES_KEY, null)
        val data = json?.let { deserialize(it) }
        stateHandle[STATE_HANDLE_STATES_KEY] = data
    }

    private fun loadFromDefaultStates() {
        stateHandle[STATE_HANDLE_STATES_KEY] = DefaultTracksStates.tracks
    }

    private fun serialize(data: List<Track>): String {
        val type = Types.newParameterizedType(List::class.java, Track::class.java)
        return moshi.adapter<List<Track>>(type).toJson(data)
    }

    private fun deserialize(json: String): List<Track>? {
        val type = Types.newParameterizedType(List::class.java, Track::class.java)
        return moshi.adapter<List<Track>>(type).fromJson(json)
    }

    companion object {
        const val PREFERENCES_NAME = "app_prefs"
        const val STATE_HANDLE_STATES_KEY = "state_handle_states"
        const val PREFERENCES_STATES_KEY = "preferences_states"
    }
}