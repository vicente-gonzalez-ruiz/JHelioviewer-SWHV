package org.helioviewer.jhv.timelines.draw;

import java.awt.Color;
import java.awt.Font;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;

import org.helioviewer.jhv.gui.UIGlobals;
import org.helioviewer.jhv.math.MathUtils;

public class DrawConstants {

    public static final int GRAPH_LEFT_SPACE = 50;
    public static final int GRAPH_RIGHT_SPACE = 10;
    public static final int GRAPH_TOP_SPACE = 20;
    public static final int GRAPH_BOTTOM_SPACE = 8;
    public static final int GRAPH_BOTTOM_AXIS_SPACE = 18;
    public static final int RIGHT_AXIS_WIDTH = 30;

    public static final int INTERVAL_SELECTION_HEIGHT = 20;
    public static final int RANGE_SELECTION_WIDTH = 15;

    public static final Color AVAILABLE_INTERVAL_BACKGROUND_COLOR = new Color(224, 224, 224);
    public static final Color SELECTED_INTERVAL_BACKGROUND_COLOR = Color.WHITE;
    public static final Color BORDER_COLOR = new Color(182, 190, 206);

    public static final Color TICK_LINE_COLOR = Color.LIGHT_GRAY;
    public static final Color LABEL_TEXT_COLOR = Color.BLACK;
    public static final Color TEXT_COLOR = Color.WHITE;
    public static final Color TEXT_BACKGROUND_COLOR = Color.GRAY;

    public static final Color MOVIE_FRAME_COLOR = Color.BLACK;
    public static final Color MOVIE_INTERVAL_COLOR = Color.LIGHT_GRAY;

    public static final DateTimeFormatter FULL_DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd\nHH:mm:ss");
    public static final DateTimeFormatter FULL_DATE_TIME_FORMAT_NO_SEC = DateTimeFormatter.ofPattern("yyyy-MM-dd\nHH:mm");
    public static final DateTimeFormatter FULL_DATE_TIME_FORMAT_REVERSE = DateTimeFormatter.ofPattern("HH:mm:ss\nyyyy-MM-dd");

    public static final DateTimeFormatter HOUR_TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");
    public static final DateTimeFormatter HOUR_TIME_FORMAT_NO_SEC = DateTimeFormatter.ofPattern("HH:mm");

    public static final DateTimeFormatter DAY_MONTH_YEAR_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter MONTH_YEAR_TIME_FORMAT = DateTimeFormatter.ofPattern("MMM yyyy");
    public static final DateTimeFormatter YEAR_ONLY_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy");

    public static final int EVENT_OFFSET = 3;

    public static final Font font = UIGlobals.uiFontSmall;
    public static final Font fontBold = UIGlobals.uiFontSmallBold;

    public static final DecimalFormat valueFormatter = MathUtils.numberFormatter("0", 4);

    public static final double DISCARD_LEVEL_LINEAR_LOW = -100;
    public static final double DISCARD_LEVEL_LOG_LOW = 1e-10;
    public static final double DISCARD_LEVEL_HIGH = 1e7; // solar wind temp, xray flux: 1e4;

}
