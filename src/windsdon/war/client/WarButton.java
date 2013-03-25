package windsdon.war.client;

import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D.Float;

/**
 *
 * @author Windsdon
 */
public class WarButton {

    private Shape hitbox;
    private boolean isOver;
    private boolean isPressed;
    private boolean isActive;
    private int mask;
    private String text;
    private AffineTransform at;
    private double x;
    private double y;
    private Point2D mousePos;
    private boolean isTriggered;
    private boolean hasSoundPlayed;

    public WarButton(Shape hitbox, String text, double x, double y) {
        this.hitbox = hitbox;
        this.text = text;
        this.x = x;
        this.y = y;
        this.mask = MouseEvent.BUTTON1;
        isOver = false;
        isPressed = false;
        at = new AffineTransform();
        hasSoundPlayed = false;
    }

    public void update(MouseEvent e) {
        if (!isActive) {
            return;
        }

        AffineTransform transform = AffineTransform.getTranslateInstance(x, y);
        transform.concatenate(at);
        Shape thb = transform.createTransformedShape(hitbox);

        mousePos = e.getPoint();
        boolean oio = isOver, oip = isPressed;
        switch (e.getID()) {
            case MouseEvent.MOUSE_MOVED:
                isOver = thb.contains(mousePos);
                break;
            case MouseEvent.MOUSE_DRAGGED:
                isOver = thb.contains(mousePos) && isOver;
                break;
            case MouseEvent.MOUSE_PRESSED:
                isPressed = isOver && (e.getButton() == mask);
                break;
            case MouseEvent.MOUSE_RELEASED:
                if (isPressed) {
                    trigger();
                }
                isPressed = false;
                isOver = thb.contains(mousePos);
                break;
        }
        
        if(isOver != oio || isPressed != oip){
            hasSoundPlayed = false;
        }
    }
    
    public boolean hasSoundPlayed(){
        return hasSoundPlayed;
    }
    
    public void soundPlayed(){
        hasSoundPlayed = true;
    }

    public void setTransform(AffineTransform t) {
        at = t;
    }

    public void setTrigger(int mask) {
        this.mask = mask;
    }

    public int getTrigger() {
        return mask;
    }

    public boolean isOver() {
        return isOver;
    }

    public boolean isPressed() {
        return isPressed;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public void setText(String newText) {
        text = newText;
    }

    public String getText() {
        return text;
    }

    public AffineTransform getAffineTransform() {
        return at;
    }

    public Shape getHitbox() {
        return hitbox;
    }

    public void setHitbox(Shape newHitbox) {
        hitbox = newHitbox;
    }
    
    public boolean isTriggered(){
        return isTriggered;
    }

    public Shape getTrasformedHitbox() {
        AffineTransform transform = AffineTransform.getTranslateInstance(x, y);
        transform.concatenate(at);
        return transform.createTransformedShape(hitbox);
    }

    private void trigger() {
        isTriggered = true;
    }
}
