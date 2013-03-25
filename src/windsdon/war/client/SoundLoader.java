package windsdon.war.client;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javazoom.jl.decoder.Decoder;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;

/**
 *
 * @author Windsdon
 */
public class SoundLoader extends Thread {

    public HashMap<String, Player> players;
    private HashMap<Integer, Thread> playing;
    private ArrayList<String[]> resources;

    public SoundLoader() {
        players = new HashMap<>();
        playing = new HashMap<>();
        resources = new ArrayList<>();
    }

    @Override
    public void run() {
        for (int i = 0; i < resources.size(); i++) {
            String[] strings = resources.get(i);
            try {
                File f = new File(new URI(SoundLoader.class.getClassLoader().getResource("res/" + strings[1]).toString()));
                FileInputStream fis = new FileInputStream(f);
                BufferedInputStream bis = new BufferedInputStream(fis);
                players.put(strings[0], new Player(bis));
            } catch (FileNotFoundException e) {
                System.out.println("File not found: " + e.getMessage());
            } catch (JavaLayerException e) {
                System.out.println("Could not start player: " + e.getMessage());
            } catch (NullPointerException e) {
                System.out.println("Null pointer excpetion");
            } catch (URISyntaxException e) {
                System.out.println("File not Found");
            }
        }

    }

    public double getCompletion() {
        return ((double) players.size()) / resources.size();
    }

    public void addLoadingList(String u) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(TextureLoader.class.getClassLoader().getResourceAsStream(u)));
            String line = reader.readLine();
            while (line != null) {
                String[] res = line.split(",");
                resources.add(res);
                line = reader.readLine();
            }
        } catch (IOException ex) {
            System.out.println("Error while reading file");
        }
    }

    public void stopAll() {
        if(playing.isEmpty()){
            return;
        }
        for (Map.Entry<Integer, Thread> entry : playing.entrySet()) {
            Integer integer = entry.getKey();
            Thread thread = entry.getValue();
            thread.stop();
        }
    }
    
    public void stopPlayer(int pid){
        playing.get(pid).stop();
    }

    public int play(final String snd) {
        Thread p = new Thread() {
            @Override
            public void run() {
                try {
                    Player m = players.get(snd);
                    m.play();
                } catch (JavaLayerException ex) {
                }
            }
        };
        
        int ref = playing.size();
        
        playing.put(ref, p);
        p.start();
        
        return ref;
        
    }
}
