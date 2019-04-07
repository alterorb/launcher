package net.alterorb.launcher.ui;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.alterorb.launcher.alterorb.AlterorbGame;
import net.alterorb.launcher.ui.UIConstants.Colors;
import net.alterorb.launcher.ui.UIConstants.Fonts;
import net.alterorb.launcher.ui.component.GameThumbnail;
import net.alterorb.launcher.ui.controller.LauncherController;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
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

    @Getter
    private AlterorbGame selectedGame;
    private List<GameThumbnail> gameThumbnails;

    @Inject
    public LauncherView(LauncherController controller) {
        setTitle("AlterOrb Launcher");
        setLayout(null);
        setSize(515, 385);
        setResizable(false);
        setLocationRelativeTo(null);

        placeholderText.setHorizontalAlignment(JLabel.CENTER);

        gameListContainer.setLayout(new BorderLayout());
        gameListContainer.setBackground(Colors.DARCULA_DARKENED);
        gameListContainer.setPreferredSize(new Dimension(GAME_LIST_CONTAINER_WIDTH, 290));
        gameListContainer.add(placeholderText, BorderLayout.CENTER);

        JScrollPane scrollPane = new JScrollPane(gameListContainer);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBounds(8, 8, 490, 300);
        add(scrollPane);

        progressBar.setVisible(false);
        progressBar.setBounds(14, 312, 315, 20);
        progressBar.setBorderPainted(true);
        add(progressBar);

        progressBarText.setVisible(false);
        progressBarText.setFont(Fonts.OPEN_SANS_12);
        progressBarText.setHorizontalAlignment(JLabel.CENTER);
        progressBarText.setBounds(14, 330, 315, 20);
        add(progressBarText);

        launchButton.setEnabled(false);
        launchButton.setFont(Fonts.OPEN_SANS_13);
        launchButton.setBounds(348, 310, 150, 40);
        launchButton.setFocusable(false);
        launchButton.setToolTipText("Select a game from the list first before launching it");
        launchButton.addActionListener(controller::launch);
        add(launchButton);
    }

    public void updateProgressBar(int percentage) {

        if (!progressBar.isVisible()) {
            progressBar.setVisible(true);
        }
        progressBar.setValue(percentage);
    }

    public void updateProgressBarText(String text) {

        if (!progressBarText.isVisible()) {
            progressBarText.setVisible(true);
        }
        progressBarText.setText(text);
    }

    public void setGameList(List<AlterorbGame> games) {
        SwingUtilities.invokeLater(() -> {
            gameListContainer.remove(placeholderText);
            gameListContainer.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
            gameThumbnails = new ArrayList<>(games.size());

            for (AlterorbGame game : games) {
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
                selectedGame = gameThumbnail.getAlterorbGame();

                for (GameThumbnail thumbnail : gameThumbnails) {

                    if (thumbnail != gameThumbnail) {
                        thumbnail.setSelected(false);
                    }
                }
            } else {
                selectedGame = null;
            }
        }
    }
}
