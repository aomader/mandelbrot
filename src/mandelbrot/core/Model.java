package mandelbrot.core;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.LinkedHashMap;
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

    public boolean active = true;
    private Point2D location = new Point2D.Double(-2.5, -1);
    private double scale = 1/200.;
    private int fps = 25;
    private int maxIter = 1000;
    private double maxRadius = 2;
    private long renderingTime = 0;
    private long renderingStart = 0;

    private int[] palette;
    private BufferedImage image;

    private static LinkedHashMap<Double, Integer> colors;

    static {
        colors = new LinkedHashMap<Double, Integer>();
        colors.put(0., 0xff004183);
        colors.put(.1, 0xffffffff);
        colors.put(.5, 0xffff7200);
        colors.put(.75, 0xff3ef000);
        colors.put(1., 0xff000000);
    }

    // ==== Constructor ====

    public Model() {
        super();
        setActive(false);
        setSize(new Dimension(1, 1));
        setFps(25);
        setMaxIterations(100);
        setMaxRadius(10);
        setActive(true);
    }

    // ==== Accessors ====

    /**
     * Get the image the algorithm renders to. It might show a not completely
     * rendered versions.
     * @return The rendered image.
     */
    public synchronized BufferedImage getImage() {
        return image;
    }

    /**
     * Whether the model shall re-render the image if necessary, e.g. caused
     * by a call to {@code show()}.
     * @return The current active state.
     */
    public synchronized boolean getActive() {
        return active;
    }

    /**
     * Set the re-render state.
     * @param active The new re-render behavior.
     */
    public synchronized void setActive(boolean active) {
        stopDrawing();
        this.active = active;
        startDrawing();
    }

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

        updateIndexes();

        startDrawing();
    }

    /**
     * Get the frames per second or better the amount of update notifications
     * per second.
     * @return The frames per second while rendering.
     */
    public synchronized int getFps() {
        return fps;
    }

    /**
     * Set the frames per second.
     * @param fps The new frames per second.
     */
    public synchronized void setFps(int fps) {
        this.fps = fps;
        timer.setDelay(1000 / fps);
        timer.setInitialDelay(1000 / fps);
    }

    /**
     * Get the number of maximally used iterations to determine whether a point
     * "escaped" or not.
     * @return The number of maximal iterations.
     */
    public synchronized int getMaxIterations() {
        return maxIter;
    }

    /**
     * Set the number of maximal iterations.
     * @param maxIter The new number of maximal iterations.
     */
    public synchronized void setMaxIterations(int maxIter) {
        stopDrawing();
        this.maxIter = maxIter;
        palette = Algorithm.createPalette(colors, maxIter);
        startDrawing();
    }

    /**
     * Get the maximal radius around the origin after which a point is
     * considered "escaped".
     * @return The maximal escape radius.
     */
    public synchronized double getMaxRadius() {
        return maxRadius;
    }

    /**
     * Set the maximal escape radius. Triggers also a redraw.
     * @param maxRadius The new radius.
     */
    public synchronized void setMaxRadius(double maxRadius) {
        stopDrawing();
        this.maxRadius = maxRadius;
        startDrawing();
    }

    /**
     * Get the progress of the last rendering attempt in the range [0.f, 1.f].
     * @return The progress of the last rendering attempt.
     */
    public synchronized float getProgress() {
        return Math.min(1.f, (float)index.get() / indexes.length);
    }

    /**
     * Get the needed time to render the current image. The value is only
     * meaningful if a call to {@code getProgress()} returns 1.f.
     * @return The rendering time in milliseconds.
     */
    public synchronized long getRenderingTime() {
        return renderingTime;
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

        setChanged();
        notifyObservers();

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

    /**
     * Convenience method to zoom in/out of a certain point given a scale.
     * @param x The x-coordinate of the anchor point in image coordinates.
     * @param y The y-coordinate of the anchor point in image coordinates.
     * @param scale Multiplied with the old size to determine the new one.
     */
    public synchronized void scale(int x, int y, double scale) {
        final int width = image.getWidth(), height = image.getHeight();

        final double w = width * scale;
        final double h = height * scale;
        final int nx = (int)Math.round((width - w) * x / width);
        final int ny = (int)Math.round((height - h) * y / height);

        show(new Rectangle(nx, ny, (int) Math.round(w), (int) Math.round(h)));
    }

    /**
     * Convenience method to move the view area by a certain delta.
     * @param dx The x-translation in image coordinates.
     */
    public synchronized void translate(int dx, int dy) {
        show(new Rectangle(dx, dy, image.getWidth(), image.getHeight()));
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
        if (active) {
            index.set(0);

            // spawn new threads to perform the calculations
            for (int i = 0; i < cores; ++i) {
                Thread thread = new Calculation();
                thread.start();
                threads.add(thread);
            }

            timer.start();

            renderingStart = System.currentTimeMillis();
        }
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

                // last thread without a task updates the rendering time,
                // that's not quite correct, since some threads might still
                // do something, but that is negligible
                if (idx == total) {
                    renderingTime = System.currentTimeMillis() - renderingStart;
                }

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
                final double iter = Algorithm.normalizedIterationCount(mx, my,
                    maxRadius, maxIter);

                int color = (iter == maxIter) ? palette[maxIter] :
                    Algorithm.interpolateColor(palette[(int)Math.floor(iter)],
                        palette[(int)Math.ceil(iter)], iter % 1);

                image.setRGB(x, y, color);
                /*
                final int iter = Algorithm.escapeTime(mx, my, maxRadius,
                    maxIter);
                image.setRGB(x, y, palette[iter]);
                */
            }
        }
    }
}
