package mandelbrot.core;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.Observable;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;

public class ForkJoinModel extends Observable implements ActionListener {

    // ==== Properties ====

    private final Timer timer = new Timer(1000 / 30, this);
    private final ForkJoinPool pool = new ForkJoinPool();

    private Point2D location = new Point2D.Double(-2.5, -1);
    private double scale = 1/200.;

    private BufferedImage image;
    private Graphics2D g2d;
    private WritableRaster raster;
    private ForkJoinTask task;

    // ==== Accessors ====

    public synchronized Dimension getSize() {
        return new Dimension(this.image.getWidth(), this.image.getHeight());
    }

    public synchronized void setSize(Dimension dimension) {
        this.image = new BufferedImage(dimension.width, dimension.height, BufferedImage.TYPE_3BYTE_BGR);
        this.g2d = this.image.createGraphics();
        this.raster = this.image.getRaster();
        draw();
        timer.start();
    }

    public synchronized BufferedImage getImage() {
        return image;
    }

    // ==== ActionListener Implementation ====

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == timer) {
            setChanged();
            notifyObservers();

            if (task != null && task.isDone()) {
                timer.stop();
            }
        }
    }

    // ==== Private Helper Methods ====

    private void draw() {
        if (task != null && !task.isDone()) {
            task.cancel(true);
        }
        task = pool.submit(new Calculation(0, this.image.getWidth() - 1));
        timer.start();
    }

    // ==== Calculation Task ====

    private class Calculation extends RecursiveAction {
        private int left, right;

        public Calculation(int left, int right) {
            this.left = left;
            this.right = right;
        }

        @Override
        protected void compute() {
            if (right - left > 20) {
                int middle = left + (right - left)/2;
                invokeAll(new Calculation(left, middle - 1),
                          new Calculation(middle, right));
            } else {
                for (int x = left; x <= right; ++x) {
                    for (int y = 0; y < image.getHeight(); ++y) {
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
