package com.github.onlaait.fbw.game.utils

import net.minestom.server.coordinate.Point
import net.minestom.server.coordinate.Vec
import net.minestom.server.instance.Instance
import net.minestom.server.instance.block.Block
import net.minestom.server.utils.block.BlockIterator

object BlockUtils {

    fun rayIntersectsBlock(rayOrigin: Vec, rayDirection: Vec, block: Point): Double? {
        val max = block.add(1.0)
        val min = block
        val invDir = Vec(1f / rayDirection.x, 1f / rayDirection.y, 1f / rayDirection.z)
        val signDirX = invDir.x < 0
        val signDirY = invDir.y < 0
        val signDirZ = invDir.z < 0
        var bbox = if (signDirX) max else min
        var tmin: Double = (bbox.x() - rayOrigin.x) * invDir.x
        bbox = if (signDirX) min else max
        var tmax: Double = (bbox.x() - rayOrigin.x) * invDir.x
        bbox = if (signDirY) max else min
        val tymin: Double = (bbox.y() - rayOrigin.y) * invDir.y
        bbox = if (signDirY) min else max
        val tymax: Double = (bbox.y() - rayOrigin.y) * invDir.y
        if (tmin > tymax || tymin > tmax) {
            return null
        }
        if (tymin > tmin) {
            tmin = tymin
        }
        if (tymax < tmax) {
            tmax = tymax
        }
        bbox = if (signDirZ) max else min
        val tzmin: Double = (bbox.z() - rayOrigin.z) * invDir.z
        bbox = if (signDirZ) min else max
        val tzmax: Double = (bbox.z() - rayOrigin.z) * invDir.z
        if (tmin > tzmax || tzmin > tmax) {
            return null
        }
        if (tzmin > tmin) {
            tmin = tzmin
        }
        return tmin
    }

    fun getTargetBlockPoint(origin: Vec, direction: Vec, maxDistance: Double, instance: Instance): Point? {
        BlockIterator(origin, direction, 0.0, maxDistance).let {
            var block: Block
            while (it.hasNext()) {
                val position = it.next()
                if (position.y() !in -64.0..319.0 || !instance.isChunkLoaded(position)) return null
                block = instance.getBlock(position)
                if (!block.isAir && !block.isLiquid && block != Block.BARRIER) return position // TODO: 블록 세부 히트박스 계산
            }
            return null
        }
    }

    fun getTargetBlockIntersection(origin: Vec, direction: Vec, maxDistance: Double, instance: Instance): Double? {
        val targetBlockPos = getTargetBlockPoint(origin, direction, maxDistance, instance) ?: return null
        return rayIntersectsBlock(origin, direction, targetBlockPos)
    }
}