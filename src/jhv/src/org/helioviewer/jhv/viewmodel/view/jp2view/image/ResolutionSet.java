package org.helioviewer.jhv.viewmodel.view.jp2view.image;

/**
 * A class describing the available resolution levels for a given image. It
 * supplies several simple methods to aid in selecting appropriate zoom levels.
 *
 * @author caplins
 */
public class ResolutionSet {

    /**
     * An array containing the available resolutions. The index's represent the
     * num of discardLayers
     */
    private final ResolutionLevel[] resolutions;
    public final int components;

    public ResolutionSet(int _resolutions, int _components) {
        resolutions = new ResolutionLevel[_resolutions];
        components = _components;
    }

    /**
     * Adds a resolution level. Used while setting the object up. This method is
     * really the only place a ResolutionLevel object is ever created. NOTE:
     * Normally the resolution levels will always have an origin @ (0,0) and
     * hence a Dimension object would be enough to describe the layer bounds. In
     * some situations this might not be true though, and for extendability I
     * use a rectangle object instead of a Dimension object (even though I don't
     * think I ever explicitly use the origin).
     *
     * @param discardLayer
     * @param _dims
     */
    public void addResolutionLevel(int discardLayer, int width, int height, double scaleX, double scaleY) {
        resolutions[discardLayer] = new ResolutionLevel(discardLayer, width, height, scaleX, scaleY);
    }

    public ResolutionLevel getResolutionLevel(int index) {
        return resolutions[index];
    }

    public ResolutionLevel getPreviousResolutionLevel(int w, int h) {
        int idx = 0;
        for (int i = 0; i < resolutions.length; i++) {
            idx = i;
            if (resolutions[i].width <= w && resolutions[i].height <= h)
                break;
        }
        return resolutions[idx];
    }

    public ResolutionLevel getNextResolutionLevel(int w, int h) {
        for (int i = 1; i < resolutions.length; ++i) {
            if (resolutions[i].width < w || resolutions[i].height < h)
                return resolutions[i - 1];
        }
        return resolutions[resolutions.length - 1];
    }

    public int getMaxResolutionLevels() {
        return resolutions.length - 1;
    }

    /**
     * Immutable class describing a Resolution level for a given image. Note
     * that while this class is public, its constructor is private. The
     * ResolutionSet object can be considered to be the ResolutionLevel
     * 'factory'.
     */
    public static class ResolutionLevel implements Comparable<ResolutionLevel> {

        public final int discardLayers;

        public final float scaleLevel;

        public final int width;
        public final int height;

        public final double factorX;
        public final double factorY;

        // Private constructor
        private ResolutionLevel(int _discardLayers, int _width, int _height, double _factorX, double _factorY) {
            discardLayers = _discardLayers;
            scaleLevel = 1f / (1 << discardLayers);
            width = _width;
            height = _height;
            factorX = _factorX;
            factorY = _factorY;
        }

        /**
         * The equals method. Since these objects are immutable, can only be
         * instantiated in this class, and are not cloneable you can almost
         * certain do reference comparisons using the ==, but since I am
         * paranoid I have overridden the equals method.
         */
        @Override
        public boolean equals(Object o) {
            if (o instanceof ResolutionLevel) {
                ResolutionLevel r = (ResolutionLevel) o;
                return discardLayers == r.discardLayers && width == r.width && height == r.height && factorX == r.factorX && factorY == r.factorY;
            }
            return false;
        }

        @Override
        public int hashCode() {
            assert false : "hashCode not designed";
            return 42;
        }

        @Override
        public String toString() {
            return "[[Discard=" + discardLayers + "][ScaleLevel=" + scaleLevel + "][ScaleFactor=" + factorX + "," + factorY + "][ZoomDims=" + width + "," + height + "]]";
        }

        @Override
        public int compareTo(ResolutionLevel r) {
            assert width == r.width && height == r.height && factorX == r.factorX && factorY == r.factorY : "not comparable";
            int diff = discardLayers - r.discardLayers;
            return diff > 0 ? -1 : (diff < 0 ? +1 : 0);
        }

    }

}
