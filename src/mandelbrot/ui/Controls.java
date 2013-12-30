package mandelbrot.ui;

import mandelbrot.core.Model;
import mandelbrot.ui.locale.Localization;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.html.ObjectView;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;

public class Controls extends Box implements Observer, ActionListener,
    ChangeListener {

    // ==== Constants ====

    private final double ZOOM_FACTOR = .6;
    private final double MOVE_FACTOR = .4;

    // ==== Properties ====

    private final Model model;

    private final JSpinner fpsSpinner = new JSpinner(
        new SpinnerNumberModel(25, 1, 60, 1));
    private final JSpinner maxIterSpinner = new JSpinner(
        new SpinnerNumberModel(1000, 0, 10000, 10));
    private final JSpinner maxRadiusSpinner = new JSpinner(
        new SpinnerNumberModel(2, 0, 100, 0.1));
    private final JButton leftButton = createControlButton("main.left");
    private final JButton rightButton = createControlButton("main.right");
    private final JButton upButton = createControlButton("main.up");
    private final JButton downButton = createControlButton("main.down");
    private final JButton inButton = createControlButton("main.in");
    private final JButton outButton = createControlButton("main.out");
    private final JButton fitButton = createControlButton("main.fit");
    private final JLabel renderingLabel = new JLabel();
    private final JProgressBar progressBar = new JProgressBar();

    // ==== Constructor ====

    public Controls(Model aModel) {
        super(BoxLayout.Y_AXIS);

        setPreferredSize(new Dimension(220, 600));
        setBorder(BorderFactory.createCompoundBorder(
            new MatteBorder(0, 1, 0, 0, new Color(150, 150, 150)),
            new EmptyBorder(20, 20, 20, 20)));

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
        inButton.addActionListener(this);
        outButton.addActionListener(this);
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
        final Object source = e.getSource();
        final Dimension size = model.getSize();

        // fit best
        if (source == fitButton) {
            model.fit();
        }
        // zooming and moving
        else if (source == inButton || source == outButton) {
            model.scale(size.width / 2, size.height / 2, source == inButton ?
                ZOOM_FACTOR : 1/ZOOM_FACTOR);
        // moving
        } else if (source == leftButton) {
            model.translate((int)Math.round(-size.width * MOVE_FACTOR), 0);
        } else if (source == rightButton) {
            model.translate((int)Math.round(size.width * MOVE_FACTOR), 0);
        } else if (source == upButton) {
            model.translate(0, (int)Math.round(-size.height * MOVE_FACTOR));
        } else if (source == downButton) {
            model.translate(0, (int)Math.round(size.height * MOVE_FACTOR));
        }
    }

    // ==== ChangeListener Implementation

    @Override
    public void stateChanged(ChangeEvent e) {
        if (e.getSource() == fpsSpinner) {
            model.setFps((Integer) fpsSpinner.getModel().getValue());
        } else if (e.getSource() == maxIterSpinner) {
            model.setMaxIterations(
                (Integer) maxIterSpinner.getModel().getValue());
        } else if (e.getSource() == maxRadiusSpinner) {
            model.setMaxRadius(
                (Double)maxRadiusSpinner.getModel().getValue());
        }
    }

    // ==== Observer Implementation ====

    @Override
    public void update(Observable o, Object arg) {
        if (o == model) {
            progressBar.setValue((int)(model.getProgress() * 100));
            fpsSpinner.getModel().setValue(model.getFps());
            maxIterSpinner.getModel().setValue(model.getMaxIterations());
            maxRadiusSpinner.getModel().setValue(model.getMaxRadius());
            renderingLabel.setText(model.getProgress() < 1.f ?
                Localization.get("main.rendering.title") :
                String.format(Localization.get("main.rendered.title"),
                    model.getRenderingTime() / 1000.f));
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
