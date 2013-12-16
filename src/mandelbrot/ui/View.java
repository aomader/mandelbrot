package mandelbrot.ui;

import mandelbrot.core.ForkJoinModel;
import mandelbrot.core.Model;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.Observable;
import java.util.Observer;

public class View extends JComponent implements Observer {

    // ==== Properties ====

    private Model model;

    // ==== Constructor ====

    public View() {
        super();

        model = new Model();
        model.addObserver(this);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);
                model.setSize(e.getComponent().getSize());
            }
        });

        /*
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

                    location.setLocation(location.getX() + x * scale, location.getY() + y * scale);

                    double dx = Math.abs(pressed.x - e.getX());
                    double dy = Math.abs(pressed.y - e.getY());

                    scale *= Math.max(dx / getWidth(), dy / getWidth());
                }

                repaint();
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                super.mouseWheelMoved(e);
            }
        });
        */
    }

    // ==== JComponent Overrides ====

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        BufferedImage image = model.getImage();
        if (image != null) {
            g.drawImage(image, 0, 0, image.getWidth(), image.getHeight(), null);
        }
    }

    // ==== Observer Overrides ====

    @Override
    public void update(Observable o, Object arg) {
        if (o == model) {
            repaint();
        }
    }
}
