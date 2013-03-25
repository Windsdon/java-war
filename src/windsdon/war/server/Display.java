package windsdon.war.server;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.ArrayList;
import javax.swing.JPanel;

/**
 *
 * @author Windsdon
 */
public class Display extends JPanel implements MouseWheelListener {

    private Color background = new Color(0x333333);
    private RenderingHints rh;
    private ArrayList<String> lines;
    private boolean autoScroll;
    private int lineOffset;
    private TextFormater tf;
    private int marginLeft = 10,
            marginTop = 10;
    //private int lastLineCount = 0;
    //private int lastLineHeight = 0;
    private int lastLinePos = 0;
    private float lastLineAscent;

    public Display() {
        rh = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        rh.add(new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY));
        lines = new ArrayList<>();
        autoScroll = true;
        lineOffset = 0;
        addMouseWheelListener(this);
        lines.add("§2§bConsole iniciado.");

        final Display theDisplay = this;
        new Thread() {
            @Override
            public void run() {
                while (true) {
                    theDisplay.updateScroll();
                    //theDisplay.repaint();
                    try {
                        synchronized (this) {
                            wait(200);
                        }
                    } catch (InterruptedException ex) {
                    }
                }
            }
        }.start();
    }

    @Override
    public void paint(Graphics ctx) {
        Graphics2D g = (Graphics2D) ctx;
        g.setRenderingHints(rh);
        g.setColor(background);
        g.fillRect(0, 0, getWidth(), getHeight());
        ////////////////////////////////////////////////////////
        /*g.setColor(Color.yellow);
         g.setFont(new Font("Lucida Console", Font.BOLD, 25));
         g.drawString("Testando", 50, 50);*/
        ////////////////////////////////////////////////////////
        int ypos = 0;
        //int lineCount = 0;

        g.setTransform(AffineTransform.getTranslateInstance(0, -lineOffset));

        lineDrawLoop:
        for (int i = 0; i < lines.size(); i++) {
            String string = lines.get(i);
            AttributedCharacterIterator paragraph = getAttributedString(string).getIterator();
            int pstart = paragraph.getBeginIndex();
            int pend = paragraph.getEndIndex();
            LineBreakMeasurer lineMesure = new LineBreakMeasurer(paragraph, new FontRenderContext(null, true, false));
            lineMesure.setPosition(pstart);

            while (lineMesure.getPosition() < pend) {
                TextLayout layout = lineMesure.nextLayout(getWidth() - 2 * marginLeft);

                ypos += layout.getAscent();

                layout.draw(g, marginLeft, ypos + marginTop);

                lastLineAscent = layout.getAscent() + layout.getDescent() + layout.getLeading();

                ypos += layout.getDescent() + layout.getLeading();

            }

        }

        lastLinePos = (int) (ypos + lastLineAscent);

        //lastLineCount = lineCount;

        ////////////////////////////////////////////////////////
        g.setTransform(new AffineTransform());
        getBorder().paintBorder(this, g, 0, 0, getWidth(), getHeight());
        g.dispose();
    }

    public void autoScroll() {
        repaint();
        lineOffset = lastLinePos - getHeight() + 2 * marginTop;
        repaint();
    }

    public void addLine(String text) {
        lines.add(text);
        if (autoScroll) {
            autoScroll();
        } else {
            repaint();
        }
    }

    private void updateScroll() {
        if (lastLinePos - lineOffset < getHeight()) {
            lineOffset = lastLinePos - getHeight() + marginTop;
        } else if (lineOffset < 0) {
            if (lastLinePos < getHeight()) {
                lineOffset = lastLinePos - getHeight() + marginTop;
            } else {
                lineOffset = 0;
            }
        }
        repaint();
    }

    private AttributedString getAttributedString(String str) {
        return TextFormater.get(str, Color.white, new Font("Lucida console", Font.PLAIN, 14));
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        lineOffset += e.getScrollAmount() * e.getPreciseWheelRotation() * 10;
        updateScroll();
        repaint();
    }
}
