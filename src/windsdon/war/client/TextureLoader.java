package windsdon.war.client;

import java.awt.Component;
import java.awt.Image;
import java.awt.MediaTracker;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 *
 * @author Windsdon
 */
public class TextureLoader extends Thread {

    public HashMap<String, Image> textures;
    private ArrayList<String[]> resources;
    private MediaTracker tracker;
    private boolean started;

    public TextureLoader(int initialSize, Component c) {
        textures = new HashMap<>(initialSize);
        resources = new ArrayList<>();
        tracker = new MediaTracker(c);
    }

    public Image getTexture(String textureID) {
        return textures.get(textureID);
    }

    public double getCompletion() {
        return (double) (textures.size()) / resources.size();
    }

    public void addLoadingList(String u) {
        List<String> lines;
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

    private Image loadImage(String res) {
        try {
            return ImageIO.read(TextureLoader.class.getClassLoader().getResource("res/" + res));
        } catch (IOException ex) {
            System.out.println("Failed to load " + res);
            return null;
        }
    }

    @Override
    public void run() {
        started = true;

        for (int i = 0; i < resources.size(); i++) {
            String[] res = resources.get(i);
            Image image = loadImage(res[1]);
            tracker.addImage(image, i);
            try {
                tracker.waitForID(i);
            } catch (InterruptedException ex) {
                System.out.println("Failed to load " + i);
            }

            textures.put(res[0], image);
        }
    }

    public boolean isStarted() {
        return started;
    }
}
