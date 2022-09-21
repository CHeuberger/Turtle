/*
 * Copyright: Carlos F. Heuberger. All rights reserved.
 *
 */
package cfh.turtle.calc;

/**
 * @author Carlos F. Heuberger, 2022-09-20
 *
 */
public sealed interface Value  {

    public static Value of(String text) throws NumberFormatException {
        try {
            return new Int(Integer.parseInt(text));
        } catch (NumberFormatException ex) {
            return new Num(Double.parseDouble(text));
        }
    }

    static Value of(int value) {
        return new Int(value);
    }
    
    static Value of(double value) {
        return new Num(value);
    }

    public int asInt();
    public double asDouble();

    //==============================================================================================

    final class Num implements Value {
        private final double value;

        Num(double value) { this.value = value; }
        @Override public int asInt() { return (int) Math.round(value); }
        @Override public double asDouble() { return value; }
        @Override public String toString() { return Double.toString(value); }
        @Override public int hashCode() { return Double.hashCode(value); }
        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            return (obj instanceof Num other) && (other.value == this.value);
        }
    }
    
    //==============================================================================================
    
    final class Int implements Value {
        private final int value;
        
        Int(int value) { this.value = value; }
        @Override public int asInt() { return value; }
        @Override public double asDouble() { return value; }
        @Override public String toString() { return Integer.toString(value); }
        @Override public int hashCode() { return Integer.hashCode(value); }
        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            return (obj instanceof Int other) && (other.value == this.value);
        }
    }
}
