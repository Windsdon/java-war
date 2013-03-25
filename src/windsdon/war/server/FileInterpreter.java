package windsdon.war.server;

import java.util.HashMap;

/**
 *
 * @author Windsdon
 */
public abstract class FileInterpreter {

    public static HashMap<String, String> getMap(String[] lines) {
        HashMap<String, String> map = new HashMap<>(lines.length);
        for (int i = 0; i < lines.length; i++) {
            String string = lines[i];
            if (string.matches(" *#+.*") || !string.matches("[A-Za-z0-9_-]+=.*")) {
                continue;
            } else {
                int equalspos = string.indexOf("=");
                map.put(string.substring(0, equalspos).toLowerCase(), string.substring(equalspos + 1));
            }
        }
        return map;
    }
}
