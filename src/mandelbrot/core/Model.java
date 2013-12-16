package mandelbrot.core;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.Observable;
import java.util.Vector;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;

public class Model extends Observable implements ActionListener {

    static private final int WINDOW_SIZE = 20;

    // ==== Properties ====

    private final Timer timer = new Timer(1000 / 25, this);
    private final int cores = Runtime.getRuntime().availableProcessors();
    private final Vector<Thread> threads = new Vector<Thread>(cores);

    private Point[] points;
    private int pointIndex;
    private final Object pointLock = new Object();

    private Point2D location = new Point2D.Double(-2.5, -1);
    private double scale = 1/200.;

    private BufferedImage image;
    private Graphics2D g2d;
    private WritableRaster raster;

    // ==== Accessors ====

    public synchronized Dimension getSize() {
        return new Dimension(this.image.getWidth(), this.image.getHeight());
    }

    public synchronized void setSize(Dimension dimension) {
        this.image = new BufferedImage(dimension.width, dimension.height, BufferedImage.TYPE_3BYTE_BGR);
        this.g2d = this.image.createGraphics();
        this.raster = this.image.getRaster();

        shufflePoints();

        draw();
    }

    public synchronized BufferedImage getImage() {
        return image;
    }

    // ==== ActionListener Implementation ====

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == timer) {
            setChanged();
            notifyObservers();


            boolean alive = false;
            for (Thread thread : threads) {
                if (thread.isAlive()) {
                    alive = true;
                    break;
                }
            }

            if (!alive) {
                timer.stop();
            }
        }
    }

    // ==== Private Helper Methods ====

    private void shufflePoints() {
        int width = (int)Math.ceil((double)image.getWidth() / WINDOW_SIZE);
        int height = (int)Math.ceil((double)image.getHeight() / WINDOW_SIZE);

        Point[] points = new Point[width * height];
        for (int i = 0; i < points.length; ++i) {
            points[i] = new Point(i % width, i / width);
        }

        for (int i = 0; i < points.length; ++i) {
            int j = (int)(Math.random() * points.length);
            Point t = points[i];
            points[i] = points[j];
            points[j] = t;
        }

        this.points = points;
        this.pointIndex = 0;
    }

    private void draw() {
        for (Thread thread : threads) {
            if (thread.isAlive()) {
                thread.interrupt();
            }
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {}
        }

        threads.removeAllElements();
        pointIndex = 0;

        for (int i = 0; i < cores; ++i) {
            Thread thread = new Thread(new Calculation());
            thread.start();
            threads.add(thread);
        }

        timer.start();
    }

    // ==== Calculation Task ====

    private class Calculation implements Runnable {
        @Override
        public void run() {
            final int width = image.getWidth();
            final int height = image.getHeight();

            while (!Thread.currentThread().isInterrupted()) {
                int index;

                synchronized (pointLock) {
                    if (pointIndex >= points.length) {
                        break;
                    }
                    index = pointIndex++;
                }

                final Point p = points[index];

                for (int x = p.x * WINDOW_SIZE, e = x + WINDOW_SIZE; x < e && x < width; ++x) {
                    for (int y = p.y * WINDOW_SIZE, f = y + WINDOW_SIZE; y < f && y < height; ++y) {
                        double mx = x * scale + location.getX();
                        double my = y * scale + location.getY();

                        int iter = Algorithm.escapeTime(mx, my, 10000);
                        int ia[] = new int[3];
                        ia[0] = iter % 256;
                        ia[1] = iter % 256;
                        ia[2] = iter % 256;
                        raster.setPixel(x, y, ia);
                    }
                }
            }
        }
    }
}
