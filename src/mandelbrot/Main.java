package mandelbrot;

import mandelbrot.ui.Window;

import javax.swing.*;

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
                new Window();
            }
        });
    }

}
