package cfh.turtle;

import java.awt.Color;

public class Pyramid2{

    public static void main(String[]args) {
        Turtle t = new Turtle();
        t.delay(100);
        pyramid(t, 200, 5);
        pyramid2(new Turtle(Color.BLUE), 200, 5);
        
        System.out.println(t);
    }

    public static void square(Turtle t, double base, int levels){
        for (int i = 0; i < 4 ; i++){
            t.forward(base/((levels*2)-1));
            t.left(90);
        }
    }

    public static void  pyramid(Turtle t, double base, int levels){
        //for (int p = 1; (p-1)< levels; p++){
        for (int j=0; j <levels; j++){
            int count = ((levels)*2)-(j*2)-1;
            for (int i=0; i<count; i++){
                System.out.printf("j=%-2d  i=%-2d  dir=%-3d%n", j, i, t.dir());
                square(t, base, levels);
                t.forward(base/((levels*2)-1));
            }

            t.penup();
            t.left(90);
            t.forward(base/((levels*2)-1));
            t.right(90);
//            t.backward(base-(base/(((levels*2)-(j*2))-1)));
            t.backward(base/(2*levels-1)*(count-1));
            t.pendown();
        }
    }
    
    public static void pyramid2(Turtle turtle, double base, int levels) {
        for (int j=levels; j > 0; j--){
            level2(turtle, j, base /(2*levels-1));
        }

    }
    
    public static void level2(Turtle turtle, int level, double size) {
        var count = 2*level - 1;
        for (var i = 0; i < count; i++) {
            square(turtle, size);
            turtle.penup();
            turtle.forward(size);
            turtle.pendown();
        }
        turtle.penup();
        turtle.backward(size*(count-1));
        turtle.left(90);
        turtle.forward(size);
        turtle.right(90);
        turtle.pendown();
    }
    
    public static void square(Turtle turtle, double size) {
        for (var i = 0; i < 4; i++) {
            turtle.forward(size);
            turtle.left(90);
        }
    }
}