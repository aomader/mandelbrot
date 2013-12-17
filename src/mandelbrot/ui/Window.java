package mandelbrot.ui;

import mandelbrot.core.Model;

import javax.swing.*;
import java.awt.*;

public class Window extends JFrame {

    public Window() {
        super();

        setTitle("Mandelbrot");
        setSize(200, 200);
        setVisible(true);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        Model model = new Model();

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new Controls(model), BorderLayout.WEST);
        panel.add(new View(model), BorderLayout.CENTER);

        setContentPane(panel);
        pack();
    }
}
