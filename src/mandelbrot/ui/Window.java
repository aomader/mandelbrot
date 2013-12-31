package mandelbrot.ui;

import mandelbrot.core.Model;
import mandelbrot.ui.locale.Localization;

import javax.swing.*;
import java.awt.*;

/**
 * The actual Window of the application containing the view and the controls.
 *
 * The Window creates an instance of {@code Model}, which it uses to create
 * and show instances of {@code View} and {@code Controls}. Thus it's sort
 * of a container for the smaller pieces.
 */
public class Window extends JFrame {

    /**
     * Create a new hidden Window that can be shown.
     */
    public Window() {
        super();

        setTitle(Localization.get("main.title"));
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        final Model model = new Model();

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new Controls(model), BorderLayout.EAST);
        panel.add(new View(model), BorderLayout.CENTER);
        setContentPane(panel);

        pack();
    }
}
