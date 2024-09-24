package com.github.onlaait.fbw.system

import com.charleskorn.kaml.Yaml
import com.github.onlaait.fbw.server.Logger
import com.github.onlaait.fbw.utils.CoroutineManager
import com.github.onlaait.fbw.utils.CoroutineManager.mustBeCompleted
import com.github.onlaait.fbw.utils.allPlayers
import com.github.onlaait.fbw.utils.data
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import net.minestom.server.entity.Player
import java.nio.file.Path
import kotlin.io.path.*

@Serializable
data class PlayerData(
    var lastKnownName: String,
    var muteTime: Long? = null,
    var isParticipant: Boolean = true,
) {
    companion object {

        private const val PATH = "playerdata"

        init {
            Path(PATH).createDirectories()
        }

        fun read(player: Player): PlayerData {
            Logger.debug { "Loading player data of ${player.username}" }
            val path = getPath(player)
            return if (path.isRegularFile()) {
                Yaml.default.decodeFromString(path.readText())
            } else {
                PlayerData(player.username)
            }
        }

        fun write(player: Player) = CoroutineManager.fileOutputScope.launch {
            Logger.debug { "Storing player data of ${player.username}" }
            getPath(player).writer().use { it.write(Yaml.default.encodeToString(player.data)) }
        }.mustBeCompleted()

        fun writeAllPlayers() = allPlayers.forEach { write(it) }

        private fun getPath(player: Player): Path = Path("$PATH/${player.uuid}.yml")
    }
}
