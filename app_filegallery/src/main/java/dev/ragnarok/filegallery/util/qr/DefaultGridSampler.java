package dev.ragnarok.filegallery.util.qr;

/**
 * @author Sean Owen
 */
public final class DefaultGridSampler {

    public static BitMatrix sampleGrid(BitMatrix image,
                                       int dimensionX,
                                       int dimensionY,
                                       float p1ToX, float p1ToY,
                                       float p2ToX, float p2ToY,
                                       float p3ToX, float p3ToY,
                                       float p4ToX, float p4ToY,
                                       float p1FromX, float p1FromY,
                                       float p2FromX, float p2FromY,
                                       float p3FromX, float p3FromY,
                                       float p4FromX, float p4FromY) throws NotFoundException {

        PerspectiveTransform transform = PerspectiveTransform.quadrilateralToQuadrilateral(
                p1ToX, p1ToY, p2ToX, p2ToY, p3ToX, p3ToY, p4ToX, p4ToY,
                p1FromX, p1FromY, p2FromX, p2FromY, p3FromX, p3FromY, p4FromX, p4FromY);

        return sampleGrid(image, dimensionX, dimensionY, transform);
    }

    private static void checkAndNudgePoints(BitMatrix image,
                                            float[] points) throws NotFoundException {
        int width = image.getWidth();
        int height = image.getHeight();
        // Check and nudge points from start until we see some that are OK:
        boolean nudged = true;
        int maxOffset = points.length - 1; // points.length must be even
        for (int offset = 0; offset < maxOffset && nudged; offset += 2) {
            int x = (int) points[offset];
            int y = (int) points[offset + 1];
            if (x < -1 || x > width || y < -1 || y > height) {
                throw NotFoundException.getNotFoundInstance();
            }
            nudged = false;
            if (x == -1) {
                points[offset] = 0.0f;
                nudged = true;
            } else if (x == width) {
                points[offset] = width - 1;
                nudged = true;
            }
            if (y == -1) {
                points[offset + 1] = 0.0f;
                nudged = true;
            } else if (y == height) {
                points[offset + 1] = height - 1;
                nudged = true;
            }
        }
        // Check and nudge points from end:
        nudged = true;
        for (int offset = points.length - 2; offset >= 0 && nudged; offset -= 2) {
            int x = (int) points[offset];
            int y = (int) points[offset + 1];
            if (x < -1 || x > width || y < -1 || y > height) {
                throw NotFoundException.getNotFoundInstance();
            }
            nudged = false;
            if (x == -1) {
                points[offset] = 0.0f;
                nudged = true;
            } else if (x == width) {
                points[offset] = width - 1;
                nudged = true;
            }
            if (y == -1) {
                points[offset + 1] = 0.0f;
                nudged = true;
            } else if (y == height) {
                points[offset + 1] = height - 1;
                nudged = true;
            }
        }
    }

    public static BitMatrix sampleGrid(BitMatrix image,
                                       int dimensionX,
                                       int dimensionY,
                                       PerspectiveTransform transform) throws NotFoundException {
        if (dimensionX <= 0 || dimensionY <= 0) {
            throw NotFoundException.getNotFoundInstance();
        }
        BitMatrix bits = new BitMatrix(dimensionX, dimensionY);
        float[] points = new float[2 * dimensionX];
        for (int y = 0; y < dimensionY; y++) {
            int max = points.length;
            float iValue = y + 0.5f;
            for (int x = 0; x < max; x += 2) {
                points[x] = (float) (x / 2) + 0.5f;
                points[x + 1] = iValue;
            }
            transform.transformPoints(points);
            // Quick check to see if points transformed to something inside the image;
            // sufficient to check the endpoints
            checkAndNudgePoints(image, points);
            try {
                for (int x = 0; x < max; x += 2) {
                    if (image.get((int) points[x], (int) points[x + 1])) {
                        // Black(-ish) pixel
                        bits.set(x / 2, y);
                    }
                }
            } catch (ArrayIndexOutOfBoundsException aioobe) {
                // This feels wrong, but, sometimes if the finder patterns are misidentified, the resulting
                // transform gets "twisted" such that it maps a straight line of points to a set of points
                // whose endpoints are in bounds, but others are not. There is probably some mathematical
                // way to detect this about the transformation that I don't know yet.
                // This results in an ugly runtime exception despite our clever checks above -- can't have
                // that. We could check each point's coordinates but that feels duplicative. We settle for
                // catching and wrapping ArrayIndexOutOfBoundsException.
                throw NotFoundException.getNotFoundInstance();
            }
        }
        return bits;
    }

}
