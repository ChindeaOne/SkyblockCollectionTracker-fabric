package io.github.chindeaone.collectiontracker.tracker;

import java.util.List;

/**
 *
 */
public class TrackingRenderData {

    public final List<String> mainLines;
    public final List<String> extraLines;

    public TrackingRenderData(List<String> mainLines, List<String> extraLines) {
        this.mainLines = mainLines;
        this.extraLines = extraLines;
    }

    public static final TrackingRenderData EMPTY =
            new TrackingRenderData(List.of(), List.of());
}
