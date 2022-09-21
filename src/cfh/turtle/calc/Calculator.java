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
    
    public Value eval(String text) throws NumberFormatException, IllegalArgumentException {
        var values = new Stack<Value>();
        var operators = new Stack<Operator>();
        
        System.out.println(text);  // XXX
        try (var scanner = new Scanner(text);) {
            while (scanner.hasNext()) {
                var word = scanner.next();
                
                if (Operator.is(word)) {
                    var op = Operator.get(word);
                    if (op == Operator.RPAR) {
                        while (!operators.isEmpty() && operators.peek() != Operator.LPAR) {
                            var top = operators.pop();
                            top.execute(values);
                        }
                        if (operators.isEmpty() || operators.pop() != Operator.LPAR)
                            throw new IllegalArgumentException("mismatched parenthesis");
                        
                    } else {
                        while (!operators.isEmpty()
                            && operators.peek() != Operator.LPAR
                            && operators.peek().before(op) )
                        {
                            var top = operators.pop();
                            top.execute(values);
                        }
                        operators.push(op);
                    }

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
        
        System.out.println();  // XXX
        return values.pop();
    }
}
