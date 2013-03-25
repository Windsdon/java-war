package windsdon.war.client;

import java.awt.event.MouseEvent;
import java.util.ArrayList;

/**
 *
 * @author Windsdon
 */
public class ButtonManager{
    private ArrayList<WarButton> buttons;

    public ButtonManager(){
        buttons = new ArrayList<>();
    }
    
    public void add(WarButton b){
        buttons.add(b);
    }
    
    public void update(MouseEvent e) {
        for (int i = 0; i < buttons.size(); i++) {
            WarButton warButton = buttons.get(i);
            warButton.update(e);
            if(warButton.isOver()){
                break;
            }
        }
        
    }

    void deactivateAll() {
        for (int i = 0; i < buttons.size(); i++) {
            WarButton warButton = buttons.get(i);
            warButton.setActive(false);
        }
    }
}
