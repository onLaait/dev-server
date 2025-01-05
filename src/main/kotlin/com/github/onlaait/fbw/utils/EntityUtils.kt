package com.github.onlaait.fbw.utils

import net.minestom.server.entity.Entity
import net.minestom.server.entity.metadata.EntityMeta

inline fun <reified TMeta : EntityMeta> Entity.editMeta(noinline block: TMeta.() -> Unit) {
    editEntityMeta(TMeta::class.java, block)
}