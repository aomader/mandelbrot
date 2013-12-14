package mandelbrot.ui;

import mandelbrot.core.Algorithm;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

public class View extends JComponent {
    /** The coordinate within the Mandelbrot space aligned to the upper left corner, */
    private Point2D location = new Point2D.Double(-2.5, -1);

    /** Mandelbrot units per pixel. */
    private double scale = 1/200.;

    public View() {
        super();

        addMouseListener(new MouseAdapter() {
            private Point pressed;

            @Override
            public void mousePressed(MouseEvent e) {
                pressed = e.getPoint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                double distance = Math.sqrt(Math.pow(pressed.getX() - e.getX(), 2) + Math.pow(pressed.getY() - e.getY(), 2));

                if (distance > 50.f) {
                    int x = Math.min(pressed.x, e.getX());
                    int y = Math.min(pressed.y, e.getY());

                    location.setLocation(location.getX() + x*scale, location.getY() + y*scale);

                    double dx = Math.abs(pressed.x - e.getX());
                    double dy = Math.abs(pressed.y - e.getY());

                    scale *= Math.max(dx / getWidth(), dy / getWidth());
                }

                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int width = getWidth();
        int height = getHeight();

        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                double mx = x * scale + location.getX();
                double my = y * scale + location.getY();

                int iter = Algorithm.escapeTime(mx, my, 1000);
                Color c = new Color(iter % 256, iter % 256, iter % 256);
                g.setColor(c);
                g.fillRect(x, y, 1, 1);
            }
        }
    }
}
