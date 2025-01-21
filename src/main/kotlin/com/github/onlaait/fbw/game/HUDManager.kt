package com.github.onlaait.fbw.game

import com.github.onlaait.fbw.entity.FPlayer
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor

class HUDManager(val player: FPlayer) {

    private val bossBars = mutableListOf<BossBar>()

    fun clear() {
        bossBars.forEach {
            it.removeViewer(player)
        }
        bossBars.clear()
    }

    // Weapon Separator

    fun showWeaponSeparator() {
        setElementText(Element.WEAPON_SEPARATOR, "\uE001")
    }

    // Ammo

    fun showAmmo() {
        setAmmoText("$ammo/$maxAmmo")
    }

    fun setAmmoText(ammo: String) {
        setElementText(Element.AMMO, ammo)
    }

    private var ammo = 0
    private var maxAmmo = 0

    fun setAmmo(ammo: Int) {
        this.ammo = ammo
        showAmmo()
    }

    fun setAmmo(ammo: Int, maxAmmo: Int) {
        this.ammo = ammo
        this.maxAmmo = maxAmmo
        showAmmo()
    }

    fun setMaxAmmo(maxAmmo: Int) {
        this.maxAmmo = maxAmmo
        showAmmo()
    }

    //

    private fun setElementText(element: Element, text: String) {
        setLine(element.line, Component.text(text).color(TextColor.color(element.id, 0, 0)).font(element.font))
    }

    private fun setLine(line: Int, text: Component) {
        repeat(line - bossBars.size) {
            bossBars += makeEmptyBossBar()
        }
        bossBars[line - 1].name(text)
    }

    private fun makeEmptyBossBar(): BossBar =
        BossBar.bossBar(Component.empty(), 0f, BossBar.Color.WHITE, BossBar.Overlay.PROGRESS).also { it.addViewer(player) }

    private enum class Element(val line: Int, val id: Int, val font: Key) {
        WEAPON_SEPARATOR(1, 2, Key.key("fbw:fbw")),
        AMMO(2, 3, Key.key("fbw:num9")),
    }
}