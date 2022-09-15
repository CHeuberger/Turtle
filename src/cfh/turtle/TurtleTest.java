/*
 * Copyright: Carlos F. Heuberger. All rights reserved.
 *
 */
package cfh.turtle;

/**
 * @author Carlos F. Heuberger, 2022-09-13
 *
 */
public class TurtleTest {

    public static void main(String[] args) {
        var turtle = new Turtle();
        
        turtle.pendown();
        turtle.forward(20);
        turtle.right(90);
        turtle.forward(5);
        turtle.penup();
        
        turtle.backward(5);
        turtle.left(90);
        turtle.forward(20);
        
        turtle.pendown();
        for (var i = 0; i < 4; i++) {
            turtle.forward(20);
            turtle.left(90);
        }
        turtle.penup();
        
//        turtle.up().move(-500).down().move(40).up().move(480);
        turtle.up().move(1000).down().move(40).up();
        
        System.out.println(turtle);
    }
}
