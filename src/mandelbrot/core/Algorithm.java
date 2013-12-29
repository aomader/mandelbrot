package mandelbrot.core;

import java.awt.*;

public class Algorithm {

    /**
     * A simple implementation of the escape time algorithm.
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

    // TODO: Add methods for color transformations!

    public static Color colorFromIterations(int iter, int maxIter) {
        float v = (float)iter / maxIter;
        return new Color(Color.HSBtoRGB(0.95f + v, .6f, 1.f));
    }

}
