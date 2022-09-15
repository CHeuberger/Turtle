/*
 * Copyright: Carlos F. Heuberger. All rights reserved.
 *
 */
package cfh.turtle;

import static java.lang.Math.*;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * @author Carlos F. Heuberger, 2022-09-13
 *
 */
public class Turtle {

    private final Path2D path = new Path2D.Double();
    
    private boolean pen = true;
    private double dir = 0;
    private double x = 0;
    private double y = 0;
    
    private boolean move = true;
    private int delay = 0;

    public Turtle() {
        this(Color.BLACK);
    }
    
    public Turtle(Color color) {
        var panel = new TurtlePanel();
        panel.setForeground(color);
        addListener(panel::repaint);
        
        var frame = new JFrame("Turtle");
        frame.setDefaultCloseOperation(frame.DISPOSE_ON_CLOSE);
        frame.setSize(800, 600);
        frame.add(panel);
        frame.validate();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public Turtle delay(int milliseconds) {
       if (milliseconds < 0) throw new IllegalArgumentException("negative delay: " + milliseconds);
       delay = milliseconds;
       return this;
    }

    public Turtle up() { return penup(); }
    
    public Turtle penup() {
        pen = false;
        return this;
    }
    
    public Turtle down() { return pendown(); }
    
    public Turtle pendown() {
        if (!pen) {
            move = true;
        }
        pen = true;
        return this;
    }
    
    public Turtle left() { return left(90); }
    
    public Turtle left(int degrees) {
        dir += toRadians(degrees);
        while (dir > 2 * PI) {
            dir -= 2*PI;
        }
        return this;
    }
    
    public Turtle right() { return right(90); }
    
    public Turtle right(int degrees) {
        dir -= toRadians(degrees);
        while (dir < 0) {
            dir += 2*PI;
        }
        return this;
    }
    
    public Turtle move(double amount) { return forward(amount); }
    
    public Turtle forward(double amount) {
        if (pen && move) {
            path.moveTo(x,  y);
            move = false;
        }
        x +=  amount * cos(dir);
        y += -amount * sin(dir);
        if (pen) {
            path.lineTo(x, y);
            repaint();
        }
        return this;
    }
    
    public Turtle backward(double amount) {
        return forward(-amount);
    }
    
    public int dir() {
        return (int) round(toDegrees(dir)) % 360;
    }
    
    public Path2D path() {
        return path;
    }
    
    @Override
    public String toString() {
        try ( var formatter = new Formatter() ) {
            for (var iter = path.getPathIterator(null); !iter.isDone(); iter.next()) {
                var coords = new double[6];
                var type = iter.currentSegment(coords);
                formatter.format(
                    switch (type) {
                        case PathIterator.SEG_MOVETO -> "move %4.0f %4.0f%n";
                        case PathIterator.SEG_LINETO -> "line %4.0f %4.0f%n";
                        default -> "unhandled type: " + type;
                    },
                    coords[0], coords[1]);
            }
            return formatter.toString();
        }
    }
    
    //----------------------------------------------------------------------------------------------
    
    private final List<Listener> listeners = new ArrayList<>();
    
    public boolean addListener(Listener l) {
        return listeners.add(l);
    }
    
    public boolean removeListner(Listener l) {
        return listeners.remove(l);
    }
    
    private void repaint() {
        for (var l : listeners) {
            l.repaint(delay, path);
        }
    }
    
    ////////////////////////////////////////////////////////////////////////////////////////////////
    
    public static interface Listener {
        public void repaint(int delay, Path2D path);
    }
}

@SuppressWarnings("serial")
class TurtlePanel extends JPanel implements Turtle.Listener {

    private Path2D path = null;
    
    TurtlePanel() { 
        //
    }
    
    @Override
    public void repaint(int delay, Path2D path) {
        this.path = path;
        repaint();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        

        if (path != null) {
            var gg = (Graphics2D) g.create();
            try {
                var panel = getBounds();
                panel.x = -panel.width/2;
                panel.y = -panel.height/2;
                var bounds = path.getBounds();
                if (!panel.contains(bounds)) {
                    bounds.add(0, 0);
                    bounds.grow(10, 10);
                    double scale = Math.min(panel.getWidth()/bounds.getWidth(), panel.getHeight()/bounds.getHeight());
                    gg.scale(scale, scale);
                    gg.translate(-bounds.x, -bounds.y);
                } else {
                    gg.translate(-panel.x, -panel.y);
                }
                paintAxes(gg);
                paintPath(gg);
            } finally {
                gg.dispose();
            }
        }
    }

    private void paintAxes(Graphics2D gg) {
        var foreground = gg.getColor();
        gg.setColor(new Color(foreground.getColorSpace(), foreground.getColorComponents(null), 0.2f));
        var stroke = gg.getStroke();
        try {
            gg.setStroke(new BasicStroke(1, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10, 
                new float[] {5, 5, 5, 5}, 0));
            var clip = gg.getClipBounds();
            gg.drawLine(0, clip.y, 0, clip.y+clip.height);
            gg.drawLine(clip.x,  0, clip.x+clip.width, 0);
        } finally {
            gg.setStroke(stroke);
        }
    }
    
    private void paintPath(Graphics2D gg) {
        gg.setColor(getForeground());
        gg.draw(path);
    }
}

