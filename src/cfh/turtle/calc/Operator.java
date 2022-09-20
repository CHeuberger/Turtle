/*
 * Copyright: Carlos F. Heuberger. All rights reserved.
 *
 */
package cfh.turtle.calc;

import static java.util.function.Function.*;
import static java.util.stream.Collectors.*;

import java.util.Arrays;
import java.util.Map;
import java.util.Stack;
import java.util.function.Consumer;
import java.util.function.DoubleBinaryOperator;

/**
 * @author Carlos F. Heuberger, 2022-09-20
 *
 */
public enum Operator {
    
    MUL("*", 120, true, op2((a,b) -> a * b)),
    DIV("/", 120, true, op2((a,b) -> a / b)),
    ADD("+", 110, true, op2((a,b) -> a + b)),
    SUB("-", 110, true, op2((a,b) -> a - b)),
    ;
    
    //==============================================================================================
    
    private static final Map<String, Operator> operators;
    static {
        operators = Arrays.stream(Operator.values())
            .collect(toUnmodifiableMap(Operator::symbol, identity()));
    }

    public static boolean is(String symbol) { return operators.containsKey(symbol); }
    public static Operator get(String symbol) { return operators.get(symbol); }
    
    private static Consumer<Stack<Value>> op2(DoubleBinaryOperator op) {
        return (Stack<Value> stack) -> {
            if (stack.size() < 2)
                throw new ArithmeticException("not enough values");
            var a = stack.pop().asDouble();
            var b = stack.pop().asDouble();
            var result = op.applyAsDouble(a, b);
            stack.push(Value.of(result));
        };
    }

    //----------------------------------------------------------------------------------------------
    
    private final String symbol;
    private final int precedence;
    private final boolean left;
    private final Consumer<Stack<Value>> runner;
    
    private Operator(String symbol, int precedence, boolean left, Consumer<Stack<Value>> runner) {
        assert !symbol.isBlank() : "blank symbol";
        this.symbol = symbol;
        this.precedence = precedence;
        this.left = left;
        assert runner != null : "null runner";
        this.runner = runner;
    }
    
    public String symbol() { return symbol; }
    public boolean left() { return left; }
    
    public boolean before(Operator other) {
        return (this.precedence > other.precedence) 
            || (this.precedence == other.precedence && this.left);
    }
    
    public void execute(Stack<Value> stack) {
        runner.accept(stack);
    }
    
    @Override
    public String toString() {
        return symbol;
    }
}
