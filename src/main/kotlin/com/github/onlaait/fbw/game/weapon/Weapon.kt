package com.github.onlaait.fbw.game.weapon

abstract class Weapon(val name: String) {

    abstract val modelId: String

    abstract val ammo: Int

    private var onLClick: (WeaponHolder.WeaponData) -> Unit = {}
    private var onRClick: (WeaponHolder.WeaponData) -> Unit = {}
    private var onLHold: (WeaponHolder.WeaponData) -> Unit = {}
    private var onRHold: (WeaponHolder.WeaponData) -> Unit = {}
    private var onLRelease: (WeaponHolder.WeaponData) -> Unit = {}
    private var onRRelease: (WeaponHolder.WeaponData) -> Unit = {}
    private var onReload: (WeaponHolder.WeaponData) -> Unit = {}

    fun lClick(weaponData: WeaponHolder.WeaponData) {
        onLClick(weaponData)
    }

    fun rClick(weaponData: WeaponHolder.WeaponData) {
        onRClick(weaponData)
    }

    fun lHold(weaponData: WeaponHolder.WeaponData) {
        onLHold(weaponData)
    }

    fun rHold(weaponData: WeaponHolder.WeaponData) {
        onRHold(weaponData)
    }

    fun lRelease(weaponData: WeaponHolder.WeaponData) {
        onLRelease(weaponData)
    }

    fun rRelease(weaponData: WeaponHolder.WeaponData) {
        onRRelease(weaponData)
    }

    fun reload(weaponData: WeaponHolder.WeaponData) {
        onReload(weaponData)
    }

    fun onLClick(function: (WeaponHolder.WeaponData) -> Unit) {
        onLClick = function
    }

    fun onRClick(function: (WeaponHolder.WeaponData) -> Unit) {
        onRClick = function
    }

    fun onLHold(function: (WeaponHolder.WeaponData) -> Unit) {
        onLHold = function
    }

    fun onRHold(function: (WeaponHolder.WeaponData) -> Unit) {
        onRHold = function
    }

    fun onLRelease(function: (WeaponHolder.WeaponData) -> Unit) {
        onLRelease = function
    }

    fun onRRelease(function: (WeaponHolder.WeaponData) -> Unit) {
        onRRelease = function
    }

    fun onReload(function: (WeaponHolder.WeaponData) -> Unit) {
        onReload = function
    }

}