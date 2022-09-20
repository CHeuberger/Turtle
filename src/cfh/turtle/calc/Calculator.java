/*
 * Copyright: Carlos F. Heuberger. All rights reserved.
 *
 */
package cfh.turtle.calc;

import static java.util.Objects.*;

import java.util.Map;
import java.util.Scanner;
import java.util.Stack;

/**
 * @author Carlos F. Heuberger, 2022-09-20
 *
 */
public class Calculator {

    private final Map<String, Value> variables;
    
    public Calculator(Map<String, Value> variables) {
        this.variables = requireNonNull(variables);
    }
    
    public double asDouble(String text) {
        var values = new Stack<Value>();
        var operators = new Stack<Operator>();
        
        try (var scanner = new Scanner(text);) {
            while (scanner.hasNext()) {
                var word = scanner.next();
                
                if (Operator.is(word)) {
                    var op = Operator.get(word);
                    if (!operators.isEmpty()) {
                        var top = operators.peek();
                        if (top.before(op)) {
                            assert top == operators.pop();
                            top.execute(values);
                        }
                    }
                    operators.push(op);
                } else if(variables.containsKey(word)) {
                    var value = variables.get(word);
                    values.push(value);
                } else {
                    var value = Value.of(word);
                    values.push(value);
                }
            }
        }
        
        while (!operators.isEmpty()) {
            var op = operators.pop();
            op.execute(values);
        }
        
        if (values.size() != 1) {
            System.err.println(values);
            throw new IllegalArgumentException("invalid expression");
        }
        
        return values.pop().asDouble();
    }
}
