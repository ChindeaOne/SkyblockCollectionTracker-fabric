package io.github.chindeaone.collectiontracker.tracker;

import java.util.List;

/**
 * Data class for holding rendering lines for the tracking overlay.
 * @param mainLines
 * @param extraLines
 */
public record TrackingRenderData(List<String> mainLines, List<String> extraLines) {

    public static final TrackingRenderData EMPTY =
            new TrackingRenderData(List.of(), List.of());
}
