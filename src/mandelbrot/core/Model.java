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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class Model extends Observable implements ActionListener {

    // ==== Constants ====

    /**
     * Escape Time Algorithm to compute the iteration count bounded by
     * maxRadius and maxIterations.
     */
    public static final int ALGORITHM_ESCAPE_TIME = 0;

    /**
     * Normalized Iteration Count Algorithm to compute the iteration count
     * bounded by maxRadius and maxIterations.
     */
    public static final int ALGORITHM_NORMALIZED_ITERATION_COUNT = 1;

    // ==== Properties ====

    private final GraphicsConfiguration config =
        GraphicsEnvironment.getLocalGraphicsEnvironment().
            getDefaultScreenDevice().getDefaultConfiguration();

    private final Timer timer = new Timer(1000, this);
    private final int cores = Runtime.getRuntime().availableProcessors();
    private final Vector<Thread> threads = new Vector<Thread>(cores);
    private CountDownLatch firstRun;

    private int[] indexes;
    private double[] iterations;
    private AtomicInteger[] histogram;
    private final AtomicInteger index = new AtomicInteger();
    private final AtomicInteger processed = new AtomicInteger();

    private boolean active = true;
    private Point2D location = new Point2D.Double(-2.5, -1);
    private double scale = 1/200.;
    private int fps = 25;
    private int algorithm = ALGORITHM_NORMALIZED_ITERATION_COUNT;
    private int maxIter = 1000;
    private double maxRadius = 2;
    private boolean histEqualization = true;
    private long renderingTime = 0;
    private long renderingStart = 0;

    private int[] palette;
    private BufferedImage image;

    private static LinkedHashMap<Double, Integer> colors;

    static {
        colors = new LinkedHashMap<Double, Integer>();
        colors.put(0., 0xff000000);
        colors.put(.1, 0xff4b0d57);
        colors.put(.4, 0xffbb4330);
        colors.put(.6, 0xfffaa303);
        colors.put(.8, 0xffffe248);
        colors.put(.9, 0xffffed93);
        colors.put(0.95, 0xffffffff);
        colors.put(1., 0xff000000);
    }

    // ==== Constructor ====

    public Model() {
        super();
        setActive(false);
        setSize(new Dimension(1, 1));
        setFps(25);
        setMaxIterations(200);
        setMaxRadius(10);
        setActive(true);
    }

    // ==== Accessors ====

    /**
     * Get the image the algorithm renders to. It might show a not completely
     * rendered versions.
     *
     * @return The rendered image.
     */
    public synchronized final BufferedImage getImage() {
        return image;
    }

    /**
     * Whether the model shall re-render the image if necessary, e.g. caused
     * by a call to {@code show()}.
     *
     * @return The current active state.
     * @see #setActive(boolean)
     */
    public synchronized final boolean isActive() {
        return active;
    }

    /**
     * Set the re-render state.
     *
     * @param active The new re-render behavior.
     * @see #isActive()
     */
    public synchronized void setActive(boolean active) {
        stopDrawing();
        this.active = active;
        startDrawing();
    }

    /**
     * Get the size of the created image.
     *
     * @return The size of the image.
     * @see #setSize(java.awt.Dimension)
     */
    public synchronized final Dimension getSize() {
        return new Dimension(this.image.getWidth(), this.image.getHeight());
    }

    /**
     * Set's the size of the rendered image, triggers a redraw if necessary.
     *
     * @param size The new size of the image.
     * @see #getSize()
     */
    public synchronized void setSize(Dimension size) {
        if (image == null || size.width != image.getWidth() ||
            size.height != image.getHeight()) {
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
    }

    /**
     * Get the frames per second or better the amount of update notifications
     * per second.
     *
     * @return The frames per second while rendering.
     * @see #setFps(int)
     */
    public synchronized final int getFps() {
        return fps;
    }

    /**
     * Set the frames per second.
     *
     * @param fps The new frames per second.
     * @see #getFps()
     */
    public synchronized void setFps(int fps) {
        this.fps = fps;
        timer.setDelay(1000 / fps);
        timer.setInitialDelay(1000 / fps);
    }

    /**
     * Get the currently used algorithm to calculate the sequence and thus the
     * iteration count.
     *
     * @return The used algorithm.
     */
    public synchronized final int getAlgorithm() {
        return algorithm;
    }

    /**
     * Set the algorithm used to calculate the sequence and thus the
     * iteration count. Triggers also a redraw.
     * You must specify one of the following choices:
     *
     * <p>
     * <ul>
     * <li>{@code ALGORITHM_ESCAPE_TIME}
     * (defined in {@code Model}):
     * Uses the very simple but frequently used Escape Time Algorithm. This
     * might introduce visual banding and thus reducing the appeal of the image.
     *
     * <li>{@code ALGORITHM_NORMALIZED_ITERATION_COUNT}
     * (defined in {@code Model}):
     * Similar to {@code ALGORITHM_ESCAPE_TIME} but with the addition of
     * reducing the visual banding by applying some sort of logarithmic
     * smoothing.
     * </ul>
     *
     * @param algorithm The algorithm which should be used.
     * @see #getAlgorithm()
     * @see Model
     */
    public synchronized void setAlgorithm(int algorithm) {
        if (algorithm != ALGORITHM_ESCAPE_TIME &&
            algorithm != ALGORITHM_NORMALIZED_ITERATION_COUNT) {
            throw new IllegalArgumentException("algorithm must be one of: " +
                "ALGORITHM_ESCAPE_TIME, ALGORITHM_NORMALIZED_ITERATION_COUNT");
        }

        if (this.algorithm != algorithm) {
            stopDrawing();
            this.algorithm = algorithm;
            startDrawing();
        }
    }

    /**
     * Get the number of maximally used iterations to determine whether a point
     * "escaped" or not.
     *
     * @return The number of maximal iterations.
     */
    public synchronized int getMaxIterations() {
        return maxIter;
    }

    /**
     * Set the number of maximal iterations.
     *
     * @param maxIter The new number of maximal iterations.
     */
    public synchronized void setMaxIterations(int maxIter) {
        if (maxIter < 0) {
            throw new IllegalArgumentException("maxIter must be equal or " +
                "larger than 0");
        }

        if (this.maxIter != maxIter) {
            stopDrawing();
            this.maxIter = maxIter;

            palette = Algorithm.createPalette(colors, maxIter);

            histogram = new AtomicInteger[maxIter + 1];
            for (int i = 0; i <= maxIter; ++i) {
                histogram[i] = new AtomicInteger();
            }

            startDrawing();
        }
    }

    /**
     * Get the maximal radius around the origin after which a point is
     * considered "escaped".
     *
     * @return The maximal escape radius.
     */
    public synchronized double getMaxRadius() {
        return maxRadius;
    }

    /**
     * Set the maximal escape radius. Triggers also a redraw.
     *
     * @param maxRadius The new radius.
     */
    public synchronized void setMaxRadius(double maxRadius) {
        stopDrawing();
        this.maxRadius = maxRadius;
        startDrawing();
    }

    /**
     * Determine whether histogram equalization is used or not.
     *
     * @return The state of the usage.
     */
    public synchronized boolean getHistEqualization() {
        return histEqualization;
    }

    /**
     * Set the usage state of histogram equalization as a post processing
     * method in order to enhance the color usage.
     *
     * @param histEqualization The new usage state.
     */
    public synchronized void setHistEqualization(boolean histEqualization) {
        if (this.histEqualization != histEqualization) {
            stopDrawing();
            this.histEqualization = histEqualization;
            startDrawing();
        }
    }

    /**
     * Get the progress of the last rendering attempt in the range [0.f, 1.f].
     *
     * @return The progress of the last rendering attempt.
     */
    public synchronized float getProgress() {
        return Math.min(1.f, (float)processed.get() / indexes.length /
            (histEqualization ? 2 : 1));
    }

    /**
     * Get the time needed to render the current image. The value is only
     * meaningful if a call to {@code getProgress()} returns 1.f.
     *
     * @return The rendering time in milliseconds.
     */
    public synchronized long getRenderingTime() {
        return renderingTime;
    }

    // ==== Public Methods

    /**
     * Updates the location and the scale such that the rectangle is shown
     * best.
     *
     * @param rectangle The region to show in image coordinates.
     * @see #fit()
     * @see #translate(int, int)
     * @see #scale(int, int, double)
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
     *
     * @see #show(java.awt.Rectangle)
     */
    public synchronized void fit() {
        stopDrawing();

        location = new Point2D.Double(-2.5, -1);
        scale = 1/200.;

        show(new Rectangle(0, 0, (int)(3.5/scale), (int)(2./scale)));
    }

    /**
     * Convenience method to zoom in/out of a certain point given a scale.
     *
     * @param x The x-coordinate of the anchor point in image coordinates.
     * @param y The y-coordinate of the anchor point in image coordinates.
     * @param scale Multiplied with the old size to determine the new one.
     * @see #show(java.awt.Rectangle)
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
     *
     * @param dx The x-translation in image coordinates.
     * @see #show(java.awt.Rectangle)
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

        iterations = new double[total];

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
            firstRun = new CountDownLatch(cores);
            index.set(0);
            processed.set(0);

            // set histogram bins to zero
            if (histEqualization) {
                for (int i = 0; i <= maxIter; ++i) {
                    histogram[i].set(0);
                }
            }

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
        @Override
        public void run() {
            final int width = image.getWidth();
            final int total = indexes.length;

            // consume pixels until exhausted or thread is interrupted
            while (!currentThread().isInterrupted()) {
                // get next pixel candidate
                final int idx = index.getAndIncrement();

                // wait until first run is completed before continuing
                if (idx >= total) {
                    firstRun.countDown();

                    while (firstRun.getCount() > 0) {
                        try {
                            firstRun.await();
                        } catch (InterruptedException e) {}
                    }

                    break;
                }

                // 1D to 2D coordinates
                final int xy = indexes[idx];
                final int x = xy % width;
                final int y = xy / width;

                // map coordinates into Mandelbrot space
                final double mx = x * scale + location.getX();
                final double my = y * scale + location.getY();

                // the actual time consuming computation
                final double iter = (algorithm == ALGORITHM_ESCAPE_TIME) ?
                    Algorithm.escapeTime(mx, my, maxRadius, maxIter) :
                    Algorithm.normalizedIterationCount(mx, my, maxRadius,
                                                       maxIter);
                final double fraction = iter % 1;

                // set color by either interpolating or using nearest one
                final int color = (fraction < 5.96e-8) ?
                    palette[(int)Math.round(iter)] :
                    Algorithm.interpolateColor(palette[(int)Math.floor(iter)],
                                               palette[(int)Math.ceil(iter)],
                                               fraction);
                image.setRGB(x, y, color);

                // if hist. equalization enabled, store some values for second
                // run which adjusts the colors a second time
                if (histEqualization) {
                    iterations[idx] = iter;
                    histogram[(int)Math.floor(iter)].incrementAndGet();
                    histogram[(int)Math.ceil(iter)].incrementAndGet();
                }

                processed.incrementAndGet();
            }

            // if hist. equalization enabled, perform second run for coloring
            if (histEqualization) {
                while (!currentThread().isInterrupted()) {
                    final int idx = index.getAndDecrement();

                    if (idx >= total) {
                        continue;
                    }
                    if (idx < 0) {
                        break;
                    }

                    final double iter = iterations[idx];
                    final double fraction = iter % 1;

                    double rel = 0.;
                    int j = 0;
                    while (j <= Math.floor(iter)) {
                        rel += histogram[j++].get();
                    }
                    rel /= total*2;

                    int color;
                    if (fraction < 5.96e-8) {
                        color = palette[(int)Math.round(rel * maxIter)];
                    } else {
                        final double prel = rel - histogram[j-1].get() /
                            (total * 2.);
                        final int a = palette[(int)Math.round(prel * maxIter)];
                        final int b = palette[(int)Math.round(rel * maxIter)];
                        color = Algorithm.interpolateColor(a, b, fraction);
                    }

                    final int xy = indexes[idx];
                    image.setRGB(xy % width, xy / width, color);

                    processed.incrementAndGet();
                }
            }

            // update rendering time
            renderingTime = System.currentTimeMillis() -
                renderingStart;
        }
    }
}
