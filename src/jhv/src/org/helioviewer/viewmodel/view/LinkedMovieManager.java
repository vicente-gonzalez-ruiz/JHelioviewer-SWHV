package org.helioviewer.viewmodel.view;

import java.util.LinkedList;

import org.helioviewer.base.datetime.ImmutableDateTime;
import org.helioviewer.jhv.gui.components.MoviePanel;

/**
 * Class managing all linked movies.
 *
 * <p>
 * This class is responsible for synchronizing all linked movies. Therefore, all
 * linked movies have to call the various functions of this class. Then, all
 * other linked movies are set according to the new values.
 *
 * <p>
 * When playing a movie instead of just scrolling around, a master
 * movie is chosen, based on the average cadence of all linked movies. Only the
 * master movie is actually playing, all other movies are just set to the frame
 * closest to the one from the master movie.
 *
 * @author Markus Langenberg
 */
public class LinkedMovieManager {

    private static final LinkedList<MovieView> linkedMovies = new LinkedList<MovieView>();
    private static MovieView masterView;

    /**
     * Adds the given movie view to the set of linked movies.
     *
     * @param movieView
     *            View to add to the set of linked movies.
     */
    public static void linkMovie(AbstractView view) {
        if (!(view instanceof MovieView))
            return;

        MovieView movieView = (MovieView) view;
        if (movieView.getMaximumFrameNumber() > 0 && !linkedMovies.contains(movieView)) {
            linkedMovies.add(movieView);
            resetState();
        }
    }

    /**
     * Removes the given movie view from the set of linked movies.
     *
     * @param movieView
     *            View to remove from the set of linked movies.
     */
    public static void unlinkMovie(AbstractView view) {
        if (!(view instanceof MovieView))
            return;

        MovieView movieView = (MovieView) view;
        if (linkedMovies.contains(movieView)) {
            linkedMovies.remove(movieView);
            resetState();
            movieView.pauseMovie();
        }
    }

    /**
     * Plays the set of linked movies.
     */
    public static void playLinkedMovies() {
        if (masterView != null) {
            masterView.playMovie();
            MoviePanel.playStateChanged(true);
        }
    }

    /**
     * Pauses the set of linked movies.
     */
    public static void pauseLinkedMovies() {
        if (masterView != null) {
            masterView.pauseMovie();
            MoviePanel.playStateChanged(false);
        }
    }

    /**
     * Updates all linked movies according to the current frame of the master
     * frame.
     */
    public static void updateCurrentFrameToMaster(View view) {
        if (masterView == null || view != masterView)
            return;

        ImmutableDateTime masterTime = masterView.getCurrentFrameDateTime();
        for (MovieView movieView : linkedMovies) {
            if (movieView != masterView) {
                movieView.setCurrentFrame(masterTime);
            }
        }
    }

    /**
     * Updates all linked movies according to the given time stamp.
     *
     * @param dateTime
     *            time which should be matched as close as possible
     */
    public static void setCurrentFrame(ImmutableDateTime dateTime) {
        for (MovieView movieView : linkedMovies) {
            movieView.setCurrentFrame(dateTime);
        }
    }

    public static void setCurrentFrame(MovieView view, int frameNumber) {
        frameNumber = Math.max(0, Math.min(view.getMaximumFrameNumber(), frameNumber));
        setCurrentFrame(view.getFrameDateTime(frameNumber));
    }

    private static void resetState() {
        boolean wasPlaying = masterView != null && masterView.isMoviePlaying();
        if (wasPlaying)
            pauseLinkedMovies();
        updateMaster();
        if (wasPlaying)
            playLinkedMovies();
    }

    /**
     * Recalculates the master view.
     *
     * The master view is the view whose movie is actually playing, whereas all
     * other movies just jump to the frame closest to the current frame from the
     * master panel.
     */
    private static void updateMaster() {
        masterView = null;

        if (linkedMovies.isEmpty()) {
            return;
        } else if (linkedMovies.size() == 1) {
            masterView = linkedMovies.element();
            return;
        }

        long minimalInterval = Long.MAX_VALUE;
        MovieView minimalIntervalView = null;

        for (MovieView movie : linkedMovies) {
            int nframes = movie.getMaximumFrameNumber();
            long interval = movie.getFrameDateTime(nframes).getMillis() - movie.getFrameDateTime(0).getMillis();
            interval /= (nframes + 1);

            if (interval < minimalInterval) {
                minimalInterval = interval;
                minimalIntervalView = movie;
            }
        }

        masterView = minimalIntervalView;
    }

}
