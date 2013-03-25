package windsdon.war.server;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Windsdon
 */
class ServerConfig {

    public static final String SERVER_NAME = "server_name";
    public static final String ALLOW_SPECTATORS = "allow_spectators";
    public static final String AUTO_START = "auto_start";
    public static final String DEFAULT_MAP = "default_map";
    public static final String TURN_TIMEOUT = "turn_timeout";
    public static final String TURN_DURATION = "turn_duration";
    public static final String TURN_TIMING = "turn_timing";
    public static final String ALLOW_TIME_ACCUMULATION = "allow_time_accumulation";
    public static final String SUDDEN_DEATH_TIME = "sudden_death_time";
    public static final String SUDDEN_DEATH_TIMEOUT = "sudden_death_timeout";
    public static final String SUDDEN_DEATH = "sudden_death";
    public static final String MAX_PLAYERS = "max_players";
    public static final String SERVER_PASSWORD = "server_password";
    public static final String COLORS = "colors";
    public static int COLOR_BLACK = 1;
    public static int COLOR_WHITE = 2;
    public static int COLOR_BLUE = 4;
    public static int COLOR_GREEN = 8;
    public static int COLOR_YELLOW = 16;
    public static int COLOR_RED = 32;
    public static int COLOR_GRAY = 32;
    public HashMap<String, String> map;

    public ServerConfig() {
        generateDefaultConfig();
    }

    public ServerConfig(String[] lines) {
        map = FileInterpreter.getMap(lines);
        generateDefaultConfig();
    }

    private void generateDefaultConfig() {
        if (map == null) {
            map = new HashMap<>();
        }

        if (!map.containsKey(SERVER_NAME)) {
            map.put(SERVER_NAME, "War Server");
        }

        if (!map.containsKey(ALLOW_SPECTATORS)) {
            map.put(ALLOW_SPECTATORS, "true");
        }

        if (!map.containsKey(AUTO_START)) {
            map.put(AUTO_START, "false");
        }

        if (!map.containsKey(DEFAULT_MAP)) {
            map.put(DEFAULT_MAP, "");
        }

        if (!map.containsKey(TURN_TIMEOUT)) {
            map.put(TURN_TIMEOUT, "5");
        }

        if (!map.containsKey(TURN_DURATION)) {
            map.put(TURN_DURATION, "240");
        }

        if (!map.containsKey(TURN_TIMING)) {
            map.put(TURN_TIMING, "true");
        }

        if (!map.containsKey(ALLOW_TIME_ACCUMULATION)) {
            map.put(ALLOW_TIME_ACCUMULATION, "false");
        }

        if (!map.containsKey(SUDDEN_DEATH)) {
            map.put(SUDDEN_DEATH, "false");
        }

        if (!map.containsKey(SUDDEN_DEATH_TIME)) {
            map.put(SUDDEN_DEATH_TIME, "");
        }

        if (!map.containsKey(SUDDEN_DEATH_TIMEOUT)) {
            map.put(SUDDEN_DEATH_TIMEOUT, "");
        }

        if (!map.containsKey(MAX_PLAYERS)) {
            map.put(MAX_PLAYERS, "4");
        }

        if (!map.containsKey(SERVER_PASSWORD)) {
            map.put(SERVER_PASSWORD, "");
        }

        if (!map.containsKey(COLORS)) {
            map.put(COLORS, Integer.toString(COLOR_BLACK | COLOR_WHITE | COLOR_BLUE | COLOR_GREEN | COLOR_YELLOW | COLOR_RED | COLOR_GRAY));
        }
    }

    public String[] getFile() {
        ArrayList<String> lines = new ArrayList<>();
        lines.add("#War Server config file");
        lines.add("#" + new SimpleDateFormat("dd/MM/yyyy hh:mm:ss aa").format(new Date()));
        for (Map.Entry<String, String> entry : map.entrySet()) {
            String index = entry.getKey();
            String value = entry.getValue();

            lines.add(index.concat("=").concat(value));
        }

        return lines.toArray(new String[0]);
    }

    public String[] getConfigLines() {
        ArrayList<String> lines = new ArrayList<>();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            String index = entry.getKey();
            String value = entry.getValue();

            lines.add("§[fcad00]".concat(index).concat(" §r=§[90ebff] ").concat(value));
        }

        return lines.toArray(new String[0]);
    }

    public boolean exists(String s) {
        return map.containsKey(s);
    }

    public boolean isValueValid(String key, String value) {
        if (!exists(key)) {
            return false;
        }

        switch (key) {
            case SERVER_NAME:
            case SERVER_PASSWORD:
                return true;
            case SUDDEN_DEATH:
            case ALLOW_SPECTATORS:
            case ALLOW_TIME_ACCUMULATION:
            case AUTO_START:
                return "true".equals(value.toLowerCase()) || "false".equals(value.toLowerCase());
            case SUDDEN_DEATH_TIME:
            case SUDDEN_DEATH_TIMEOUT:
            case TURN_DURATION:
            case TURN_TIMEOUT:
                try {
                    Integer.parseInt(value);
                    return "".equals(value);
                } catch (NumberFormatException e) {
                    return false;
                }
            case MAX_PLAYERS:
                try {
                    int v = Integer.parseInt(value);
                    return v > 1;
                } catch (NumberFormatException e) {
                    return false;
                }
            case COLORS:
                try {
                    int v = Integer.parseInt(value);
                    return v <= (COLOR_BLACK | COLOR_WHITE | COLOR_BLUE | COLOR_GREEN | COLOR_YELLOW | COLOR_RED | COLOR_GRAY);
                } catch (NumberFormatException e) {
                    return false;
                }
            case DEFAULT_MAP:
                return true;
            default:
                return false;
        }
    }

    public void setOption(String key, String value) {
        if (exists(key) && isValueValid(key, value)) {
            map.remove(key);
            map.put(key, value);
        }
    }

    public String getOption(String string) {
        if(!exists(string)){
            throw new IllegalArgumentException();
        }
        return map.get(string);
    }
}
