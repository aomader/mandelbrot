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

        final Controls controls = new Controls(model);
        final View view = new View(model);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(controls, BorderLayout.EAST);
        panel.add(view, BorderLayout.CENTER);
        setContentPane(panel);

        pack();

        // ensure that the view updates the model size
        view.setVisible(true);
    }
}
