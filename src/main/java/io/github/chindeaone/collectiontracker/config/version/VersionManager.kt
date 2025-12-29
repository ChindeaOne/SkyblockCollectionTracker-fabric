package io.github.chindeaone.collectiontracker.config.version

import io.github.notenoughupdates.moulconfig.processor.MoulConfigProcessor

object VersionManager {

    fun injectConfigProcessor(processor: MoulConfigProcessor<*>) {
        processor.registerConfigEditor(VersionDisplay::class.java) { option, _ ->
            VersionCheck(option)
        }
    }
}