/*
 * Copyright: Carlos F. Heuberger. All rights reserved.
 *
 */
package cfh.turtle.calc;

/**
 * @author Carlos F. Heuberger, 2022-09-20
 *
 */
public sealed interface Value permits Constant {

    public static Value of(String text) throws NumberFormatException {
        return new Constant(Double.parseDouble(text));
    }
    
    static Value of(double value) {
        return new Constant(value);
    }
    
    public double asDouble();

}

/**
 * @author Carlos F. Heuberger, 2022-09-20
 *
 */
final class Constant implements Value {
    private final double value;
    
    Constant(double value) {
        this.value = value;
    }
    @Override
    public double asDouble() {
        return value;
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
