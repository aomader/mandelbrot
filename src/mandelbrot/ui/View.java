package mandelbrot.ui;

import mandelbrot.core.Algorithm;

import javax.swing.*;
import java.awt.*;

public class View extends JComponent {
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        for (int x = 0; x < getWidth(); ++x) {
            for (int y = 0; y < getHeight(); ++y) {
                double x1 = (double)x/getWidth() * 3.5 - 2.5;
                double y1 = (double)y/getHeight() * 2 - 1;
                int iter = Algorithm.escapeTime(x1, y1, 1000);
                Color c = new Color(iter % 256, iter % 256, iter % 256);
                g.setColor(c);
                g.fillRect(x, y, 1, 1);
            }
        }
    }
}
