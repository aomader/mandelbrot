# Mandelbrot

A simple but visually appealing and parallelized Java application to explore
the Mandelbrot set. For a simple but understandable explanation of what the
Mandelbrot set actually is, have a look at [this excellent blog post].

![snapshot of the GUI](http://reaktor42.de/~b52/public/mandelbrot.png)

## Features

The implementation is driven by some sort of MVC architecture inspired by
[Thread Watch] to separate the creating of the images from the UI related
parts. It's main features are listed below:

* Parallel algorithm in order to use the full potential of your n-core CPU
* Fully interactive navigation similar to Google Maps to easily explore the
  set
* Live feedback while rendering also showing intermediate results
* Many adjustable options like _algorithm_, _maximum number of iterations_,
  _maximum radius_, _histogram equalization_, _refresh rate_ and the
  _thread count_


[this excellent blog post]: http://yozh.org/mset_index/
[Thread Watch]: https://sites.google.com/site/drjohnbmatthews/threadwatch
