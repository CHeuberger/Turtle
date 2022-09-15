/*
 * Copyright: Carlos F. Heuberger. All rights reserved.
 *
 */
package cfh.turtle.gui;

import static java.lang.Math.*;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;

import javax.swing.JPanel;

/**
 * @author Carlos F. Heuberger, 2022-09-13
 *
 */
@SuppressWarnings("serial")
public class TurtlePanel extends JPanel {

    public static final String PROP_PREFIX = "turtle.";
    public static final String PROP_DELAY = PROP_PREFIX + "delay";
    public static final String PROP_DIR = PROP_PREFIX + "dir";
    public static final String PROP_POS = PROP_PREFIX + "pos";
    public static final String PROP_PEN = PROP_PREFIX + "pen";
    public static final String PROP_RESET = PROP_PREFIX + "reset";
    
    private int delay;
    private double dir;
    private double x;
    private double y;
    private boolean pen;
    
    private final Path2D path = new Path2D.Double();

    private Shape turtle = new Polygon(new int[] {20, 0, 0}, new int[] {0, 5, -5}, 3);
    
    TurtlePanel() {
        reset();
    }
    
    public void delay(int time) {
        if (time < 0) {
            throw new IllegalArgumentException("negative time: " + time);
        }
        var old = this.delay;
        this.delay = time;
        firePropertyChange(PROP_DELAY, old, this.delay);
    }
    
    public int delay() { return delay; }
    public double dir() { return dir; }
    public double x() { return x; }
    public double y() { return y; }
    public boolean pen() { return pen; }
    
    public void left(double degrees) {
        var old = dir;
        dir = (dir + degrees) % 360;
        firePropertyChange(PROP_DIR, old, dir);
        repaint();
    }
    
    public void right(double degrees) {
        var old = dir;
        dir = (dir - degrees) %360;
        while (dir < 0) {
            dir += 360;
        }
        firePropertyChange(PROP_DIR, old, dir);
        repaint();
    }
    
    void go0(double amount) {
        var step = amount<0 ? -0.5 : 1;
        var sx = x;
        var sy = y;
        var cos = cos(toRadians(dir));
        var sin = -sin(toRadians(dir));
        if (delay > 0) {
            try {
                for (var i = 1; i < (amount / step); i++) {
                    var d = i * step;
                    var old = new Point2D.Double(x, y);
                    x = sx + d * cos;
                    y = sy + d * sin;
                    firePropertyChange(PROP_POS, old, new Point2D.Double(x, y));
                    repaint();
                    Thread.sleep(delay);
                }
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
        x = sx + amount * cos;
        y = sy + amount * sin;
    }
    
    public void forward(double amount) {
        var old = new Point2D.Double(x, y);
        go0(amount);
        if (pen) {
            path.lineTo(x, y);
        } else {
            path.moveTo(x, y);
        }
        firePropertyChange(PROP_POS, old, new Point2D.Double(x, y));
        repaint();
    }
    
    public void backward(double amount) {
        forward(-amount);
    }
    public void pen(boolean down) {
        var old = pen;
        pen = down;
        firePropertyChange(PROP_PEN, old, pen);
        repaint();
    }
    
    public void reset() {
        dir = 0;
        x = y = 0;
        pen = true;
        path.reset();
        path.moveTo(x, y);
        firePropertyChange(PROP_RESET, null, null);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        var gg = (Graphics2D) g.create();
        try {
            gg.setColor(Color.WHITE);
            gg.fill(getBounds());
            
            gg.translate(getWidth()/2, getHeight()/2);
            
            drawPath(gg);
            drawAxes(gg);
            drawTurtle(gg);
        } finally {
            gg.dispose();
        }
    }
    
    private void drawPath(Graphics2D gg) {
        gg.setColor(getForeground());
        gg.draw(path);
        if (pen) {
            var end = path.getCurrentPoint();
            if (end == null) {
                gg.draw(new Line2D.Double(0, 0, x, y));
            } else {
                gg.draw(new Line2D.Double(end.getX(), end.getY(), x, y));
            }
        }
    }

    private void drawAxes(Graphics2D g) {
        var gg = (Graphics2D) g.create();
        try {
            var foreground = getForeground();
            gg.setColor(new Color(foreground.getColorSpace(), foreground.getColorComponents(null), 0.2f));
            gg.setStroke(new BasicStroke(1, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10, 
                new float[] {5, 5, 5, 5}, 0));
            var clip = gg.getClipBounds();
            gg.drawLine(0, clip.y, 0, clip.y+clip.height);
            gg.drawLine(clip.x,  0, clip.x+clip.width, 0);
        } finally {
            gg.dispose();
        }
    }
    
    private void drawTurtle(Graphics2D g) {
        var gg = (Graphics2D) g.create();
        try {
            gg.setColor(new Color(0, 0, 255, 100));
            gg.translate(x, y);
            gg.rotate(-toRadians(dir));
            if (pen) {
                gg.fill(turtle);
            } else
                gg.draw(turtle);
        } finally {
            gg.dispose();
        }
    }
}
