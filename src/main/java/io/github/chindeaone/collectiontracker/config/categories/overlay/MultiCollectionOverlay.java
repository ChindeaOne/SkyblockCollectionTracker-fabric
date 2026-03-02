package io.github.chindeaone.collectiontracker.config.categories.overlay;

import com.google.gson.annotations.Expose;
import io.github.chindeaone.collectiontracker.config.core.Position;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;

public class MultiCollectionOverlay {

    @Expose
    @ConfigLink(owner = MultiCollectionOverlay.class, field = "multiCollectionOverlay")
    public Position multiOverlayPosition = new Position(50,500);
}
