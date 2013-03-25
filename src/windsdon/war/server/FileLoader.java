package windsdon.war.server;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 *
 * @author Windsdon
 */
public abstract class FileLoader {

    public static String[] loadLines(FileInputStream in, Display console) throws IOException {
        BufferedReader reader;
        try{
             reader = new BufferedReader(new InputStreamReader(in));
        }catch(NullPointerException ex){
            throw new IOException();
        }
        ArrayList<String> lines = new ArrayList<>();
        try {
            String line = reader.readLine();
            while (line != null) {
                lines.add(line);
                line = reader.readLine();
            }
        } catch (IOException ex) {
            if (console != null) {
                console.addLine("§2Error while reading file.");
            }
            throw ex;
        }

        return lines.toArray(new String[0]);
    }
}
