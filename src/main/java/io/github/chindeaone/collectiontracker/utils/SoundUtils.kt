package io.github.chindeaone.collectiontracker.utils

import net.minecraft.client.Minecraft
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.sounds.SoundEvents

object SoundUtils {

    fun playSound() {
        val soundManager = Minecraft.getInstance().soundManager
        val soundInstance = SimpleSoundInstance.forUI(SoundEvents.PLAYER_LEVELUP, 1.0f)

        soundManager.play(soundInstance)
    }
}