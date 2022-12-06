package obj;

public class GameSetting {
    public static final int MAX_NUM_OF_RACERS = 10;
    public static final int MAX_TIMER_SEC = 10;

    public static final int INIT_RACER_POSITION = 1;
    public static final String[] OPERATORS = {"+", "-", "*", "/ ", "%"};

    public static class RACER_STATUS_FLAG {
        public static final int FLAG_READY = 0;
        public static final int FLAG_NORMAL = 1;
        public static final int FLAG_FASTEST = 2;
        public static final int FLAG_WRONG = 3;
        public static final int FLAG_TIMEOUT = 4;
        public static final int FLAG_ELIMINATED = 5;
        public static final int FLAG_QUIT = 6;
        public static final int FLAG_VICTORY = 7;
    }
    public static final String[] STATUS_STRING = {
            "Racer Ready",
            "Correct",
            "Correct & Fastest",
            "Incorrect",
            "Timeout",
            "Eliminated",
            "Rage Quit",
            "Victory"
    };
}
