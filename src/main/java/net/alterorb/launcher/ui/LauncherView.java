package net.alterorb.launcher.ui;

import lombok.RequiredArgsConstructor;
import net.alterorb.launcher.alterorb.AvailableGame;
import net.alterorb.launcher.ui.UIConstants.Colors;
import net.alterorb.launcher.ui.UIConstants.Fonts;

import javax.inject.Singleton;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class LauncherView extends JFrame {

    private static final int GAME_LIST_CONTAINER_WIDTH = 400;

    private final JButton launchButton = new JButton("Launch");
    private final JProgressBar progressBar = new JProgressBar();
    private final JLabel progressBarText = new JLabel();
    private final JPanel gameListContainer = new JPanel();
    private final JLabel placeholderText = new JLabel("Fetching games list...");

    private final LauncherViewModel model;

    private List<GameThumbnail> gameThumbnails;

    LauncherView(LauncherController controller, LauncherViewModel model) {
        this.model = model;
        setTitle("AlterOrb Launcher");
        setSize(514, 391);
        setResizable(false);
        setLocationRelativeTo(null);
        addWindowListener(controller);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        JPanel rootContainer = new JPanel();
        rootContainer.setLayout(new BoxLayout(rootContainer, BoxLayout.Y_AXIS));
        add(rootContainer);

        rootContainer.add(Box.createRigidArea(new Dimension(0, 6)));

        placeholderText.setHorizontalAlignment(JLabel.CENTER);
        gameListContainer.setLayout(new BorderLayout());
        gameListContainer.setBackground(Colors.DARCULA);
        gameListContainer.setMaximumSize(new Dimension(GAME_LIST_CONTAINER_WIDTH, 290));
        gameListContainer.add(placeholderText, BorderLayout.CENTER);

        JScrollPane scrollPane = new JScrollPane(gameListContainer);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setPreferredSize(new Dimension(490, 300));
        scrollPane.setMaximumSize(new Dimension(490, 300));
        rootContainer.add(scrollPane);

        rootContainer.add(Box.createRigidArea(new Dimension(0, 3)));

        JPanel bottomContainer = new JPanel();
        bottomContainer.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 0));
        bottomContainer.setMaximumSize(new Dimension(490, 40));
        rootContainer.add(bottomContainer);

        JPanel progressBarContainer = new JPanel();
        progressBarContainer.setLayout(new BoxLayout(progressBarContainer, BoxLayout.Y_AXIS));
        progressBarContainer.setPreferredSize(new Dimension(330, 40));
        bottomContainer.add(progressBarContainer);

        progressBarContainer.add(Box.createRigidArea(new Dimension(0, 6)));

        progressBar.setIndeterminate(true);
        progressBar.setAlignmentX(Component.CENTER_ALIGNMENT);
        progressBar.setMaximumSize(new Dimension(315, 9));
        progressBar.setBorderPainted(true);
        progressBarContainer.add(progressBar);

        progressBarText.setFont(Fonts.OPEN_SANS_12);
        progressBarText.setHorizontalAlignment(JLabel.CENTER);
        progressBarText.setAlignmentX(Component.CENTER_ALIGNMENT);
        progressBarText.setMaximumSize(new Dimension(315, 20));
        progressBarText.setText("Checking for launcher updates...");
        progressBarContainer.add(progressBarText);

        launchButton.setEnabled(false);
        launchButton.setFont(Fonts.OPEN_SANS_13);
        launchButton.setPreferredSize(new Dimension(130, 30));
        launchButton.setFocusable(false);
        launchButton.setToolTipText("Select a game from the list first before launching it");
        launchButton.addActionListener(controller::launch);
        bottomContainer.add(launchButton);
    }

    public void disableLaunchButton() {
        SwingUtilities.invokeLater(() -> launchButton.setEnabled(false));
    }

    void updateProgressBar(int percentage) {

        SwingUtilities.invokeLater(() -> {

            if (!progressBar.isVisible()) {
                progressBar.setVisible(true);
            }
            progressBar.setIndeterminate(false);
            progressBar.setValue(percentage);
        });
    }

    void hideProgressBarAndText() {

        SwingUtilities.invokeLater(() -> {
            progressBar.setVisible(false);
            progressBarText.setText(" "); // cheap hax so the layout manager doesn't blow up
        });
    }

    void updateProgressBarText(String text, Color color) {

        SwingUtilities.invokeLater(() -> {

            if (text != null) {
                progressBarText.setForeground(color);
            }
            progressBarText.setText(text);
        });
    }

    void updateGameList(List<AvailableGame> games) {
        SwingUtilities.invokeLater(() -> {
            gameListContainer.remove(placeholderText);
            gameListContainer.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
            gameThumbnails = new ArrayList<>(games.size());

            for (AvailableGame game : games) {
                GameThumbnail gameThumbnail = new GameThumbnail(game);
                gameThumbnail.addMouseListener(new ThumbnailSelectionListener(gameThumbnail));
                gameListContainer.add(gameThumbnail);
                gameThumbnails.add(gameThumbnail);
            }
            gameListContainer.setPreferredSize(new Dimension(GAME_LIST_CONTAINER_WIDTH, 115 * (games.size() / 3)));
            gameListContainer.revalidate();
            gameListContainer.repaint();
        });
    }

    @RequiredArgsConstructor
    private class ThumbnailSelectionListener extends MouseAdapter {

        private final GameThumbnail gameThumbnail;

        @Override
        public void mouseClicked(MouseEvent mouseEvent) {
            boolean selected = !gameThumbnail.isSelected();
            launchButton.setEnabled(selected);
            gameThumbnail.setSelected(selected);

            if (selected) {
                model.setSelectedGame(gameThumbnail.getAvailableGame());

                for (GameThumbnail thumbnail : gameThumbnails) {

                    if (thumbnail != gameThumbnail) {
                        thumbnail.setSelected(false);
                    }
                }
            } else {
                model.setSelectedGame(null);
            }
        }
    }
}
