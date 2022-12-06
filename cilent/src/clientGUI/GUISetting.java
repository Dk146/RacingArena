package clientGUI;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

public class GUISetting {
    public static final String GAME_NAME = "Racing Arena";

    public static final int MAX_NICKNAME_LENGTH = 10;

    // color
    public static final Color BACKGROUND_COLOR = new Color(0xABDBE3);
    public static final Color BORDER_COLOR = new Color(0xE0E0E0);
    public static final Color LIGHT_ORANGE = new Color(0xEAB676);

    // button border
    private static Border line = BorderFactory.createLineBorder(GUISetting.BORDER_COLOR, 2);
    private static Border empty = new EmptyBorder(5, 5, 5, 5);
    public static final CompoundBorder BORDER = new CompoundBorder(line, empty);

    // timer configuration
    public static final int TIMER_MAX = 10;

    // panel size
    public static final int RACER_STAT_PANEL_WIDTH = 200;
    public static final int RACER_STAT_PANEL_LABEL_WIDTH = 35;

    public static final int MIN_RACE_LENGTH = 3;
    public static final int MAX_RACE_LENGTH = 26;
    public static final int INIT_POSITION = 1;

    // color theme button configuration
    public static class ColorButtonConfig {
        public static final int COLOR_BUTTON_SIZE = 15;
    }
}


