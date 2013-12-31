package mandelbrot.core;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A helper class which bundles static methods that involve mathematical
 * computations and somehow more general algorithms.
 */
public class Algorithm {

    /**
     * A simple implementation of the escape time algorithm.
     *
     * @param x X-Coordinate within the mandelbrot space [-2.5, 1].
     * @param y Y-Coordinate within the mandelbrot space [-1, 1].
     * @param maxRadius Maximum radius above we consider the sequence outside.
     * @param maxIter Maximum number of iterations.
     * @return Number of used iterations.
     */
    public static int escapeTime(double x, double y, double maxRadius,
                                 int maxIter) {
        double x0 = x;
        double y0 = y;
        int iteration = 0;
        double maxRadiusSquared = maxRadius * maxRadius;

        // compute sequence terms until one "escapes"
        while (x*x + y*y < maxRadiusSquared && iteration < maxIter) {
            double xt = x*x - y*y + x0;
            double yt = 2*x*y + y0;

            x = xt;
            y = yt;

            iteration += 1;
        }

        return iteration;
    }

    /**
     * A simple implementation of the normalized iteration count algorithm.
     *
     * @param x X-Coordinate within the mandelbrot space [-2.5, 1].
     * @param y Y-Coordinate within the mandelbrot space [-1, 1].
     * @param maxRadius Maximum radius above we consider the sequence outside.
     * @param maxIter Maximum number of iterations.
     * @return Number of used iterations.
     */
    public static double normalizedIterationCount(double x, double y,
                                                  double maxRadius,
                                                  int maxIter) {
        double x0 = x;
        double y0 = y;
        double iteration = 0;
        double maxRadiusSquared = maxRadius * maxRadius;

        // compute sequence terms until one "escapes"
        while (x*x + y*y < maxRadiusSquared && iteration < maxIter) {
            double xt = x*x - y*y + x0;
            double yt = 2*x*y + y0;

            x = xt;
            y = yt;

            iteration += 1;
        }

        if (iteration < maxIter) {
            double zn_abs = Math.sqrt(x*x + y*y);
            double u = Math.log(Math.log(zn_abs) / Math.log(maxRadiusSquared)) /
                Math.log(2);
            iteration += 1 - Math.min(u, 1);
        }

        return Math.min(iteration, maxIter);
    }

    /**
     * Create a color palette indexed by iteration count by interpolating
     * tick values if necessary,
     *
     * @param mapping A mapping from normalized iteration count to ARGB color.
     * @param maxIter The maximum number of iterations.
     * @return A new color palette with maxIter + 1 items.
     */
    public static int[] createPalette(LinkedHashMap<Double, Integer> mapping,
                                      int maxIter) {
        double[] ticks = new double[mapping.size()];
        int[] colors = new int[mapping.size()];

        int i = 0, j = 0;

        for (Map.Entry<Double, Integer> entry : mapping.entrySet()) {
            ticks[i] = entry.getKey();
            colors[i] = entry.getValue();
            i += 1;
        }

        int[] palette = new int[maxIter + 1];

        for (i = 0; i <= maxIter; ++i) {
            double x = (double)i / maxIter;

            while (j < mapping.size() - 1 && x > ticks[j+1])
                j += 1;

            palette[i] = (x <= ticks[j] || j == mapping.size() - 1) ?
                colors[j] : interpolateColor(colors[j], colors[j+1],
                (x - ticks[j]) / (ticks[j+1] - ticks[j]));
        }

        return palette;
    }

    /**
     * Linearly interpolate between two colors.
     *
     * @param colorA The first color in ARGB format starting with the MSB.
     * @param colorB The second color in ARGB format starting with the MSB.
     * @param balance Balance between first and second color in range [0, 1].
     * @return The interpolated color.
     */
    public static int interpolateColor(int colorA, int colorB, double balance) {
        int r = ((colorA >> 16) & 0xFF) + (int)Math.round(((
            (colorB >> 16) & 0xFF) - ((colorA >> 16) & 0xFF)) * balance);
        int g = ((colorA >> 8) & 0xFF) + (int)Math.round(((
            (colorB >> 8) & 0xFF) - ((colorA >> 8) & 0xFF)) * balance);
        int b = (colorA & 0xFF) + (int)Math.round(((colorB & 0xFF) -
            (colorA & 0xFF)) * balance);
        return (0xFF << 24) | (r << 16) | (g << 8) | b;
    }

}
