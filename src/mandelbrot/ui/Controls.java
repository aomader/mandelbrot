package mandelbrot.ui;

import mandelbrot.core.Model;

import javax.swing.*;
import java.util.Observable;
import java.util.Observer;

public class Controls extends JPanel implements Observer {

    // ==== Properties ====

    final private Model model;

    final private JProgressBar progressBar = new JProgressBar();

    // ==== Constructor ====

    public Controls(Model aModel) {
        model = aModel;
        model.addObserver(this);

        progressBar.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        progressBar.setMinimum(0);
        progressBar.setMaximum(100);
        add(progressBar);
    }

    // ==== Observer Implementation ====

    @Override
    public void update(Observable o, Object arg) {
        if (o == model) {
            progressBar.setValue((int)(model.getProgress() * 100));
        }
    }

}
