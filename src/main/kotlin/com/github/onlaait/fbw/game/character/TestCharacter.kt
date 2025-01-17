package com.github.onlaait.fbw.game.character

import com.github.onlaait.fbw.game.skill.Skill

interface Character {

    val name: String

    val skills: Map<String, Skill>

    val modelId: String
}