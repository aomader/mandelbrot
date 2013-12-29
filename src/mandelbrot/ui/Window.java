package mandelbrot.ui;

import mandelbrot.core.Model;

import javax.swing.*;
import java.awt.*;

public class Window extends JDialog {

    public Window() {
        super();

        setTitle("Mandelbrot");
        setSize(1024, 700);
        setVisible(true);
        //setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        Model model = new Model();

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new Controls(model), BorderLayout.EAST);
        panel.add(new View(model), BorderLayout.CENTER);

        setContentPane(panel);
    }
}
