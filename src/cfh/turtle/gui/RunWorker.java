/*
 * Copyright: Carlos F. Heuberger. All rights reserved.
 *
 */
package cfh.turtle.gui;

import static java.util.Objects.*;

import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

import javax.swing.SwingWorker;

/**
 * @author Carlos F. Heuberger, 2022-09-15
 *
 */
public class RunWorker extends SwingWorker<Void, Void> {

    private final String text;
    private final TurtlePanel turtle;
    private final Consumer<Callable<Void>> finisher;

    RunWorker(String text, TurtlePanel turtle, Consumer<Callable<Void>> finisher) {
        this.text = requireNonNull(text);
        this.turtle = requireNonNull(turtle);
        this.finisher = requireNonNull(finisher);
    }

    @Override
    protected Void doInBackground() throws Exception {
        try (Scanner scanner = new Scanner(text)) {
            while (scanner.hasNextLine()) {
                var line = scanner.nextLine().trim();
                if (line.isBlank() || line.startsWith("#"))
                    continue;
                var words = line.split("\\h++", 2);
                switch (words[0].toLowerCase()) {
                    case "delay" -> delay(words[1]);
                    case "reset" -> turtle.reset();
                    case "forward", "move" -> forward(words[1]);
                    case "backward", "back" -> backward(words[1]);
                    case "left" -> left(words.length<2 ? null : words[1]);
                    case "right" -> right(words.length<2 ? null : words[1]);
                    case "up", "penup " -> turtle.pen(false);
                    case "down", "pendown" -> turtle.pen(true);
                    default -> throw new IllegalArgumentException("invalid command \"" + line + "\"");
                }
            }
        }
        return null;
    }

    @Override
    protected void done() {
        finisher.accept(this::get);
    }
    
    private void forward(String amount) {
        var a = parse(amount);
        turtle.forward(a);
    }
    
    private void backward(String amount) {
        var a = parse(amount);
        turtle.backward(a);
    }
    
    private void left(String degrees) {
        var d = degrees==null||degrees.isBlank() ? 90 : parse(degrees);
        turtle.left(d);
    }
    
    private void right(String degrees) {
        var d = degrees==null||degrees.isBlank() ? 90 : parse(degrees);
        turtle.right(d);
    }
    
    private void delay(String time) {
        turtle.delay((int) parse(time));
    }
    
    private double parse(String string) {
        return Double.parseDouble(string);
    }
}
