package windsdon.war.client;

import java.awt.AWTException;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputMethodEvent;
import java.awt.event.InputMethodListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import org.lwjgl.util.Timer;

/**
 *
 * @author Windsdon
 */
public class War extends Canvas implements ActionListener, MouseListener, KeyListener, MouseWheelListener, FocusListener, MouseMotionListener, InputMethodListener {

    private int w, h;
    private Dimension size;
    private boolean running;
    private Timer t;
    private double xscale, yscale;
    /**
     *
     */
    private float lastFPStime;
    private float updateFPSinterval = 0.5f;
    private int tfps;
    private int fps;
    /**
     *
     */
    private Font fontFPS = new Font(Font.MONOSPACED, Font.BOLD, 12);
    private Font fontCountdown = new Font(Font.MONOSPACED, Font.PLAIN, 26);
    private Font fontLogoMainScreen = new Font("Stencil Std", Font.PLAIN, 300);
    private Font fontLoginDialog = new Font("Calibri", Font.BOLD, 16);
    private Font fontFlashingText = new Font("Calibri", Font.BOLD, 26);
    /**
     *
     */
    private TextInput textLoginDiagIP;
    private TextInput textLoginDiagName;
    private TextInput textLoginDiagPass;
    /**
     *
     */
    private WarButton buttonLoginDialogLogin;
    /**
     *
     */
    private boolean quality = true;
    private boolean loading;
    /**
     *
     */
    private Color loadingContainerColor = new Color(0xc0c0c0);
    private Color loadingBarColor = new Color(0xa5ff7f);
    private Paint paintDarkGreenGradient;
    private Paint paintLoginScreenBG;
    private Paint paintLoginDialogBoxBG;
    private Paint paintLoginLogo;
    /**
     *
     */
    private TextureLoader loader;
    private SoundLoader sloader;
    private ButtonManager buttons;
    private InputManager inputs;
    private boolean registerInputs;
    /**
     *
     */
    private Image imageLoginBG;
    /**
     *
     */
    private TrayIcon ti;
    /**
     *
     */
    private int showingScreen;
    private boolean changedScreen;

    private static abstract class Screens {

        public static int screenLogin = 1;

        public static abstract class LoginMetrics {

            public static int loginDialogBoxWidth = 340,
                    loginDialogBoxHeight = 300,
                    loginDialogBoxX = 920,
                    loginDialogBoxY = 395,
                    loginDialogBoxPadding = 24;
        }
    };

    public War(int w, int h, JFrame frame) {
        this.w = 1280;
        this.h = 720;
        xscale = w / this.w;
        yscale = h / this.h;

        size = new Dimension(w - 10, h - 10);


        frame.addKeyListener(this);
    }

    public void resize() {
        System.out.println("resize");

        setPreferredSize(size);
        setMinimumSize(size);
        setMaximumSize(size);
    }

    public void start() {
        t = new Timer();
        running = true;
        loading = true;
        loader = new TextureLoader(0, this);
        sloader = new SoundLoader();

        loader.addLoadingList("res/textures.rls");
        sloader.addLoadingList("res/sounds.rls");
        sloader.start();

        addMouseListener(this);
        addKeyListener(this);
        addMouseWheelListener(this);
        addFocusListener(this);
        addMouseMotionListener(this);

        registerInputs = true;

        inputs = new InputManager();

        requestFocus();

        buttons = new ButtonManager();

        buttonLoginDialogLogin = new WarButton(
                new Rectangle2D.Float(0, 0, Screens.LoginMetrics.loginDialogBoxWidth - 2 * Screens.LoginMetrics.loginDialogBoxPadding, 50),
                "Login",
                Screens.LoginMetrics.loginDialogBoxPadding + Screens.LoginMetrics.loginDialogBoxX,
                214 + Screens.LoginMetrics.loginDialogBoxY);
        buttons.add(buttonLoginDialogLogin);

        paintDarkGreenGradient = new GradientPaint(0, 0, Color.BLACK, 0, h - 1, new Color(0x004406), false);
        paintLoginScreenBG = new GradientPaint(0, 0, new Color(0x00aa00), 0, h - 1, new Color(0x004400), false); //new GradientPaint(0, 0, new Color(0xeaf1e5), 0, h - 1, new Color(0xd5e1cc), false);
        paintLoginDialogBoxBG = new GradientPaint(0, 0, new Color(0x5f8029), 0, Screens.LoginMetrics.loginDialogBoxHeight, new Color(0x799941), false);
        paintLoginLogo = new GradientPaint(0, 0, new Color(0xc20000), 0, 226, new Color(0x8f0000));

        showingScreen = Screens.screenLogin;
        changedScreen = true;

        ti = new TrayIcon(new ImageIcon(WarGame.class.getClassLoader().getResource("res/trayicon.png")).getImage(), "War Online");
        try {
            SystemTray.getSystemTray().add(ti);
        } catch (AWTException ex) {
        }

        while (running) {
            update();
        }
    }

    public void update() {
        BufferStrategy bs = getBufferStrategy();

        while (bs == null) {
            createBufferStrategy(3);
            bs = getBufferStrategy();
        }

        Graphics2D g = (Graphics2D) bs.getDrawGraphics();
        g.setColor(Color.black);
        g.fillRect(0, 0, w, h);

        RenderingHints rh = new RenderingHints(null);
        if (quality) {
            rh.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            rh.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        } else {
            rh.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            rh.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
        }
        g.setRenderingHints(rh);

        Timer.tick();

        if (registerInputs) {
            registerInputs = false;
            textLoginDiagIP = new TextInput(true, 24 + Screens.LoginMetrics.loginDialogBoxX, 50 + Screens.LoginMetrics.loginDialogBoxY, 27, g);
            inputs.add(textLoginDiagIP);
            textLoginDiagName = new TextInput(true, 24 + Screens.LoginMetrics.loginDialogBoxX, 110 + Screens.LoginMetrics.loginDialogBoxY, 27, g);
            inputs.add(textLoginDiagName);
            textLoginDiagPass = new TextInput(true, 24 + Screens.LoginMetrics.loginDialogBoxX, 170 + Screens.LoginMetrics.loginDialogBoxY, 27, g);
            textLoginDiagPass.setPass(true);
            inputs.add(textLoginDiagPass);

        }

        tfps++;
        if (t.getTime() - lastFPStime >= updateFPSinterval) {
            fps = (int) (tfps / updateFPSinterval);
            tfps = 0;
            lastFPStime = t.getTime();
        }

        render(g);

        showFPS(g);

        g.dispose();
        bs.show();
    }

    private void showFPS(Graphics2D g) {
        g.setTransform(new AffineTransform());
        g.setColor(Color.black);
        textPosition("FPS: " + Integer.toString(fps), 6, 1, "top_left", fontFPS, g);
        g.setColor(Color.yellow);
        textPosition("FPS: " + Integer.toString(fps), 5, 0, "top_left", fontFPS, g);

    }

    public static void textPosition(String text, float px, float py, String align, Font f, Graphics2D g) {
        FontMetrics fm = g.getFontMetrics(f);
        g.setFont(f);
        /*g.setColor(Color.yellow);
         g.fillOval((int) px - 2, (int) py - 2, 4, 4);*/
        switch (align) {
            case "top_left":
                g.drawString(text, px, py + fm.getAscent());
                break;
            case "top_right":
                g.drawString(text, px - fm.stringWidth(text), py + fm.getAscent());
                break;
            case "bottom_left":
                g.drawString(text, px, py - fm.getDescent());
                break;
            case "bottom_right":
                g.drawString(text, px - fm.stringWidth(text), py - fm.getDescent());
                break;
            case "center_center":
                //System.out.println(py + fm.getHeight() / 2);
                g.drawString(text, px - fm.stringWidth(text) / 2, py + fm.getMaxAscent() / 4);
                break;
            case "center_left":
                g.drawString(text, px, py + fm.getMaxAscent() / 4);
                break;
        }
    }

    private void render(Graphics2D g) {
        if (loading == true) {

            if (!loader.isStarted()) {
                loader.start();
            }

            g.setPaint(paintDarkGreenGradient);
            g.fillRect(0, 0, w, h);

            double c = (loader.getCompletion() + sloader.getCompletion()) / 2;

            if (c == 1) {
                loading = false;
                return;
            }

            RoundRectangle2D loadingContainer = new RoundRectangle2D.Double(w / 2 - 150, h / 2 - 25, 300, 50, 20, 20);
            Rectangle2D loadingBar = new Rectangle2D.Double(w / 2 - 140, h / 2 - 15, 280 * c, 30);

            g.setClip(loadingBar);
            g.setColor(loadingBarColor);
            for (double i = w / 2 - 140; i < w / 2 + 140; i += 28.88) {
                g.fill(new Rectangle2D.Double(i, h / 2 - 15, 25, 30));
            }

            g.setClip(null);
            g.setColor(loadingContainerColor);
            g.setStroke(new BasicStroke(5));
            g.draw(loadingContainer);

            g.setTransform(new AffineTransform());
            return;
        }

        if (showingScreen == Screens.screenLogin) {
            if (changedScreen) {
                changeToLoginScreen();
            }
            renderLoginScreen(g);
        }


    }

    private void renderLoginScreen(Graphics2D g) {
        if (imageLoginBG == null) {
            imageLoginBG = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            Graphics2D bgcg = (Graphics2D) imageLoginBG.getGraphics();

            bgcg.drawImage(loader.textures.get("TEX_LOGIN_BG"), 0, 0, null);

            bgcg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f));

            bgcg.setPaint(paintLoginScreenBG);
            bgcg.fillRect(0, 0, w, h);
            bgcg.dispose();
        }



        g.drawImage(imageLoginBG, 0, 0, null);



        //g.setColor(Color.black);
        g.setPaint(paintLoginLogo);
        g.setTransform(AffineTransform.getTranslateInstance(20, 20));
        textPosition("WAR", 0, 0, "top_left", fontLogoMainScreen, g);

        g.setPaint(paintLoginDialogBoxBG);
        g.setTransform(AffineTransform.getTranslateInstance(Screens.LoginMetrics.loginDialogBoxX, Screens.LoginMetrics.loginDialogBoxY));
        g.fillRoundRect(0, 0, Screens.LoginMetrics.loginDialogBoxWidth, Screens.LoginMetrics.loginDialogBoxHeight, 20, 20);
        g.setColor(Color.white);
        g.drawRoundRect(0, 0, Screens.LoginMetrics.loginDialogBoxWidth, Screens.LoginMetrics.loginDialogBoxHeight, 20, 20);
        g.setTransform(new AffineTransform());

        g.setColor(Color.white);
        textPosition("IP:", 24 + Screens.LoginMetrics.loginDialogBoxX, 24 + Screens.LoginMetrics.loginDialogBoxY, "top_left", fontLoginDialog, g);
        textLoginDiagIP.render(g, t.getTime());

        g.setColor(Color.white);
        textPosition("Nome:", 24 + Screens.LoginMetrics.loginDialogBoxX, 84 + Screens.LoginMetrics.loginDialogBoxY, "top_left", fontLoginDialog, g);
        textLoginDiagName.render(g, t.getTime());

        g.setColor(Color.white);
        textPosition("Senha:", 24 + Screens.LoginMetrics.loginDialogBoxX, 144 + Screens.LoginMetrics.loginDialogBoxY, "top_left", fontLoginDialog, g);
        textLoginDiagPass.render(g, t.getTime());

        g.setFont(fontLoginDialog);
        if (buttonLoginDialogLogin.isPressed()) {
            g.setColor(new Color(0x555555));
            renderButton(buttonLoginDialogLogin, g, Color.white);
        } else if (buttonLoginDialogLogin.isOver()) {
            if (!buttonLoginDialogLogin.hasSoundPlayed()) {
                buttonLoginDialogLogin.soundPlayed();
                sloader.play("SND_BUTTON_ROLLOVER");
            }
            g.setColor(new Color(0x444444));
            renderButton(buttonLoginDialogLogin, g, Color.white);
        } else {
            g.setColor(Color.darkGray);
            renderButton(buttonLoginDialogLogin, g, Color.white);
        }

        FontMetrics m = g.getFontMetrics(fontFlashingText);
        String flashingText = "Online!";
        BufferedImage flashText = new BufferedImage(m.stringWidth(flashingText), m.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D ftg = (Graphics2D) flashText.getGraphics();
        ftg.addRenderingHints(new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON));
        ftg.setColor(Color.yellow);

        textPosition(flashingText, 0, 0, "top_left", fontFlashingText, ftg);
        ftg.dispose();

        AffineTransform cd = new AffineTransform();
        cd.translate(700, 250);
        cd.rotate(-Math.PI / 4, 0, 0);
        cd.scale(Math.abs(Math.sin(Math.PI * t.getTime() / 0.5)) / 2 + 1, Math.abs(Math.sin(Math.PI * t.getTime() / 0.5)) / 2 + 1);

        g.setTransform(cd);
        g.drawImage(flashText, -flashText.getWidth() / 2, -flashText.getHeight() / 2, null);


    }

    private void changeToLoginScreen() {
        inputs.deactivateAll();
        buttons.deactivateAll();
        textLoginDiagIP.setActive(true);
        textLoginDiagName.setActive(true);
        textLoginDiagPass.setActive(true);
        buttonLoginDialogLogin.setActive(true);

        sloader.stopAll();
        //sloader.play("SND_LOGIN_BG");
        //sloader.play("SND_BUTTON_ROLLOVER");

        changedScreen = false;
    }

    private void renderButton(WarButton b, Graphics2D g, Color textColor) {
        g.fill(b.getTrasformedHitbox());
        if (b.getText() != null) {
            Rectangle2D c = b.getTrasformedHitbox().getBounds2D();
            g.setColor(textColor);
            textPosition(b.getText(), (float) c.getCenterX(), (float) c.getCenterY(), "center_center", (Font) g.getFont(), g);
        }
    }

    private void renderTexturedButton(WarButton b, Graphics2D g, Image i) {
    }

    private String makeClockString(float time, int positions, int precision) {
        //int[] digits = new int[positions + precision > 0 ? 1 : 0];

        int ho = (int) (time / 3600);
        time -= ho * 3600;
        int mi = (int) (time / 60);
        time -= mi * 60;
        int se = (int) time;
        time -= se;
        int ml = (int) (time * Math.pow(10, precision));

        int[] digits = {ho, mi, se, ml};

        String r = "";
        for (int i = 0; i < digits.length; i++) {
            int j = digits[i];
            if (precision > 0 && i == digits.length - 1) {
                r = r.concat(".").concat(zeroFill(j, precision));
            } else {
                if (i != 0) {
                    r = r.concat(":").concat(zeroFill(j, 2));
                } else {
                    r = r.concat(zeroFill(j, 2));
                }
            }
        }
        return r;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        //System.out.println(e.paramString());
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (buttons == null) {
            System.out.println("Not yet initialized.");
            return;
        }
        if (inputs != null) {
            inputs.updateMouse(e);
        }
        buttons.update(e);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (buttons == null) {
            System.out.println("Not yet initialized.");
            return;
        }
        if (inputs != null) {
            inputs.updateMouse(e);
        }
        buttons.update(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (buttons == null) {
            System.out.println("Not yet initialized.");
            return;
        }
        if (inputs != null) {
            inputs.updateMouse(e);
        }
        buttons.update(e);
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        //System.out.println(e.paramString());
    }

    @Override
    public void mouseExited(MouseEvent e) {
        //System.out.println(e.paramString());
    }

    @Override
    public void keyTyped(KeyEvent e) {
        if (inputs != null) {
            inputs.updateKeyboard(e);
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (inputs != null) {
            inputs.updateKeyboard(e);
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (inputs != null) {
            inputs.updateKeyboard(e);
        }
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        //System.out.println(e.paramString());
    }

    @Override
    public void focusGained(FocusEvent e) {
        //System.out.println(e.paramString());
    }

    @Override
    public void focusLost(FocusEvent e) {
        //System.out.println(e.paramString());
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (buttons == null) {
            System.out.println("Not yet initialized.");
            return;
        }
        buttons.update(e);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (buttons == null) {
            System.out.println("Not yet initialized.");
            return;
        }
        buttons.update(e);
    }

    private String zeroFill(int j, int i) {
        String result = Integer.toString(j);
        while (result.length() < i) {
            result = "0".concat(result);
        }

        return result;
    }

    @Override
    public void inputMethodTextChanged(InputMethodEvent event) {
        System.out.println(event);
    }

    @Override
    public void caretPositionChanged(InputMethodEvent event) {
        System.out.println(event);
    }
}
