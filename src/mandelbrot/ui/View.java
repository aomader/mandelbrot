package mandelbrot.ui;

import mandelbrot.core.Model;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.Observable;
import java.util.Observer;

public class View extends JComponent implements Observer {

    // ==== Constants ====

    final private double ZOOM_FACTOR = .6;

    // ==== Properties ====

    final private Model model;

    // ==== Constructor ====

    public View(final Model aModel) {
        super();

        model = aModel;
        model.addObserver(this);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                // better use window resized event
                model.setSize(e.getComponent().getSize());
            }

            @Override
            public void componentShown(ComponentEvent e) {
                model.setSize(getSize());
                model.fit();
            }
        });

        addMouseListener(new MouseAdapter() {
            private Point pressed;

            @Override
            public void mousePressed(MouseEvent e) {
                pressed = e.getPoint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                model.show(new Rectangle(Math.min(pressed.x, e.getX()),
                                         Math.min(pressed.y, e.getY()),
                                         Math.abs(pressed.x - e.getX()),
                                         Math.abs(pressed.y - e.getY())));
            }
        });

        addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                final Dimension size = model.getSize();

                final double factor = e.getWheelRotation() < 0 ?
                    ZOOM_FACTOR : 1 / ZOOM_FACTOR;

                final double w = size.width * factor, h = size.height * factor;
                final int x = (int)Math.round((size.width - w) * e.getX() /
                    size.width);
                final int y = (int)Math.round((size.height - h) * e.getY() /
                    size.height);

                model.show(new Rectangle(x, y, (int)Math.round(w),
                    (int)Math.round(h)));
            }
        });
    }

    // ==== JComponent Overrides ====

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        BufferedImage image = model.getImage();
        g.drawImage(image, 0, 0, image.getWidth(), image.getHeight(), null);
    }

    // ==== Observer Implementation ====

    @Override
    public void update(Observable o, Object arg) {
        if (o == model) {
            repaint();
        }
    }
}
