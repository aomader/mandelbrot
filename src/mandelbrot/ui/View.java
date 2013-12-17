package mandelbrot.ui;

import mandelbrot.core.Model;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.Observable;
import java.util.Observer;

public class View extends JComponent implements Observer {

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
                super.componentResized(e);
                model.setSize(e.getComponent().getSize());
            }

            @Override
            public void componentShown(ComponentEvent e) {
                super.componentShown(e);
                model.setSize(getSize());
                model.fit();
            }
        });

        addMouseListener(new MouseAdapter() {
            private Point pressed;

            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                pressed = e.getPoint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);
                model.show(new Rectangle(Math.min(pressed.x, e.getX()),
                                         Math.min(pressed.y, e.getY()),
                                         Math.abs(pressed.x - e.getX()),
                                         Math.abs(pressed.y - e.getY())));
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
