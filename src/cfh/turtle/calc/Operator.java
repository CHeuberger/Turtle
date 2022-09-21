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
import java.util.function.IntBinaryOperator;

/**
 * @author Carlos F. Heuberger, 2022-09-20
 *
 */
/*
 * 
 */
public enum Operator {
    
    LPAR("(", 150, true, null),
    RPAR(")", 150, true, null),
    
    // post increment ++ --  R
    // pre-increment ++ --, unary + - ! ~ R
    
    EXP("^", 130, false, opDD((a,b) -> Math.pow(a, b))),
    
    MUL("*", 120, true, opDD((a,b) -> a * b)),
    DIV("/", 120, true, opDD((a,b) -> a / b)),
    MOD("%", 120, true, opDD((a,b) -> a % b)),
    
    ADD("+", 110, true, opDD((a,b) -> a + b)),
    SUB("-", 110, true, opDD((a,b) -> a - b)),
    
    LEF_SHIFT("<<", 100, true, opII((a,b) -> a << b)),
    RIGHT_SHIFT(">>", 100, true, opII((a,b) -> a >> b)),
    UNSIGNED_SHIFT(">>>", 100, true, opII((a,b) -> a >>>b)),
    // shift << >> >>> 
    // relational < > <= >=
    // equal == !=
    // AND &
    // XOR ^
    // OR |
    // sAND &&
    // sOR ||
    // ternary ?:
    //(assignment)???
    ;
    
    //==============================================================================================
    
    private static final Map<String, Operator> operators;
    static {
        operators = Arrays.stream(Operator.values())
            .collect(toUnmodifiableMap(Operator::symbol, identity()));
    }

    public static boolean is(String symbol) { return operators.containsKey(symbol); }
    public static Operator get(String symbol) { return operators.get(symbol); }
    
    private static Consumer<Stack<Value>> opII(IntBinaryOperator op) {
        return (Stack<Value> stack) -> {
            if (stack.size() < 2)
                throw new ArithmeticException("not enough values");
            var right = stack.pop().asInt();
            var left = stack.pop().asInt();
            var result = op.applyAsInt(left, right);
            System.out.printf("(%s %s) = %s", left, right, result);  // XXX
            stack.push(Value.of(result));
        };
    }
    private static Consumer<Stack<Value>> opDD(DoubleBinaryOperator op) {
        return (Stack<Value> stack) -> {
            if (stack.size() < 2)
                throw new ArithmeticException("not enough values");
            var right = stack.pop().asDouble();
            var left = stack.pop().asDouble();
            var result = op.applyAsDouble(left, right);
            System.out.printf("(%s %s) = %s", left, right, result);  // XXX
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
        this.runner = runner;
    }
    
    public String symbol() { return symbol; }
    public boolean left() { return left; }
    
    public boolean before(Operator other) {
        return (this.precedence > other.precedence) 
            || (this.precedence == other.precedence && this.left);
    }
    
    public void execute(Stack<Value> stack) {
        System.out.print(symbol);  // XXX
        System.out.flush();
        runner.accept(stack);
        System.out.println();  // XXX
    }
    
    @Override
    public String toString() {
        return symbol;
    }
}
