package net.alterorb.launcher.ui;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import java.awt.Color;

public class ProgressBarView extends JFrame {

    private final JProgressBar progressBar = new JProgressBar();
    private final JLabel textLabel = new JLabel("Checking launcher version");

    public ProgressBarView() {
        setResizable(false);
        setLayout(null);
        setUndecorated(true);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(300, 73);
        setLocationRelativeTo(null);

        textLabel.setBounds(5, 5, 290, 30);
        textLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(textLabel);

        progressBar.setBounds(5, 35, 290, 35);
        progressBar.setIndeterminate(true);
        add(progressBar);
    }

    public void setText(String text) {
        textLabel.setText(text);
    }

    public void switchToErrorState(String message) {
        textLabel.setText(message);
        textLabel.setForeground(Color.RED);
        remove(progressBar);

        JButton exitButton = new JButton("Exit");
        exitButton.setBounds(5, 35, 290, 35);
        exitButton.addActionListener(e -> System.exit(0));
        add(exitButton);
        this.repaint();
    }

    public void setProgressBarPercentage(int percentage) {
        progressBar.setIndeterminate(false);
        progressBar.setStringPainted(true);
        progressBar.setValue(percentage);
    }
}
