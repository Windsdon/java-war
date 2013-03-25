package windsdon.war.client;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

/**
 *
 * @author Windsdon
 */
public class InputManager {
    private ArrayList<TextInput> list;
    
    public InputManager(){
        list = new ArrayList<>();
    }
    
    public void add(TextInput e){
        list.add(e);
    }
    
    public void updateMouse(MouseEvent e){
        for (int i = 0; i < list.size(); i++) {
            TextInput textInput = list.get(i);
            textInput.updateMouse(e);
            if(textInput.hasFocus()){
                break;
            }
        }
    }
    
    public void updateKeyboard(KeyEvent e){
        for (int i = 0; i < list.size(); i++) {
            TextInput textInput = list.get(i);
            textInput.updateKeyboard(e);
        }
    }

    void deactivateAll() {
        for (int i = 0; i < list.size(); i++) {
            TextInput textInput = list.get(i);
            textInput.setActive(false);
        }
    }
}
