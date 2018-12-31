package net.alterorb.launcher.ui;

import net.alterorb.launcher.ui.UIConstants.Colors;
import net.alterorb.launcher.ui.UIConstants.Fonts;
import net.alterorb.launcher.ui.component.GameThumbnail;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;

public class LauncherView extends JFrame {

    public LauncherView() {
        setTitle("AlterOrb Launcher");
        setLayout(null);
        setSize(515, 490);
        setResizable(false);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panel.setBackground(Colors.DARCULA_DARKENED);
        panel.setPreferredSize(new Dimension(400, 390));

        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBounds(8, 8, 490, 400);
        add(scrollPane);

        panel.add(new GameThumbnail(null));
        panel.add(new GameThumbnail(null));
        panel.add(new GameThumbnail(null));
        panel.add(new GameThumbnail(null));
        panel.add(new GameThumbnail(null));
        panel.add(new GameThumbnail(null));
        panel.add(new GameThumbnail(null));

        JProgressBar progressBar = new JProgressBar();
        progressBar.setBounds(14, 412, 315, 20);
        progressBar.setBorderPainted(true);
        add(progressBar);

        JLabel label = new JLabel("Downloading gamepack");
        label.setFont(Fonts.OPEN_SANS_12);
        label.setHorizontalAlignment(JLabel.CENTER);
        label.setBounds(14, 430, 315, 20);
        add(label);

        JButton launchButton = new JButton("Launch");
        launchButton.setFont(new Font("Open Sans Bold", Font.PLAIN, 14));
        launchButton.setBounds(348, 410, 150, 40);
        launchButton.setFocusable(false);
        launchButton.addActionListener(e -> System.out.println("boot"));
        add(launchButton);
    }
}
