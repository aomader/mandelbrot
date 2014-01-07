package mandelbrot;

import mandelbrot.ui.Window;

import javax.swing.*;

/**
 * Provides the main entry point of the application.
 *
 * Creates a new {@code Window} within the UI thread and shows it.
 */
public class Main {

    public static void main(String[] args) {
        // try to use system specific look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Unable to set system specific look and feel");
        }

        // create the window within the UI-thread
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                final Window window = new Window();
                window.setVisible(true);
                window.setExtendedState(window.getExtendedState() | JFrame.MAXIMIZED_BOTH);
            }
        });
    }

}
