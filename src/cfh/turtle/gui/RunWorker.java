/*
 * Copyright: Carlos F. Heuberger. All rights reserved.
 *
 */
package cfh.turtle.gui;

import static java.util.Objects.*;

import java.util.HashMap;
import java.util.Map;
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
    
    private final Map<String, Value> variables = new HashMap<>();

    RunWorker(String text, TurtlePanel turtle, Consumer<Callable<Void>> finisher) {
        this.text = requireNonNull(text);
        this.turtle = requireNonNull(turtle);
        this.finisher = requireNonNull(finisher);
    }

    @Override
    protected Void doInBackground() throws Exception {
        variables.clear();
        try (Scanner scanner = new Scanner(text)) {
            while (scanner.hasNextLine()) {
                String[] words;
                var line = scanner.nextLine().trim();
                if (line.isBlank() || line.startsWith("#"))
                    continue;

                words = line.split("\\h*=\\h*", 2);
                if (words.length == 2) {
                    variables.put(words[0], Constant.of(words[1]));
                    continue;
                }
                
                words = line.split("\\h++", 2);
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
    
    ////////////////////////////////////////////////////////////////////////////////////////////////
    
    private static sealed interface Value {
        //
    }
    
    private static final class Constant implements Value {
        static Constant of(String text) throws NumberFormatException {
            return new Constant(Double.parseDouble(text));
        }
        
        private final double value;
        Constant(double value) {
            this.value = value;
        }
        @Override
        public String toString() {
            return Double.toHexString(value);
        }
        @Override
        public int hashCode() {
            return Double.hashCode(value);
        }
        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            return (obj instanceof Constant other) && (other.value == this.value);
        }
    }
}
