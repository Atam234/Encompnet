package app.getknit.knit.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Which call a chat puts first (docs/VOICE_CALLS.md C-3.11-3), over the settings DataStore beside [IncomingCallStore].
 * Off by default, so the chat picks by the route; on, its top bar always offers the mesh call and its overflow the live
 * one. Bound in every build for the Settings row, read by the chat only where both call kinds are wired; rides a Knit
 * backup like every other choice the person made. [keepBursts], the mesh call's other choice, lives here too.
 */
class CallPreferenceStore(
    private val dataStore: DataStore<Preferences>,
) {
    /** The Calls screen's "Prefer mesh calls" switch. */
    val preferMesh: Flow<Boolean> = dataStore.data.map { it[KEY_PREFER_MESH] ?: false }

    /**
     * The Calls screen's "Save what I say in mesh calls" switch (ADR 2026-11.tq7q), off by default: on, each burst this
     * phone speaks in a mesh call is kept and sent to the peer's thread as a voice message when the call ends. Read by
     * `BurstNoteKeeper` as each burst begins, only where mesh calls are wired.
     */
    val keepBursts: Flow<Boolean> = dataStore.data.map { it[KEY_KEEP_BURSTS] ?: false }

    suspend fun setPreferMesh(value: Boolean) = dataStore.edit { it[KEY_PREFER_MESH] = value }

    suspend fun setKeepBursts(value: Boolean) = dataStore.edit { it[KEY_KEEP_BURSTS] = value }

    internal companion object {
        val KEY_PREFER_MESH = booleanPreferencesKey("calls_prefer_mesh")
        val KEY_KEEP_BURSTS = booleanPreferencesKey("calls_keep_bursts")
    }
}
