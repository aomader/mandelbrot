package mandelbrot.core;

public class Algorithm {

    /**
     * A simple implementation of the escape time algorithm.
     * @param x X-Coordinate within the mandelbrot space [-2.5, 1].
     * @param y Y-Coordinate within the mandelbrot space [-1, 1].
     * @param maxSquare Maximum squared sum.
     * @param maxIter Maximum number of iterations.
     * @return Number of used iterations.
     */
    public static int escapeTime(double x, double y, double maxSquare,
                                 int maxIter) {
        double x0 = x;
        double y0 = y;
        int iteration = 0;

        while (x*x + y*y < maxSquare && iteration < maxIter) {
            double xt = x*x - y*y + x0;
            double yt = 2*x*y + y0;

            x = xt;
            y = yt;

            iteration += 1;
        }

        return iteration;
    }

    // TODO: Add methods for color transformations!

}
