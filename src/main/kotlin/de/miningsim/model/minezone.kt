package de.miningsim.model

import org.bukkit.Location
import org.bukkit.Material

class MineZone(
    val name: String,
    var min: Location,
    var max: Location
) {
    val blockComposition: MutableMap<Material, Int> = linkedMapOf()
    private val brokenBlocks: MutableSet<Location> = mutableSetOf()
    private var totalWeight: Int = 0
    private val random = java.util.Random()

    fun setBlockWeight(material: Material, weight: Int) {
        blockComposition[material] = weight
        recalcTotal()
    }

    fun removeBlock(material: Material) {
        blockComposition.remove(material)
        recalcTotal()
    }

    private fun recalcTotal() {
        totalWeight = blockComposition.values.sum()
    }

    fun getRandomBlock(): Material {
        if (blockComposition.isEmpty()) return Material.STONE
        var roll = random.nextInt(totalWeight)
        for ((mat, weight) in blockComposition) {
            roll -= weight
            if (roll < 0) return mat
        }
        return blockComposition.keys.first()
    }

    fun markBroken(loc: Location) { brokenBlocks.add(loc.block.location) }
    fun clearBroken() { brokenBlocks.clear() }
    fun getBrokenCount() = brokenBlocks.size

    fun getAllLocations(): List<Location> {
        val world = min.world ?: return emptyList()
        val minX = minOf(min.blockX, max.blockX)
        val minY = minOf(min.blockY, max.blockY)
        val minZ = minOf(min.blockZ, max.blockZ)
        val maxX = maxOf(min.blockX, max.blockX)
        val maxY = maxOf(min.blockY, max.blockY)
        val maxZ = maxOf(min.blockZ, max.blockZ)
        return buildList {
            for (x in minX..maxX)
                for (y in minY..maxY)
                    for (z in minZ..maxZ)
                        add(Location(world, x.toDouble(), y.toDouble(), z.toDouble()))
        }
    }

    fun getTotalBlocks(): Int {
        return (Math.abs(min.blockX - max.blockX) + 1) *
                (Math.abs(min.blockY - max.blockY) + 1) *
                (Math.abs(min.blockZ - max.blockZ) + 1)
    }

    fun contains(loc: Location): Boolean {
        if (loc.world != min.world) return false
        val x = loc.blockX; val y = loc.blockY; val z = loc.blockZ
        return x in minOf(min.blockX, max.blockX)..maxOf(min.blockX, max.blockX) &&
                y in minOf(min.blockY, max.blockY)..maxOf(min.blockY, max.blockY) &&
                z in minOf(min.blockZ, max.blockZ)..maxOf(min.blockZ, max.blockZ)
    }
}