package serverdatabase;

public class ServerDBConfig {
    public static final String JDBC_DRIVER = "org.h2.Driver";
    public static final String DB_URL = "jdbc:h2:~/Racing Arena/racing-arena-db";

    // database credentials
    public static final String DB_USER = "sa";
    public static final String DB_PASS = "";

    // database constant
    public static final String TABLE = "RACER_ACCOUNT";
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String IS_ONLINE = "is_online";
    public static final String VICTORY = "victory";

    // query statement
    public static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE
            + " ("+ USERNAME + " VARCHAR(11), "
            + PASSWORD + " VARCHAR(17), "
            + VICTORY + " INTEGER, "
            + IS_ONLINE + " INTEGER, "
            + "PRIMARY KEY ( " + USERNAME + " ))";
}
