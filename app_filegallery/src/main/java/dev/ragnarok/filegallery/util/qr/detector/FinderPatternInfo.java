package dev.ragnarok.filegallery.util.qr.detector;

/**
 * <p>Encapsulates information about finder patterns in an image, including the location of
 * the three finder patterns, and their estimated module size.</p>
 *
 * @author Sean Owen
 */
public final class FinderPatternInfo {

    private final FinderPattern bottomLeft;
    private final FinderPattern topLeft;
    private final FinderPattern topRight;

    public FinderPatternInfo(FinderPattern[] patternCenters) {
        bottomLeft = patternCenters[0];
        topLeft = patternCenters[1];
        topRight = patternCenters[2];
    }

    public FinderPattern getBottomLeft() {
        return bottomLeft;
    }

    public FinderPattern getTopLeft() {
        return topLeft;
    }

    public FinderPattern getTopRight() {
        return topRight;
    }

}
