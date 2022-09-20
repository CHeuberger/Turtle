/*
 * Copyright: Carlos F. Heuberger. All rights reserved.
 *
 */
package cfh.turtle.gui;

import static java.util.Objects.*;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.function.Consumer;

import javax.swing.SwingWorker;

import cfh.turtle.calc.Calculator;
import cfh.turtle.calc.Value;

/**
 * @author Carlos F. Heuberger, 2022-09-15
 *
 */
public class RunWorker extends SwingWorker<Void, Void> {

    private final String text;
    private final TurtlePanel turtle;
    private final Consumer<SwingWorker<Void, Void>> finisher;
    
    private final Map<String, Value> variables = new HashMap<>();

    RunWorker(String text, TurtlePanel turtle, Consumer<SwingWorker<Void, Void>> finisher) {
        this.text = requireNonNull(text);
        this.turtle = requireNonNull(turtle);
        this.finisher = requireNonNull(finisher);
    }

    @Override
    protected Void doInBackground() throws Exception {
        variables.clear();
        var lineNumber = 0;
        try (Scanner scanner = new Scanner(text)) {
            while (scanner.hasNextLine()) {
                lineNumber += 1;
                String[] words;
                var line = scanner.nextLine().trim();
                if (line.isBlank() || line.startsWith("#"))
                    continue;

                words = line.split("\\h*=\\h*", 2);
                if (words.length == 2) {
                    var name = words[0];
                    if (name.isBlank()) throw new ParseException("empty name", lineNumber);
                    try { 
                        var value = Value.of(words[1]);
                        variables.put(name, value);
                    } catch (NumberFormatException ex) {
                        var message = ex.getClass().getSimpleName() + ": " + ex.getMessage();
                        throw (ParseException) new ParseException(message, lineNumber).initCause(ex);
                    }
                    continue;
                }
                
                words = line.split("\\h++", 2);
                switch (words[0].toLowerCase()) {
                    case "delay" -> delay(arg(words, 1, lineNumber));
                    case "reset" -> turtle.reset();
                    case "forward", "move" -> forward(arg(words, 1, lineNumber));
                    case "backward", "back" -> backward(arg(words, 1, lineNumber));
                    case "left" -> left(words.length<2 ? null : words[1]);
                    case "right" -> right(words.length<2 ? null : words[1]);
                    case "up", "penup " -> turtle.pen(false);
                    case "down", "pendown" -> turtle.pen(true);
                    default -> throw new ParseException("invalid command \"" + line + "\"", lineNumber);
                }
            }
        }
        return null;
    }

    @Override
    protected void done() {
        finisher.accept(this);
    }
    
    private String arg(String[] words, int i, int lineNumber) throws ParseException {
        if (i < words.length)
            return words[i];
        else
            throw new ParseException("missing argument", lineNumber);
    }
    private void forward(String amount) {
        var a = calculate(amount);
        turtle.forward(a);
    }
    
    private void backward(String amount) {
        var a = calculate(amount);
        turtle.backward(a);
    }
    
    private void left(String degrees) {
        var d = degrees==null||degrees.isBlank() ? 90 : calculate(degrees);
        turtle.left(d);
    }
    
    private void right(String degrees) {
        var d = degrees==null||degrees.isBlank() ? 90 : calculate(degrees);
        turtle.right(d);
    }
    
    private void delay(String time) {
        turtle.delay((int) calculate(time));
    }
    
    private double calculate(String text) {
        return new Calculator(variables).asDouble(text);
    }
}
