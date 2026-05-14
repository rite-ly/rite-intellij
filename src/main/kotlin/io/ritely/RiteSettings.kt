package io.ritely

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service
import java.nio.file.Path

@State(name = "RiteSettings", storages = [Storage("rite.xml")])
class RiteSettings : PersistentStateComponent<RiteSettings.State> {
    data class State(var serverPath: String = "")

    private var state = State()

    override fun getState(): State = state

    override fun loadState(s: State) {
        state = s
    }

    fun customServerPath(): Path? =
        state.serverPath.takeIf { it.isNotBlank() }?.let(Path::of)

    companion object {
        fun getInstance(): RiteSettings = service()
    }
}
