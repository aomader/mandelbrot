package mandelbrot.core;

public class Algorithm {
    /**
     * A simple implementation of the escape time algorithm.
     *
     * @param x X-Coordinate within the mandelbrot space [-2.5, 1].
     * @param y Y-Coordinate within the mandelbrot space [-1, 1].
     * @param max Maximum number of iterations.
     * @return Number of used iterations.
     */
    public static int escapeTime(double x, double y, int max) {
        double x0 = x;
        double y0 = y;
        int iteration = 0;

        while (x*x + y*y < 4 && iteration < max) {
            double xt = x*x - y*y + x0;
            double yt = 2*x*y + y0;

            x = xt;
            y = yt;

            iteration += 1;
        }

        return iteration;
    }
}
