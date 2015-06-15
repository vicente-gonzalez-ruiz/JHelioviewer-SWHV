package org.helioviewer.viewmodel.view;

import org.helioviewer.base.datetime.ImmutableDateTime;
import org.helioviewer.viewmodel.view.cache.ImageCacheStatus;

/**
 * View to interact with image series.
 * <p>
 * This interface is designed to interact with image series, such as JPX-Files.
 * It provides basic functions to navigate within the image series.
 * <p>
 * This interface is designed to interact with time stamp providing image
 * series. Besides from basic functions like set/getCurrentFrame, there is one
 * additional mechanism, that should be explained: Image series with time stamps
 * provide two basic modes, how to play an movie: The show every frame,
 * independent from the different time stamps of each frame or the speed of the
 * computer. This mode will be called "relative mode" or "simple mode". The
 * other one, called "absolute mode" or "physical mode", shows the frames in a
 * way that the physical time you can see stays constant on every machine. This
 * affects two aspects: If the desired speed is too high for the computer, the
 * TimedMovieView should automatically skip frames to keep its speed. On the
 * other hand, if the time gaps between frames vary, the TimedMovieView should
 * adjust its frame rate as well.
 *
 * @author Markus Langenberg
 */
public interface MovieView extends View {

    /**
     * Animation mode.
     *
     * @see MovieView#setAnimationMode(AnimationMode)
     */
    public enum AnimationMode {
        LOOP {
            @Override
            public String toString() {
                return "Loop";
            }
        },
        STOP {
            @Override
            public String toString() {
                return "Stop";
            }
        },
        SWING {
            @Override
            public String toString() {
                return "Swing";
            }
        }
    }

    /**
     * Returns the current frame number.
     *
     * @return current frame number
     * @see #setCurrentFrame
     * @see TimedMovieView#setCurrentFrame
     */
    public int getCurrentFrameNumber();

    /**
     * Returns the maximum frame number.
     *
     * @return maximum frame number
     */
    public int getMaximumFrameNumber();

    /**
     * Returns the last accessible frame number.
     *
     * @return maximum accessible frame number
     */
    public int getMaximumAccessibleFrameNumber();

    /**
     * Sets the current animation mode.
     *
     * The animation mode describes, what should happen when the last frames is
     * reached:
     * <p>
     * LOOP: Go back to first frame and start again.<br>
     * STOP: Go back to first frame and stop playing.<br>
     * SWING: On reaching the last frame, play movie backwards, on reaching the
     * first frame again, play movie forwards.
     *
     * @param mode
     *            new animation mode
     */
    public void setAnimationMode(AnimationMode mode);

    /**
     * Sets the desired frame rate.
     *
     * Implicit, switches to relative mode and thus overrides all settings
     * previously set by {@link TimedMovieView#setDesiredAbsoluteSpeed}.
     *
     * @param framesPerSecond
     *            desired frame rate
     * @see TimedMovieView#setDesiredAbsoluteSpeed
     */
    public void setDesiredRelativeSpeed(int framesPerSecond);

   /**
     * Returns the image cache status.
     *
     * @return image cache status
     */
    public ImageCacheStatus getImageCacheStatus();

    /**
     * Returns time stamp of any frame specified.
     * 
     * @param frameNumber
     * @return time stamp of the requested frame
     */
    public ImmutableDateTime getFrameDateTime(int frameNumber);

    /**
     * Sets the desired absolute speed.
     * 
     * Implicit, switches to absolute mode and thus overrides all settings
     * previously set by {@link MovieView#setDesiredRelativeSpeed}
     * 
     * @param observationSecondsPerSecond
     *            desired absolute speed
     * @see MovieView#setDesiredRelativeSpeed
     */
    public void setDesiredAbsoluteSpeed(int observationSecondsPerSecond);

    // <!- only for Layers
    public boolean isMoviePlaying();

    public void playMovie();

    public void pauseMovie();

    public void setCurrentFrame(ImmutableDateTime time);
    // -->

}
