package mandelbrot.ui;

import javax.swing.*;

public class Window extends JFrame {

    public Window() {
        setTitle("Mandelbrot");
        setSize(200, 200);
        setVisible(true);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        setContentPane(new View());
    }
}
