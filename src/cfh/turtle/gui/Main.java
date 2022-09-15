/*
 * Copyright: Carlos F. Heuberger. All rights reserved.
 *
 */
package cfh.turtle.gui;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 * @author Carlos F. Heuberger, 2022-09-13
 *
 */
public class Main {

    public static void main(String... args) {
        SwingUtilities.invokeLater(new Main(args)::initGUI);
    }

    private Main(String... args) {
        //
    }
    
    private void initGUI() {
        var panel = new MainPanel();
        
        var frame = new JFrame("Turtle");
        frame.setDefaultCloseOperation(frame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (panel.closing()) {
                    frame.dispose();
                }
            }
        });
        frame.add(panel, BorderLayout.CENTER);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
