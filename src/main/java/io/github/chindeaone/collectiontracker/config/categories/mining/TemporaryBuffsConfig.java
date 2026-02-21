package io.github.chindeaone.collectiontracker.config.categories.mining;

import com.google.gson.annotations.Expose;
import io.github.chindeaone.collectiontracker.config.core.Position;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class TemporaryBuffsConfig {

    @Expose
    @ConfigOption(
            name = "Enable Temporary Buffs Tracker",
            desc = "Toggles an overlay that tracks mining temporary buffs."
    )
    @ConfigEditorBoolean
    public boolean enableTempBuffTracker = false;

    @Expose
    @ConfigLink(owner = TemporaryBuffsConfig.class, field = "tempBuffTracker")
    public Position tempBuffPosition = new Position(400, 100);

    @Expose
    public long refinedCacaoTime = 0L;
    @Expose
    public long filetTime = 0L;
    @Expose
    public long pristinePotatoTime = 0L;
    @Expose
    public long powderPumpkinTime = 0L;
}