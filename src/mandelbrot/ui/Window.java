package mandelbrot.ui;

import mandelbrot.core.Model;
import mandelbrot.ui.locale.Localization;

import javax.swing.*;
import java.awt.*;

public class Window extends JFrame {

    public Window() {
        super();

        setTitle(Localization.get("main.title"));
        setSize(1024, 700);
        setVisible(true);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        final Model model = new Model();

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new Controls(model), BorderLayout.EAST);
        panel.add(new View(model), BorderLayout.CENTER);
        setContentPane(panel);
    }
}
