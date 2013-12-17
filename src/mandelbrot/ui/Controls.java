package mandelbrot.ui;

import mandelbrot.core.Model;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;

public class Controls extends JPanel implements Observer, ActionListener,
    ChangeListener {

    // ==== Properties ====

    final private Model model;

    final private SpinnerNumberModel fpsModel = new SpinnerNumberModel(
        25, 1, 60, 1);
    final private JSpinner fpsSpinner = new JSpinner(
        new SpinnerNumberModel(25, 1, 60, 1));
    final private JSpinner maxIterSpinner = new JSpinner(
        new SpinnerNumberModel(1000, 0, 10000, 10));
    final private JButton bestFitButton = new JButton("Fit!");
    final private JProgressBar progressBar = new JProgressBar();

    // ==== Constructor ====

    public Controls(Model aModel) {
        super();

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        model = aModel;
        model.addObserver(this);

        fpsSpinner.addChangeListener(this);
        maxIterSpinner.addChangeListener(this);

        JPanel settings = new JPanel(new GridLayout(2, 2));
        settings.add(new JLabel("FPS:"));
        settings.add(fpsSpinner);
        settings.add(new JLabel("Max. iter.:"));
        settings.add(maxIterSpinner);
        add(settings);

        add(Box.createVerticalGlue());

        bestFitButton.addActionListener(this);
        add(bestFitButton);

        progressBar.setMinimum(0);
        progressBar.setMaximum(100);
        add(progressBar);
    }

    // ==== ActionListener Implementation ====

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == bestFitButton) {
            model.fit();
        }
    }

    // ==== ChangeListener Implementation

    @Override
    public void stateChanged(ChangeEvent e) {
        if (e.getSource() == fpsSpinner) {
            model.setFps((Integer) fpsSpinner.getModel().getValue());
        } else if (e.getSource() == maxIterSpinner) {
            model.setMaximumIterations(
                (Integer)maxIterSpinner.getModel().getValue());
        }
    }

    // ==== Observer Implementation ====

    @Override
    public void update(Observable o, Object arg) {
        if (o == model) {
            progressBar.setValue((int)(model.getProgress() * 100));
            fpsSpinner.getModel().setValue(model.getFps());
            maxIterSpinner.getModel().setValue(model.getMaximumIterations());
        }
    }

}
