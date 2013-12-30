package mandelbrot.ui;

import mandelbrot.core.Model;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.Observable;
import java.util.Observer;

public class View extends JComponent implements Observer, ActionListener {

    // ==== Constants ====

    private final double ZOOM_FACTOR = .6;
    private final double PAN_THRESHOLD = 8.;

    // ==== Properties ====

    private final Model model;

    private final Timer timer = new Timer(250, this);

    // ==== Constructor ====

    public View(final Model aModel) {
        super();

        setPreferredSize(new Dimension((int)(1.25 * 800), 800));

        model = aModel;
        model.addObserver(this);

        timer.setRepeats(false);

        // resizing
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                timer.restart();
            }

            @Override
            public void componentShown(ComponentEvent e) {
                model.setSize(getSize());
                model.fit();
            }
        });

        // zooming and panning
        MouseAdapter mouseAdapter = new MouseAdapter() {
            private Point pressed;
            private boolean panning;

            @Override
            public void mousePressed(MouseEvent e) {
                pressed = e.getPoint();
                panning = false;
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (panning) {
                    model.setActive(true);
                }
            }

            // panning
            @Override
            public void mouseDragged(MouseEvent e) {
                if (!panning) {
                    double d = Math.sqrt(Math.pow(pressed.getX() - e.getX(), 2)
                        + Math.pow(pressed.getY() - e.getY(), 2));

                    if (d >= PAN_THRESHOLD) {
                        panning = true;
                        pressed = e.getPoint();
                        model.setActive(false);
                    }

                    return;
                }

                model.translate(pressed.x - e.getX(), pressed.y - e.getY());
                pressed = e.getPoint();
            }

            // zoom on double click
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    model.scale(e.getX(), e.getY(),
                        e.getButton() == MouseEvent.BUTTON1 ? ZOOM_FACTOR :
                        1 / ZOOM_FACTOR);
                }
            }

            // zoom through mouse wheel movement
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                model.scale(e.getX(), e.getY(), e.getWheelRotation() < 0 ?
                    ZOOM_FACTOR : 1 / ZOOM_FACTOR);
            }
        };

        addMouseListener(mouseAdapter);
        addMouseMotionListener(mouseAdapter);
        addMouseWheelListener(mouseAdapter);
    }

    // ==== JComponent Overrides ====

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        BufferedImage image = model.getImage();
        int w = Math.min(image.getWidth(), getWidth()),
            h = Math.min(image.getHeight(), getHeight());
        g.drawImage(image, 0, 0, w, h, 0, 0, w, h, null);
    }

    // ==== Observer Implementation ====

    @Override
    public void update(Observable o, Object arg) {
        if (o == model) {
            repaint();
        }
    }

    // ==== ActionListener Implementation

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == timer) {
            model.setSize(getSize());
        }
    }

}
