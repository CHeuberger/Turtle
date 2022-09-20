/*
 * Copyright: Carlos F. Heuberger. All rights reserved.
 *
 */
package cfh.turtle.gui;

import static javax.swing.JOptionPane.*;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;

/**
 * @author Carlos F. Heuberger, 2022-09-13
 *
 */
@SuppressWarnings("serial")
public class MainPanel extends JPanel {

    private static final Border EMPTY_BORDER = new EmptyBorder(4, 4, 4, 4);
    private static final Font FONT = new Font("monospaced", Font.PLAIN, 12);
    
    private final JTextArea script;
    private final TurtlePanel turtle;
    private final JTextField turtleStatus;
    
    private final List<Object> actions = new ArrayList<>();
    
    private static final String PREF_SCRIPT = "script.text";
    private static final String PREF_FILE = "script.file";
    private final Preferences preferences = Preferences.userNodeForPackage(getClass());
    
    private boolean changed = false;
    
    MainPanel() {
        var newScript = newMenuItem("New", this::doNew);
        var open = newMenuItem("Open ...", this::doOpen);
        var save = newMenuItem("Save ...", this::doSave);
        
        var run = newMenuItem("Run", this::doRun);
        
        var quit = newMenuItem("Quit", this::doQuit);
        
        var scriptMenu = newMenu("Script");
        scriptMenu.add(newScript);
        scriptMenu.add(open);
        scriptMenu.add(save);
        scriptMenu.addSeparator();
        scriptMenu.add(run);
        scriptMenu.addSeparator();
        scriptMenu.add(quit);
        
        var refresh = newButton("Refresh", this::doRefresh);
        
        var bar = new JMenuBar();
        bar.add(scriptMenu);
        bar.add(new JSeparator());
        bar.add(newButton(run.getAction()));
        bar.add(new JSeparator());
        bar.add(refresh);
        
        script = newTextArea(40, 40);
        script.setText(preferences.get(PREF_SCRIPT, ""));
        script.getActionMap().put("Run", newAction("Run", this::doRun));
        script.getInputMap().put(KeyStroke.getKeyStroke("ctrl ENTER"), "Run");
        script.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void removeUpdate(DocumentEvent e) { changed = true; }
            @Override public void insertUpdate(DocumentEvent e) { changed = true; }
            @Override public void changedUpdate(DocumentEvent e) { changed = true; }
        });
        actions.add(script);
        
        turtle = new TurtlePanel();
        turtle.setForeground(Color.BLACK);
        turtle.setPreferredSize(new Dimension(1000, 800));
        turtle.addPropertyChangeListener(this::turtleChanged);
        
        turtleStatus = newTextField("");
        turtleStatus.setEditable(false);
        
        setLayout(new BorderLayout());
        add(bar, BorderLayout.PAGE_START);
        add(newScrollPane(script), BorderLayout.LINE_START);
        add(newScrollPane(turtle), BorderLayout.CENTER);
        add(turtleStatus, BorderLayout.PAGE_END);
        
        enabled(true);
    }

    public boolean closing() {
        return !changed || showConfirmDialog(this, "Script changed, quit anyway?", "Quit", YES_NO_OPTION) == YES_OPTION;
    }
    
    private void enabled(boolean enabled) {
        for (var obj : actions) {
            if (obj instanceof Action action) {
                action.setEnabled(enabled);
            } else if (obj instanceof Component comp) {
                comp.setEnabled(enabled);
            }
        }
    }
    
    private void doNew(ActionEvent ev) {
        if (!changed || showConfirmDialog(this, "Clear actual script?", "New", YES_NO_OPTION) == YES_OPTION) {
            script.setText("");
            changed = false;
        }
    }
    
    private void doOpen(ActionEvent ev) {
        if (!changed || showConfirmDialog(this, "Discard actual script?", "New", YES_NO_OPTION) == YES_OPTION) {
            var file = new File(preferences.get(PREF_FILE, ""));
            var chooser = new JFileChooser();
            chooser.setAcceptAllFileFilterUsed(true);
            chooser.setFileSelectionMode(chooser.FILES_ONLY);
            chooser.setMultiSelectionEnabled(false);
            chooser.setSelectedFile(file);
            if (chooser.showOpenDialog(this) == chooser.APPROVE_OPTION) {
                file = chooser.getSelectedFile();
                preferences.put(PREF_FILE, file.getAbsolutePath());
                try (var reader = new BufferedReader(new FileReader(file))) {
                    script.setText(reader.lines().collect(Collectors.joining("\n")));
                    changed = false;
                } catch (IOException ex) {
                    ex.printStackTrace();
                    error(ex.getClass().getSimpleName(), "Exception reading \"%s\":", file, ex.getMessage());
                }
                preferences.put(PREF_SCRIPT, script.getText());
            }
        }
    }
    
    private void doSave(ActionEvent ev) {
        var file = new File(preferences.get(PREF_FILE, ""));
        var chooser = new JFileChooser();
        chooser.setAcceptAllFileFilterUsed(true);
        chooser.setFileSelectionMode(chooser.FILES_ONLY);
        chooser.setMultiSelectionEnabled(false);
        chooser.setSelectedFile(file);
        if (chooser.showSaveDialog(this) == chooser.APPROVE_OPTION) {
            file = chooser.getSelectedFile();
            preferences.put(PREF_FILE, file.getAbsolutePath());
            try (var writer = new BufferedWriter(new FileWriter(file))) {
                writer.write(script.getText());
                changed = false;
            } catch (IOException ex) {
                ex.printStackTrace();
                error(ex.getClass().getSimpleName(), "Exception saving \"%s\":", file, ex.getMessage());
            }
        }
    }
    
    private void doRun(ActionEvent ev) {
        enabled(false);
        var text = script.getText();
        preferences.put(PREF_SCRIPT, text);
        
        var worker = new RunWorker(text, turtle, this::runDone);
        worker.execute();
    }
    
    private void runDone(SwingWorker<?, ?> worker) {
        try {
            worker.get();
        } catch (ExecutionException ex) {
            var cause = ex.getCause();
            cause.printStackTrace();
            error(cause.getClass().getSimpleName(), cause.getMessage());
            if (cause instanceof ParseException pex) {
                var line = pex.getErrorOffset() - 1;
                if (line >= 0) {
                    try {
                        var start = script.getLineStartOffset(line);
                        var end = script.getLineEndOffset(line) - 1;
                        script.setEnabled(true);
                        script.select(start, end);
                    } catch (BadLocationException lex) {
                        lex.printStackTrace();
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            error(ex.getClass().getSimpleName(), ex.getMessage());
        } finally {
             enabled(true);
             script.requestFocus();
        }

    }
    
    private void doQuit(ActionEvent ev) {
        if (closing()) {
            SwingUtilities.getWindowAncestor(this).dispose();
        }
    }
    
    private void doRefresh(ActionEvent ev) {
        enabled(true);
        updateTurtleStatus();
        repaint();
    }
    
    private void turtleChanged(PropertyChangeEvent ev) {
        if (ev.getPropertyName().startsWith(TurtlePanel.PROP_PREFIX)) {
            updateTurtleStatus();
        }
    }
    
    private void updateTurtleStatus() {
        var status = "%s %03.0f° %5.0f,%-5.0f  %5dms".formatted(
            turtle.pen()?"▼":"△", turtle.dir(), turtle.x(), turtle.y(), turtle.delay());
        turtleStatus.setText(status);
    }
    
    private void error(String title, String format, Object... args) {
        var message = format.formatted(args);
        System.err.printf("%s : %s%n", title, message);
        showMessageDialog(this, message, title, ERROR_MESSAGE);
    }
    
    private Action newAction(String name, ActionListener listener) {
        var action = new AbstractAction(name) {
            @Override
            public void actionPerformed(ActionEvent e) {
                listener.actionPerformed(e);
            }
        };
        actions.add(action);
        return action;
    }
    
    private JMenu newMenu(String title) {
        var menu = new JMenu(title);
        return menu;
    }
    
    private JMenuItem newMenuItem(String name, ActionListener listener) {
        return newMenuItem(newAction(name, listener));
    }
    
    private JMenuItem newMenuItem(Action action) {
        var item = new JMenuItem(action);
        return item;
    }
    
    private JTextField newTextField(String text) {
        var field = new JTextField(text);
        field.setFont(FONT);
        return field;
    }
    
    private JTextArea newTextArea(int rows, int cols) {
        var area = new JTextArea(rows, cols);
        area.setFont(FONT);
        return area;
    }
    
    private JButton newButton(Action action) {
        var button = new JButton(action);
        button.setFocusable(false);
        return button;
    }
    
    private JButton newButton(String title, ActionListener listener) {
        var button = new JButton(title);
        button.addActionListener(listener);
        button.setFocusable(false);
        return button;
    }
    
    private JScrollPane newScrollPane(Component comp) {
        var pane = new JScrollPane(comp);
        pane.setBorder(EMPTY_BORDER);
        return pane;
    }
}
