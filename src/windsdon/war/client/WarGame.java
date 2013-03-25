package windsdon.war.client;

import java.awt.Image;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import windsdon.war.editor.WarEditor;

/**
 *
 * @author Windsdon
 */
public class WarGame extends JFrame {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        new WarGame(args);
    }

    private WarGame(String[] args) {
        if(args!=null){
            for (int i = 0; i < args.length; i++) {
                String string = args[i];
                if(string.equals("edit")){
                    new WarEditor().setVisible(true);
                    return;
                }
            }
        }
        
        int w = 1280;
        int h = 720;
        
        Image icon = new ImageIcon(WarGame.class.getClassLoader().getResource("res/globe.png")).getImage();

        War game = new War(w, h, this);
        add(game);
        setTitle("War Online - V0.1 DEV");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        game.resize();
        
        //game.setBounds(0, 0, w, h);
        //setExtendedState(Frame.MAXIMIZED_BOTH);
        //setUndecorated(true);
        pack();
        setLocationRelativeTo(null);
        setResizable(false);
        setVisible(true);
        setIconImage(icon);

        //ti.displayMessage("Você está sendo atacado!", null, TrayIcon.MessageType.WARNING);

        System.out.println("start");

        game.start();
    }
}
