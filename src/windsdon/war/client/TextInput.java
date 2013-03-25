package windsdon.war.client;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import org.lwjgl.util.Timer;

/**
 *
 * @author Windsdon
 */
public class TextInput {

    private boolean isActive;
    private boolean isEditable;
    private boolean hasFocus;
    private String text;
    private int offset;
    private int cursorPos;
    private int x, y;
    private AffineTransform at;
    private Timer t;
    private float lastCursorFlash;
    private float cursorFlashFrequency = 1;
    private boolean cursorShowing;
    private double charWidth;
    private float charSpace;
    private double w, h;
    private boolean holdCursorOn;
    //
    private Shape hitbox;
    private Shape editingShape;
    private Rectangle2D cutShape;
    private Stroke stroke;
    private Color colorBG;
    private Color colorText;
    private Color colorBorder;
    private FontMetrics m;
    private Graphics2D ctx;
    private Font font;
    private int chars;
    //
    public static Stroke DEFAULT_STROKE = new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    public static Color DEFAULT_BG = Color.white;
    public static Color DEFAULT_BORDER = Color.darkGray;
    public static Color DEFAULT_TEXT = Color.black;
    public static Font DEFAULT_FONT = new Font(Font.MONOSPACED, Font.PLAIN, 16);
    private double paddingX, paddingY;
    private boolean isPassword;

    public TextInput(boolean editable, int x, int y, int chars, Graphics2D g) {
        paddingX = 10;
        paddingY = 0;

        ctx = (Graphics2D) g;
        m = ctx.getFontMetrics(DEFAULT_FONT);
        charWidth = m.getMaxAdvance() / 4;
        charSpace = 4;//m.getMaxAdvance() / 4;
        w = charWidth * chars + 2 * paddingX + (chars - 1) * charSpace;
        h = m.getHeight() + 2 * paddingY;

        this.chars = chars;

        text = "";

        Rectangle2D hb = new Rectangle2D.Double(0, 0, w, h);

        isEditable = editable;
        hitbox = hb;
        editingShape = hb;
        cutShape = new Rectangle2D.Double(paddingX, paddingY, w - paddingX, h - paddingY);
        this.x = x;
        this.y = y;
        colorBG = DEFAULT_BG;
        colorBorder = DEFAULT_BORDER;
        colorText = DEFAULT_TEXT;
        stroke = DEFAULT_STROKE;

        font = DEFAULT_FONT;

        at = new AffineTransform();

        t = new Timer();
    }

    public void updateMouse(MouseEvent e) {
        if (!isActive || !isEditable) {
            return;
        }

        Point2D mousePos = e.getPoint();
        AffineTransform finalTransform = AffineTransform.getTranslateInstance(x, y);
        finalTransform.concatenate(at);
        switch (e.getID()) {
            case MouseEvent.MOUSE_PRESSED:
                hasFocus = finalTransform.createTransformedShape(hitbox).contains(mousePos);
                holdCursorOn = hasFocus;
                break;
        }
    }

    public void updateKeyboard(KeyEvent e) {
        if (!isActive || !isEditable) {
            return;
        }

        if (!hasFocus) {
            return;
        }

        boolean cursorMoved = false;

        switch (e.getID()) {
            case KeyEvent.KEY_PRESSED:
                //case KeyEvent.KEY_TYPED:
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_BACK_SPACE:
                        if (cursorPos > 0) {
                            String before,
                                    after;
                            if (cursorPos > 1) {
                                before = text.substring(0, cursorPos - 1);
                            } else {
                                before = "";
                            }
                            if (cursorPos < text.length()) {
                                after = text.substring(cursorPos, text.length());
                            } else {
                                after = "";
                            }
                            text = before.concat(after);
                            //text = text.substring(0, cursorPos - 1).concat(text.substring(cursorPos, text.length() - 1));
                            cursorPos--;
                            cursorMoved = true;
                        }
                        break;

                    case KeyEvent.VK_DELETE:
                        if (cursorPos < text.length()) {
                            text = text.substring(0, cursorPos).concat(text.substring(cursorPos + 1, text.length()));
                        }
                        break;
                    case KeyEvent.KEY_LOCATION_LEFT:
                    case KeyEvent.VK_LEFT:
                        if (cursorPos > 0) {
                            cursorPos--;
                            cursorMoved = true;
                        }
                        break;
                    case KeyEvent.KEY_LOCATION_RIGHT:
                    case KeyEvent.VK_RIGHT:
                        if (cursorPos < text.length()) {
                            cursorPos++;
                            cursorMoved = true;
                        }
                        break;
                    default:
                        if (!font.canDisplay(e.getKeyChar())) {
                            break;
                        }
                        String before,
                         after;
                        if (cursorPos > 0) {
                            before = text.substring(0, cursorPos);
                        } else {
                            before = "";
                        }
                        if (cursorPos < text.length()) {
                            after = text.substring(cursorPos, text.length());
                        } else {
                            after = "";
                        }
                        text = before.concat(Character.toString(e.getKeyChar())).concat(after);
                        cursorPos++;
                        cursorMoved = true;
                }
                break;
        }

        if (cursorMoved) {
            holdCursorOn = true;
            if (cursorPos - offset > chars) {
                offset += cursorPos - offset - chars;
            }
            if (cursorPos < offset) {
                offset -= offset - cursorPos;
            }
        }


    }

    public void render(Graphics2D g, float time) {
        /*AffineTransform finalTransformation = new AffineTransform(at);
         finalTransformation.concatenate(AffineTransform.getTranslateInstance(x, y));*/

        AffineTransform previous = g.getTransform();
        AffineTransform newTransform = AffineTransform.getTranslateInstance(x, y);
        newTransform.concatenate(previous);

        g.setTransform(newTransform);

        g.setColor(colorBG);

        g.fill(editingShape);

        g.setStroke(stroke);
        g.setColor(colorBorder);

        g.draw(editingShape);

        g.setClip(cutShape);
        g.setColor(colorText);

        String v = getVisibleText();

        for (int i = 0; i < v.length(); i++) {
            char ch;
            if (isPassword) {
                 ch = '*';
            } else {
                ch = v.charAt(i);
            }
            
            War.textPosition(Character.toString(ch), (float) (cutShape.getMinX() + i * (charWidth + charSpace)), (float) (cutShape.getCenterY()), "center_left", font, g);
        }

        if (hasFocus) {
            if (holdCursorOn) {
                holdCursorOn = false;
                lastCursorFlash = time;
                cursorShowing = true;
            } else if (time - lastCursorFlash >= cursorFlashFrequency) {
                cursorShowing = !cursorShowing;
                lastCursorFlash = time;
            }
        }

        if (cursorShowing && hasFocus) {
            g.setColor(Color.black);
            int cursorDisplayX = (int) (cutShape.getMinX() + (cursorPos - offset) * (charWidth + charSpace) + charSpace / 2);
            g.drawLine(cursorDisplayX, (int) cutShape.getMinY(), cursorDisplayX, (int) cutShape.getMaxY());
        }

        g.setClip(null);

        /*g.setColor(Color.red);
         g.draw(hitbox);*/

        g.setTransform(previous);
    }

    public String getVisibleText() {
        return text.substring(offset, Math.min(offset + chars, text.length()));
    }

    public boolean hasFocus() {
        return false;
    }

    public void getFocus() {
        hasFocus = true;
        holdCursorOn = true;
    }

    public void setActive(boolean a) {
        isActive = a;
    }

    public boolean isActive() {
        return isActive;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getCursorPos() {
        return 0;
    }

    public void setCursorPos() {
    }

    public String getSelection() {
        return null;
    }

    public void setSelection(int start, int end) {
    }

    public void setTrasformation(AffineTransform t) {
        at = t;
    }

    public void setHitbox(Shape s) {
    }

    public Shape getHitbox() {
        return null;
    }

    public void setEditingBox(Shape s) {
    }

    public Shape getEditingBox() {
        return null;
    }

    public void setColor(Color c) {
    }

    public Color getColor() {
        return null;
    }

    public void setStroke(Stroke s) {
    }

    public Stroke getStroke() {
        return null;
    }

    public void setStrokeColor(Color c) {
    }

    public Color getStrokeColor() {
        return null;
    }

    public void setTextColor(Color c) {
    }

    public Color getTextColor() {
        return null;
    }

    public void setFont(Font f) {
    }

    public Font getFont() {
        return null;
    }

    public void setAlpha(AlphaComposite c) {
    }

    public void setOffset(int of) {
        offset = of;
    }

    public int getOffset() {
        return offset;
    }

    /**
     * ATENÇÃO: vai mudar o editingBox e o hitBox!
     *
     * @param l
     */
    public void setCharLength(int l) {
    }

    void setPass(boolean b) {
        isPassword = b;
    }
}
