package io.github.chindeaone.collectiontracker.config.categories.foraging;

import com.google.gson.annotations.Expose;
import io.github.chindeaone.collectiontracker.config.core.Position;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class LotteryConfig {

    @Expose
    @ConfigOption(
            name = "Better Lottery",
            desc = "Displays current Lottery perks and compacts Lottery chat messages"
    )
    @ConfigEditorBoolean
    public boolean enableLottery = false;

    @Expose
    public String lastLotteryPerk = "";

    @Expose
    @ConfigOption(
            name = "Foraging Islands Only",
            desc = "Allows the Lottery overlay to be rendered only in Foraging Islands."
    )
    @ConfigEditorBoolean
    public boolean lotteryInForagingIslandsOnly = true;

    @Expose
    @ConfigOption(
            name = "Disable Lottery chat messages",
            desc = "Hides Lottery chat messages while displaying perks in the overlay and allowing other mods to process the messages."
    )
    @ConfigEditorBoolean
    public boolean disableLotteryChatMessages = false;

    @Expose
    @ConfigLink(owner = HotfConfig.class, field = "Lottery")
    public Position lotteryPosition = new Position(500, 100);
}
