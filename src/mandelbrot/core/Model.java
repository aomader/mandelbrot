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
import java.util.concurrent.atomic.AtomicInteger;

public class Model extends Observable implements ActionListener {

    // ==== Properties ====

    private final GraphicsConfiguration config =
        GraphicsEnvironment.getLocalGraphicsEnvironment().
            getDefaultScreenDevice().getDefaultConfiguration();

    private final Timer timer = new Timer(1000, this);
    private final int cores = Runtime.getRuntime().availableProcessors();
    private final Vector<Thread> threads = new Vector<Thread>(cores);

    private int[] indexes;
    private AtomicInteger index = new AtomicInteger();

    private Point2D location = new Point2D.Double(-2.5, -1);
    private double scale = 1/200.;
    private int fps = 25;
    private int maxIter = 1000;

    private BufferedImage image;
    private WritableRaster raster;

    // ==== Constructor ====

    public Model() {
        super();
        setSize(new Dimension(1, 1));
        setFps(25);
    }

    // ==== Accessors ====

    /**
     * Get the size of the created image.
     * @return The size of the image.
     */
    public synchronized Dimension getSize() {
        return new Dimension(this.image.getWidth(), this.image.getHeight());
    }

    /**
     * Set's the size of the rendered image, triggers a redraw if necessary.
     * @param size The new size of the image.
     */
    public synchronized void setSize(Dimension size) {
        stopDrawing();

        BufferedImage newImage = config.createCompatibleImage(size.width,
            size.height, Transparency.OPAQUE);

        // copy over already rendered parts
        if (image != null) {
            newImage.getGraphics().drawImage(image, 0, 0, image.getWidth(),
                                             image.getHeight(), null);
        }

        image = newImage;
        raster = image.getRaster();

        updateIndexes();

        startDrawing();
    }

    public synchronized int getFps() {
        return fps;
    }

    public synchronized void setFps(int fps) {
        this.fps = fps;
        timer.setDelay(1000 / fps);
        timer.setInitialDelay(1000 / fps);
    }

    public synchronized int getMaximumIterations() {
        return maxIter;
    }

    public synchronized void setMaximumIterations(int maxIter) {
        stopDrawing();
        this.maxIter = maxIter;
        startDrawing();
    }

    /**
     * Get the image the algorithm renders to. It might show not completely
     * rendered versions.
     * @return The rendered image.
     */
    public synchronized BufferedImage getImage() {
        return image;
    }

    /**
     * Get the progress of the last rendering attempt in the range [0.f, 1.f].
     * @return The progress of the last rendering attempt.
     */
    public synchronized float getProgress() {
        return Math.min(1.f, (float)index.get() / indexes.length);
    }

    // ==== Public Methods

    /**
     * Updates the location and the scale such that the rectangle is shown
     * best.
     * @param rectangle The region to show in image coordinates.
     */
    public synchronized void show(Rectangle rectangle) {
        stopDrawing();

        final double ratio = (double)image.getWidth() / image.getHeight();

        // ensure that everything within rect is shown
        if ((double)rectangle.width / rectangle.height > ratio) {
            final double delta = (rectangle.width / ratio - rectangle.height);
            rectangle.y -= delta / 2.;
            rectangle.height += delta;
        } else {
            final double delta = (rectangle.height * ratio - rectangle.width);
            rectangle.x -= delta / 2.;
            rectangle.width += delta;
        }

        // update Mandelbrot coordinates
        location.setLocation(location.getX() + rectangle.x * scale,
                             location.getY() + rectangle.y * scale);
        scale = rectangle.width * scale / image.getWidth();

        // scale image region to provide a fast yet not sharp preview
        BufferedImage s = config.createCompatibleImage(rectangle.width,
                                                       rectangle.height);
        s.getGraphics().drawImage(image, 0, 0, rectangle.width,
                                  rectangle.height, rectangle.x, rectangle.y,
                                  rectangle.x + rectangle.width,
                                  rectangle.y + rectangle.height, null);
        image.getGraphics().drawImage(s, 0, 0, image.getWidth(),
                                      image.getHeight(), null);

        startDrawing();
    }

    /**
     * Update location and scale such that the whole Mandelbrot space
     * is shown best.
     */
    public synchronized void fit() {
        stopDrawing();

        location = new Point2D.Double(-2.5, -1);
        scale = 1/200.;

        show(new Rectangle(0, 0, (int)(3.5/scale), (int)(2./scale)));
    }

    // ==== ActionListener Implementation ====

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == timer) {
            setChanged();
            notifyObservers();

            for (Thread thread : threads) {
                if (thread.isAlive()) {
                    return;
                }
            }

            // stop the timer if all threads are finished
            timer.stop();
        }
    }

    // ==== Private Helper Methods ====

    private void updateIndexes() {
        final int total = image.getWidth() * image.getHeight();

        // create increasing pixel indexes
        indexes = new int[total];
        for (int i = 0; i < total; ++i)
            indexes[i] = i;

        // apply Knuth shuffle for random permutation
        for (int i = 0; i < total; ++i) {
            int j = (int)(Math.random() * total);

            int t = indexes[i];
            indexes[i] = indexes[j];
            indexes[j] = t;
        }

        index.set(0);
    }

    private void stopDrawing() {
        // tell all threads to stop
        for (Thread thread : threads) {
            thread.interrupt();
        }

        // wait for all threads to finish
        for (Thread thread : threads) {
            while (thread.isAlive()) {
                try {
                    thread.join();
                } catch (InterruptedException e) {}
            }
        }

        threads.removeAllElements();

        timer.stop();
    }

    private void startDrawing() {
        index.set(0);

        // spawn new threads to perform the calculations
        for (int i = 0; i < cores; ++i) {
            Thread thread = new Calculation();
            thread.start();
            threads.add(thread);
        }

        timer.start();
    }

    // ==== Calculation Task ====

    private class Calculation extends Thread {
        private int[] pixel = new int[3];

        @Override
        public void run() {
            final int width = image.getWidth();
            final int total = indexes.length;

            // consume pixels until exhausted or thread is interrupted
            while (!Thread.currentThread().isInterrupted()) {
                // get next pixel candidate
                final int idx = index.getAndIncrement();

                // stop when all pixels are consumed
                if (idx >= total) {
                    break;
                }

                // 1D to 2D coordinates
                final int xy = indexes[idx];
                final int x = xy % width;
                final int y = xy / width;

                // map coordinates into Mandelbrot space
                final double mx = (xy % width) * scale + location.getX();
                final double my = (xy / width) * scale + location.getY();

                // the actual time consuming computation
                final int iter = Algorithm.escapeTime(mx, my, 4, maxIter);

                /* TODO: The arrangement, e.g. RGB, BGR, etc. is defined by
                         the image, we have to look that up. */

                // set the new color of the pixel
                pixel[0] = (int)((float)iter/255 * 255);
                pixel[1] = pixel[0];
                pixel[2] = pixel[0];
                raster.setPixel(x, y, pixel);
            }
        }
    }
}
