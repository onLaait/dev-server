package com.github.onlaait.fbw.model

import com.github.onlaait.fbw.server.Schedule
import com.google.gson.JsonElement
import net.minestom.server.MinecraftServer
import net.minestom.server.timer.ExecutionType
import net.minestom.server.timer.Task
import net.minestom.server.timer.TaskSchedule
import net.worldseed.multipart.GenericModel
import net.worldseed.multipart.ModelLoader
import net.worldseed.multipart.animations.*
import net.worldseed.multipart.animations.AnimationHandler.AnimationDirection
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Consumer

class FAnimationHandler(private val model: GenericModel) : AnimationHandler {

    private val task: Task

    private val animations: MutableMap<String, ModelAnimation> = ConcurrentHashMap()
    private val repeating = TreeMap<Int, ModelAnimation>()
    private var playingOnce: String? = null

    private val callbacks: MutableMap<String, Runnable> = ConcurrentHashMap()
    private val callbackTimers: MutableMap<String, Int> = ConcurrentHashMap()

    init {
        loadDefaultAnimations()
        this.task = MinecraftServer.getSchedulerManager()
            .scheduleTask({ this.tick() }, TaskSchedule.immediate(), Schedule.NEXT_CLIENT_TICK, ExecutionType.TICK_END)
    }

    private fun loadDefaultAnimations() {
        val models = mutableSetOf("default.bbmodel")
        model.id?.let { models += it }
        var i = 0
        models.forEach {
            val loadedAnimations = ModelLoader.loadAnimations(it)
            // Init animation
            for ((key, value) in loadedAnimations["animations"].asJsonObject.entrySet()) {
                registerAnimation(key, value, i)
                i--
            }
        }
    }

    override fun registerAnimation(name: String, animation: JsonElement, priority: Int) {
        val animationLength = animation.asJsonObject["animation_length"]
        val length = animationLength?.asDouble ?: 0.0

        val animationSet = HashSet<BoneAnimation>()
        val animatedBones = HashSet<String>()

        for ((boneName, value) in animation.asJsonObject["bones"].asJsonObject.entrySet()) {
            val bone = model.getPart(boneName) ?: continue

            val animationRotation = value.asJsonObject["rotation"]
            val animationPosition = value.asJsonObject["position"]
            val animationScale = value.asJsonObject["scale"]

            var animated = false

            if (animationRotation != null) {
                animated = true
                val boneAnimation = BoneAnimationImpl(
                    model.id,
                    name,
                    boneName,
                    bone,
                    animationRotation,
                    ModelLoader.AnimationType.ROTATION,
                    length
                )
                animationSet.add(boneAnimation)
            }
            if (animationPosition != null) {
                animated = true
                val boneAnimation = BoneAnimationImpl(
                    model.id,
                    name,
                    boneName,
                    bone,
                    animationPosition,
                    ModelLoader.AnimationType.TRANSLATION,
                    length
                )
                animationSet.add(boneAnimation)
            }
            if (animationScale != null) {
                animated = true
                val boneAnimation = BoneAnimationImpl(
                    model.id,
                    name,
                    boneName,
                    bone,
                    animationScale,
                    ModelLoader.AnimationType.SCALE,
                    length
                )
                animationSet.add(boneAnimation)
            }

            if (animated) {
                animatedBones.add(boneName)
            }
        }

        animations[name] = ModelAnimationClassic(name, (length * 20).toInt(), priority, animationSet, animatedBones)
    }

    override fun registerAnimation(animator: ModelAnimation) {
        animations[animator.name()] = animator
    }

    @Throws(IllegalArgumentException::class)
    override fun playRepeat(animation: String) {
        playRepeat(animation, AnimationDirection.FORWARD)
    }

    @Throws(IllegalArgumentException::class)
    override fun playRepeat(animation: String, direction: AnimationDirection) {
        requireNotNull(animationPriorities()[animation]) { "Animation $animation does not exist" }
        val modelAnimation = animations[animation]

        if (repeating.containsKey(animationPriorities()[animation])
            && modelAnimation!!.direction() == direction
        ) return

        modelAnimation!!.setDirection(direction)

        repeating[animationPriorities()[animation]!!] = modelAnimation
        val top = repeating.firstEntry()

        if (top != null && animation == top.value!!.name()) { //The animation you want to play is the highest priority
            repeating.values.forEach(Consumer { v: ModelAnimation? ->
                if (v!!.name() != animation) { //Stop all lower priority animations to ensure the correct one is playing
                    v.stop() //The extra loop seemed redundant, please let me know if this breaks something
                }
            })
            if (playingOnce == null) {
                modelAnimation.play(false) //Start the repeating animation if no playOnce animation is currently playing
            }
        }
    }

    @Throws(IllegalArgumentException::class)
    override fun stopRepeat(animation: String) {
        requireNotNull(animationPriorities()[animation]) { "Animation $animation does not exist" }

        val modelAnimation = animations[animation]

        modelAnimation!!.stop() //Stop the highest priority repeating animation
        val priority = animationPriorities()[animation]!!

        val currentTop = repeating.firstEntry()

        repeating.remove(priority)

        val firstEntry = repeating.firstEntry()

        if (this.playingOnce == null && firstEntry != null && currentTop != null && (firstEntry.key != currentTop.key)) {
            firstEntry.value!!.play(false) //Restart the new highest priority repeating animation
        }
    }


    @Throws(IllegalArgumentException::class)
    override fun playOnce(animation: String, cb: Runnable) {
        this.playOnce(animation, true, cb)
    }

    @Throws(IllegalArgumentException::class)
    override fun playOnce(animation: String, override: Boolean, cb: Runnable) {
        this.playOnce(animation, AnimationDirection.FORWARD, override, cb)
    }

    @Throws(IllegalArgumentException::class)
    override fun playOnce(animation: String, direction: AnimationDirection, override: Boolean, cb: Runnable) {
        requireNotNull(animationPriorities()[animation]) { "Animation $animation does not exist" }

        val modelAnimation = animations[animation]

        val currentDirection = modelAnimation!!.direction()
        modelAnimation.setDirection(direction)

        if (callbacks.containsKey(animation)) { //This animation had a pending runnable
            callbacks[animation]!!.run() //Run callback runnable
        }

        val callbackTimer = callbackTimers.getOrDefault(animation, 0)

        if (animation == this.playingOnce && direction == AnimationDirection.PAUSE && callbackTimer > 0) { //This animation was already playing, paused and not finished
            // Pause. Only call if we're not stopped
            playingOnce = animation
            callbacks[animation] = cb
        } else if (animation == this.playingOnce && currentDirection != direction) { //This animation was already playing, but in a different direction
            playingOnce = animation
            callbacks[animation] = cb
            if (currentDirection != AnimationDirection.PAUSE) callbackTimers[animation] =
                modelAnimation.animationTime() - callbackTimer + 1
        } else if (direction != AnimationDirection.PAUSE) { //This animation was not playing, or it was in the same direction
            if (playingOnce != null) { //Stop current animation
                animations[playingOnce]!!.stop()
                modelAnimation.stop()
            }
            playingOnce = animation

            callbacks[animation] = cb
            callbackTimers[animation] = modelAnimation.animationTime()
            modelAnimation.play(false)

            val animatedBones = modelAnimation.animatedBones
            repeating.values.forEach(Consumer { v: ModelAnimation? ->
                if (v!!.name() != animation) {
                    if (override) {
                        v.stop() //Stop all repeating animations
                    } else {
                        v.stop(animatedBones) //Stop all 'animatedBones' for all repeating animations
                    }
                }
            })
        }
    }

    private fun tick() {
        try {
            for ((key, value) in callbackTimers) {
                val modelAnimation = animations[key] //Get playOnce animation from string

                if (value <= 0) { //All ticks were removed so playOnce should end
                    if (this.playingOnce != null && this.playingOnce == key) {
                        val firstEntry = repeating.firstEntry()
                        if (firstEntry != null) {
                            firstEntry.value!!.play(true) //Restart or resume the highest priority repeating animation
                        }
                        this.playingOnce = null
                    }

                    model.triggerAnimationEnd(key, modelAnimation!!.direction()) //Call AnimationCompleteEvent

                    modelAnimation.stop()
                    callbackTimers.remove(key) //Remove playOnce animation from map

                    val cb = callbacks.remove(key)
                    cb?.run() //Run 'callback' runnable
                } else {
                    if (modelAnimation!!.direction() != AnimationDirection.PAUSE) {
                        callbackTimers[key] =
                            value - 1 //Countdown 1 tick until it reaches 0 during playOnce animation
                    }
                }
            }

            if (callbacks.size + repeating.size == 0) return  //Return if no playOnce or repeating animation is playing

            animations.forEach { (_, animation) ->
                animation.tick() //Play every tick (besides the first one) of the animation
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun destroy() {
        task.cancel()
    }

    override fun getPlaying(): String? {
        if (this.playingOnce != null) return this.playingOnce
        return getRepeating()
    }

    override fun getRepeating(): String? {
        val playing = repeating.firstEntry()
        return if (playing != null) playing.value!!.name() else null
    }

    override fun getAnimation(animation: String): ModelAnimation? {
        return animations[animation]
    }

    override fun animationPriorities(): Map<String, Int> {
        return object : HashMap<String, Int>() {
            init {
                for ((key, value) in animations) {
                    put(key, value.priority())
                }
            }
        }
    }
}
