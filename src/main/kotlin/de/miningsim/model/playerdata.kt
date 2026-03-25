package de.miningsim.model

import java.util.UUID

data class PlayerData(
    val uuid: UUID,
    var name: String,
    var coins: Double = 0.0
) {
    fun addCoins(amount: Double) { coins += amount }

    fun removeCoins(amount: Double): Boolean {
        if (coins < amount) return false
        coins -= amount
        return true
    }
}