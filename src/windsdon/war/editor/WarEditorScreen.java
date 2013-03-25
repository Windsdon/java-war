/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package windsdon.war.editor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.font.TextAttribute;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JPanel;

/**
 *
 * @author Windsdon
 */
public class WarEditorScreen extends JPanel implements MouseListener, MouseWheelListener {

    private Image theImage;
    private int usableWidth, usableHeight;
    private MapInfo mapInfo;
    public static String TOOL_ADD = "add";
    public static String TOOL_SELECT = "select";
    private String toolMode;
    private int editingTerritory = -1;
    private ArrayList<Territory> unassignedTerritories;
    private Point2D mousePos;
    private int selectedTerritoryA = -1;
    private int selectedTerritoryB = -1;
    private boolean connecting = false;
    private Point visualPoint1;
    private Point visualPoint2;

    public WarEditorScreen() {
        addMouseListener(this);
        toolMode = TOOL_ADD;
    }

    @Override
    public void paint(Graphics ctx) {
        Graphics2D g = (Graphics2D) ctx;

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());

        if (theImage != null) {
            if (((double) theImage.getWidth(null)) / theImage.getHeight(null) >= ((double) getWidth()) / getHeight()) {
                usableWidth = getWidth();
                usableHeight = theImage.getHeight(null) * getWidth() / theImage.getWidth(null);

            } else {
                usableWidth = theImage.getWidth(null) * getHeight() / theImage.getHeight(null);
                usableHeight = getHeight();
            }

            if (usableWidth > getWidth() || usableHeight > getHeight()) {
                System.out.println(usableWidth + ";" + usableHeight);
            }

            g.drawImage(theImage, 0, 0, usableWidth, usableHeight, null);

            g.setColor(Color.red);
            g.setStroke(new BasicStroke(3));
            g.drawRect(0, 0, usableWidth, usableHeight);

            g.setStroke(new BasicStroke(3));

            for (int i = 0; i < mapInfo.regions.size(); i++) {
                Region region = mapInfo.regions.get(i);


                for (int j = 0; j < region.regionTerritories.size(); j++) {
                    Integer t = region.regionTerritories.get(j);

                    VertexData v = getTerritoryById(t).vertices;
                    int[] xPoints = new int[v.vertexPos.size()];
                    int[] yPoints = new int[v.vertexPos.size()];

                    for (int k = 0; k < v.vertexPos.size(); k++) {
                        double[] vpos = v.vertexPos.get(k);
                        xPoints[k] = (int) (vpos[0] * usableWidth);
                        yPoints[k] = (int) (vpos[1] * usableHeight);
                    }

                    Polygon tp = new Polygon(xPoints, yPoints, xPoints.length);

                    g.setColor(region.regionColor);
                    g.fill(tp);

                    AttributedString at = new AttributedString(mapInfo.territories.get(t).territoryName);
                    at.addAttribute(TextAttribute.FONT, new Font(Font.SANS_SERIF, Font.BOLD, 20));
                    at.addAttribute(TextAttribute.BACKGROUND, Color.black);
                    at.addAttribute(TextAttribute.FOREGROUND, Color.white);

                    g.drawString(at.getIterator(), (float) tp.getBounds2D().getCenterX(), (float) tp.getBounds2D().getCenterY());

                    if (mapInfo.territories.get(t).territoryNumber == selectedTerritoryA
                            || mapInfo.territories.get(t).territoryNumber == selectedTerritoryB) {
                        g.setColor(Color.white);
                        g.draw(tp);
                    } else {
                        g.setColor(Color.black);
                        g.draw(tp);
                    }

                }
            }
            for (int i = 0; i < unassignedTerritories.size(); i++) {
                Territory territory = unassignedTerritories.get(i);

                if (editingTerritory == territory.territoryNumber) {
                    g.setColor(Color.red);
                    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
                    g.fillOval((int) (territory.vertices.vertexPos.get(0)[0] * usableWidth),
                            (int) (territory.vertices.vertexPos.get(0)[1] * usableHeight), 5, 5);
                    g.setColor(Color.white);
                    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
                } else {
                    g.setColor(Color.white);
                    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
                }

                g.fillPolygon(vertexToPolygon(territory.vertices.vertexPos));

                if (territory.territoryNumber == selectedTerritoryA) {
                    g.setColor(Color.red);
                    g.draw(vertexToPolygon(territory.vertices.vertexPos));
                } else {
                    g.setColor(Color.black);
                    g.draw(vertexToPolygon(territory.vertices.vertexPos));
                }
            }

            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));

            g.setColor(Color.pink);
            float[] dash = {10};
            g.setStroke(new BasicStroke(5, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, dash, 0));
            for (int i = 0; i < mapInfo.connections.size(); i++) {
                int[] is = mapInfo.connections.get(i);
                Territory t1 = getTerritoryById(is[0]);
                Territory t2 = getTerritoryById(is[1]);
                Polygon p1 = vertexToPolygon(t1.vertices.vertexPos);
                Polygon p2 = vertexToPolygon(t2.vertices.vertexPos);
                g.drawLine((int) p1.getBounds2D().getCenterX(), (int) p1.getBounds2D().getCenterY(),
                        (int) p2.getBounds2D().getCenterX(), (int) p2.getBounds2D().getCenterY());

            }

            for (int i = 0; i < mapInfo.visualConnections.size(); i++) {
                double[][] dses = mapInfo.visualConnections.get(i);
                drawVisualConnection(g, dses);
            }

            if (connecting) {
                g.setColor(Color.red);
                g.drawString("Connecting", 10, usableHeight - 10);
            }

        }

        g.dispose();
    }

    public void setImage(File f) {
        mapInfo = new MapInfo();
        unassignedTerritories = new ArrayList<>();
        try {
            theImage = ImageIO.read(f);
        } catch (IOException ex) {
        }

        mapInfo.aspectRatio = ((double) theImage.getWidth(null)) / theImage.getHeight(null);

        repaint();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    private Polygon vertexToPolygon(ArrayList<double[]> l) {
        Polygon m = new Polygon();

        for (int i = 0; i < l.size(); i++) {
            double[] ds = l.get(i);
            m.addPoint((int) (ds[0] * usableWidth), (int) (ds[1] * usableHeight));
        }

        return m;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (theImage == null) {
            return;
        }
        if (e.getButton() == MouseEvent.BUTTON2) {
            if (visualPoint1 == null) {
                visualPoint1 = e.getPoint();
            } else {
                visualPoint2 = e.getPoint();
                makeVisualConnection();
            }
            repaint();
            return;
        }
        if (toolMode.equals(TOOL_ADD)) {
            Territory editing;
            selectedTerritoryA = -1;
            selectedTerritoryB = -1;
            if (editingTerritory == -1) {
                editing = new Territory();
                unassignedTerritories.add(editing);
                editingTerritory = editing.territoryNumber;
                mapInfo.territories.add(editing);
            } else {
                editing = getTerritoryById(editingTerritory);
            }
            if (e.getButton() == MouseEvent.BUTTON3) {
                ArrayList<double[]> vertices = new ArrayList<>();

                for (int i = 0; i < unassignedTerritories.size(); i++) {
                    Territory territory = unassignedTerritories.get(i);
                    vertices.addAll(territory.vertices.vertexPos);
                }

                for (int i = 0; i < mapInfo.territories.size(); i++) {
                    Territory territory = mapInfo.territories.get(i);
                    vertices.addAll(territory.vertices.vertexPos);
                }

//                for (int i = 0; i < mapInfo.vertices.size(); i++) {
//                    VertexData vertexData = mapInfo.vertices.get(i);
//                    vertices.addAll(vertexData.vertexPos);
//                }

                for (int i = 0; i < vertices.size(); i++) {
                    double[] ds = vertices.get(i);
                    if (e.getPoint().distance(new Point((int) (ds[0] * usableWidth), (int) (ds[1] * usableHeight))) <= 5) {
//                        if ((editing.vertices.vertexPos.get(0)[0] - ds[0]) * usableWidth <= 5
//                                && (editing.vertices.vertexPos.get(0)[1] - ds[1]) * usableHeight <= 5) {
//                            endTerritoryEditing();
//                        } else {
//                            editing.vertices.vertexPos.add(ds);
//                        }
                        editing.vertices.vertexPos.add(ds);
                        editing.vertices.vertexCount = editing.vertices.vertexPos.size();
                        repaint();
                        return;
                    }
                }
            } else {
                double[] pos = {e.getX() / (double) usableWidth, e.getY() / (double) usableHeight};
                editing.vertices.vertexPos.add(pos);
                editing.vertices.vertexCount = editing.vertices.vertexPos.size();
            }
        } else if (toolMode.equals(TOOL_SELECT)) {
            ArrayList<Territory> allTerritories = new ArrayList<>(unassignedTerritories);
            allTerritories.addAll(mapInfo.territories);

            for (int i = 0; i < allTerritories.size(); i++) {
                Territory territory = allTerritories.get(i);
                if (vertexToPolygon(territory.vertices.vertexPos).contains(e.getPoint())) {
                    if (selectedTerritoryA == -1) {
                        selectedTerritoryA = territory.territoryNumber;
                        repaint();
                        return;
                    } else {
                        if (connecting) {
                            if (territory.territoryNumber != selectedTerritoryA) {
                                selectedTerritoryB = territory.territoryNumber;
                                connectTerritories();
                            } else {
                                connecting = false;
                            }
                        } else {
                            if (territory.territoryNumber != selectedTerritoryA) {
                                selectedTerritoryA = territory.territoryNumber;
                                repaint();
                                return;
                            } else {
                                selectedTerritoryA = -1;
                                repaint();
                                return;
                            }
                        }
                    }
                }
            }
            selectedTerritoryA = -1;
        }
        repaint();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void mouseExited(MouseEvent e) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    void updateRegion(Object k, Color c, String n, String v) {
        Region r = (Region) k;
        r.regionColor = c;
        r.regionName = n;
        r.regionNumber = Integer.parseInt(v);
        repaint();
    }

    void setClickMode(String mode) {
        if (!mode.equals(toolMode)) {
            if (toolMode.equals(TOOL_ADD) && editingTerritory != -1) {
                endTerritoryEditing();
            }
        }
        toolMode = mode;
    }

    public void endTerritoryEditing() {
        if (editingTerritory != -1) {
            selectedTerritoryA = getTerritoryById(editingTerritory).territoryNumber;
            //mapInfo.vertices.add(selectedTerritoryA, getTerritoryById(editingTerritory).vertices);
            editingTerritory = -1;
            repaint();
        }
    }

    private void connectTerritories() {
        if (connecting) {
            if (selectedTerritoryA != -1 && selectedTerritoryB != -1) {
                for (int i = 0; i < mapInfo.connections.size(); i++) {
                    int[] is = mapInfo.connections.get(i);
                    if ((is[0] == selectedTerritoryA && is[1] == selectedTerritoryB)
                            || (is[0] == selectedTerritoryB && is[1] == selectedTerritoryA)) {
                        selectedTerritoryA = -1;
                        selectedTerritoryB = -1;
                        return;
                    }
                }
                int[] e = {Math.min(selectedTerritoryA, selectedTerritoryB), Math.max(selectedTerritoryA, selectedTerritoryB)};
                mapInfo.connections.add(e);
                getTerritoryById(selectedTerritoryA).territoryBorderCount++;
                getTerritoryById(selectedTerritoryB).territoryBorderCount++;
                selectedTerritoryA = -1;
                selectedTerritoryB = -1;
                repaint();
            }
        }
    }

    public Object[] getRegions() {
        Region[] t = new Region[mapInfo.regions.size()];
        for (int i = 0; i < mapInfo.regions.size(); i++) {
            Region region = mapInfo.regions.get(i);
            t[i] = region;
        }
        return t;
    }

    void addRegion(String text, String text0, Color background) {
        Region r = new Region();
        r.regionColor = background;
        r.regionName = text;
        r.regionValue = Integer.parseInt(text0);
        mapInfo.regions.add(r);
    }

    void linkTerritory(String text, Region k) {
        Territory t = getTerritoryById(selectedTerritoryA);
        if (t != null) {
            t.territoryName = text;
            unassignedTerritories.remove(t);
            for (int i = 0; i < mapInfo.regions.size(); i++) {
                Region r = mapInfo.regions.get(i);
                for (int j = 0; j < r.regionTerritories.size(); j++) {
                    Integer m = r.regionTerritories.get(j);
                    if (m == t.territoryNumber) {
                        r.regionTerritories.remove(t);
                    }
                }

                if (r.regionNumber == k.regionNumber) {
                    r.regionTerritories.add(t.territoryNumber);
                }
            }
        }

        repaint();



    }

    private Territory getTerritoryById(int id) {
        for (int i = 0; i < unassignedTerritories.size(); i++) {
            Territory territory = unassignedTerritories.get(i);
            if (territory.territoryNumber == id) {
                return territory;
            }
        }

        for (int i = 0; i < mapInfo.territories.size(); i++) {
            Territory territory = mapInfo.territories.get(i);
            if (territory.territoryNumber == id) {
                return territory;
            }
        }

        return null;
    }

    void toggleConnecting() {
        if (connecting) {
            connecting = false;
            selectedTerritoryA = -1;
            selectedTerritoryB = -1;
        } else {
            connecting = true;
        }

        repaint();
    }

    void saveToFile(File file) {
        try {
            Gson g = new GsonBuilder().setPrettyPrinting().create();

            FileWriter f = new FileWriter(file);

            mapInfo.connectionCount = mapInfo.connections.size();
            mapInfo.regionCount = mapInfo.regions.size();
            mapInfo.territoryCount = mapInfo.territories.size();

            f.write(g.toJson(mapInfo));
            f.close();
        } catch (IOException ex) {
            Logger.getLogger(WarEditorScreen.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void makeVisualConnection() {
        if (visualPoint1 == null || visualPoint2 == null) {
            return;
        }
        double[] p1 = {visualPoint1.getX() / usableWidth, visualPoint1.getY() / usableHeight};
        double[] p2 = {visualPoint2.getX() / usableWidth, visualPoint2.getY() / usableHeight};
        double[][] p = {p1, p2};

        mapInfo.visualConnections.add(p);
        mapInfo.visualConnectionsCount++;

        visualPoint1 = null;
        visualPoint2 = null;
    }

    private void drawVisualConnection(Graphics2D g, double[][] p) {
        g.setColor(Color.GRAY);
        g.setStroke(new BasicStroke(10, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine((int) (p[0][0] * usableWidth), (int) (p[0][1] * usableHeight), (int) (p[1][0] * usableWidth), (int) (p[1][1] * usableHeight));
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        //return;
    }

    public static class Region {

        public int regionNumber;
        public ArrayList<Integer> regionTerritories;
        public Color regionColor;
        public String regionName;
        public int regionValue;
        private static int regionCount;

        public Region(int number, ArrayList<Integer> territories, Color c, String name, int value) {
            regionNumber = number;
            regionTerritories = territories;
            regionColor = c;
            regionName = name;
            regionValue = value;
        }

        private Region() {
            regionNumber = regionCount;
            regionTerritories = new ArrayList<>();
            regionColor = Color.black;
            regionName = "";
            regionValue = 0;
            regionCount++;
        }

        @Override
        public String toString() {
            return regionName;
        }
    }

    private static class MapInfo {

        public String id;
        public int regionCount;
        public int territoryCount;
        public int connectionCount;
        public int visualConnectionsCount;
        public ArrayList<Region> regions;
        public ArrayList<Territory> territories;
        public ArrayList<int[]> connections;
        public ArrayList<double[][]> visualConnections;
        //public ArrayList<VertexData> vertices;
        public double aspectRatio;

        public MapInfo() {
            id = "";
            regionCount = 0;
            territoryCount = 0;
            connectionCount = 0;
            regions = new ArrayList<>();
            territories = new ArrayList<>();
            connections = new ArrayList<>();
            //vertices = new ArrayList<>();
            aspectRatio = 1;
            visualConnections = new ArrayList<>();
        }
    }

    private static class Territory {

        public String territoryName;
        public int territoryNumber;
        public int territoryBorderCount;
        public static int territoryCount = 0;
        public VertexData vertices;

        public Territory(String name, int number, int border) {
            territoryBorderCount = border;
            territoryName = name;
            territoryNumber = number;
        }

        public Territory() {
            territoryBorderCount = 0;
            territoryName = "";
            territoryNumber = territoryCount;
            territoryCount++;
            vertices = new VertexData();
        }
    }

    private static class VertexData {

        public int vertexCount;
        public ArrayList<double[]> vertexPos;

        public VertexData(int count, ArrayList<double[]> pos) {
            vertexCount = count;
            vertexPos = pos;
        }

        public VertexData() {
            vertexCount = 0;
            vertexPos = new ArrayList<>();
        }
    }
}
