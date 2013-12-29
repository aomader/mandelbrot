package mandelbrot.ui;

import mandelbrot.core.Model;
import mandelbrot.ui.locale.Localization;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;

public class Controls extends Box implements Observer, ActionListener,
    ChangeListener {

    // ==== Properties ====

    final private Model model;

    final private JSpinner fpsSpinner = new JSpinner(
        new SpinnerNumberModel(25, 1, 60, 1));
    final private JSpinner maxIterSpinner = new JSpinner(
        new SpinnerNumberModel(1000, 0, 10000, 10));
    final private JSpinner maxRadiusSpinner = new JSpinner(
        new SpinnerNumberModel(2, 0, 100, 0.1));
    final private JButton leftButton = createControlButton("main.left");
    final private JButton rightButton = createControlButton("main.right");
    final private JButton upButton = createControlButton("main.up");
    final private JButton downButton = createControlButton("main.down");
    final private JButton inButton = createControlButton("main.in");
    final private JButton outButton = createControlButton("main.out");
    final private JButton fitButton = createControlButton("main.fit");
    final private JLabel renderingLabel = new JLabel();
    final private JProgressBar progressBar = new JProgressBar();

    // ==== Constructor ====

    public Controls(Model aModel) {
        super(BoxLayout.Y_AXIS);

        setMaximumSize(new Dimension(300, Integer.MAX_VALUE));
        setBorder(new EmptyBorder(15, 15, 15, 15));

        model = aModel;
        model.addObserver(this);

        // add listeners
        fpsSpinner.addChangeListener(this);
        maxIterSpinner.addChangeListener(this);
        maxRadiusSpinner.addChangeListener(this);
        leftButton.addActionListener(this);
        rightButton.addActionListener(this);
        upButton.addActionListener(this);
        downButton.addActionListener(this);
        fitButton.addActionListener(this);

        // settings
        addSetting("main.fps", fpsSpinner);
        add(Box.createRigidArea(new Dimension(0, 30)));
        addSetting("main.iter", maxIterSpinner);
        add(Box.createRigidArea(new Dimension(0, 30)));
        addSetting("main.radius", maxRadiusSpinner);
        add(Box.createRigidArea(new Dimension(0, 30)));

        // controls
        JPanel moving = new JPanel(new GridLayout(3, 3, 2, 2));
        moving.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        moving.add(outButton);
        moving.add(upButton);
        moving.add(inButton);
        moving.add(leftButton);
        moving.add(fitButton);
        moving.add(rightButton);
        moving.add(createGlue());
        moving.add(downButton);
        moving.add(createGlue());
        addSetting("main.viewport", moving);

        // Vertical spacing in a really weird way ..
        add(new JPanel(new GridBagLayout()));

        // rendering time
        renderingLabel.setToolTipText(Localization.get("main.rendering.help"));
        add(renderingLabel);
        add(Box.createRigidArea(new Dimension(0, 8)));

        // progress bar
        progressBar.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        progressBar.setMinimum(0);
        progressBar.setMaximum(100);
        add(progressBar);
    }

    // ==== ActionListener Implementation ====

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == fitButton) {
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
            renderingLabel.setText(model.getProgress() < 1.f ?
                Localization.get("main.rendering.title") :
                String.format(Localization.get("main.rendered.title"), 0.1337));
        }
    }

    // ==== Private Helper Methods ====

    private void addSetting(String key, JComponent control) {
        JLabel label = new JLabel(Localization.get(key + ".title"));
        label.setFont(label.getFont().deriveFont(Font.BOLD));
        label.setLabelFor(maxRadiusSpinner);
        control.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        add(label);
        add(Box.createRigidArea(new Dimension(0, 6)));
        add(control);
        add(Box.createRigidArea(new Dimension(0, 6)));
        add(createHelpLabel(Localization.get(key + ".help")));
    }

    private static JTextArea createHelpLabel(String text) {
        JTextArea textArea = new JTextArea();
        textArea.setFont(textArea.getFont().deriveFont(Font.ITALIC));
        textArea.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        textArea.setEditable(false);
        textArea.setCursor(null);
        textArea.setOpaque(false);
        textArea.setFocusable(false);
        textArea.setText(text);
        textArea.setMaximumSize(new Dimension(300, 400));
        textArea.setLineWrap(true);
        return textArea;
    }

    private static JButton createControlButton(String key) {
        JButton button = new JButton(Localization.get(key + ".title"));
        button.setToolTipText(Localization.get(key + ".help"));
        button.setFont(button.getFont().deriveFont(16.f));
        return button;
    }

}
