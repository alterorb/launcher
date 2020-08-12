package net.alterorb.launcher.ui;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.alterorb.launcher.alterorb.AvailableGame;
import net.alterorb.launcher.ui.UIConstants.Colors;
import net.alterorb.launcher.ui.UIConstants.Fonts;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;

@Slf4j
public class GameThumbnail extends JComponent {

    private static final RenderingHints ANTI_ALIAS_HINT = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    private static final int MAX_GAME_TITLE_WIDTH = 95;
    private static final int MINIMUM_WIDTH = 105;
    private static final int MINIMUM_HEIGHT = 115;

    @Getter
    private final AvailableGame availableGame;

    @Getter
    private boolean selected;
    private boolean hovered;
    private BufferedImage thumbnail;

    public GameThumbnail(AvailableGame availableGame) {
        this.availableGame = availableGame;
        try {
            thumbnail = ImageIO.read(GameThumbnail.class.getResource("/thumbnails/" + availableGame.getInternalName() + ".jpg"));
        } catch (IOException | IllegalArgumentException e) {
            LOGGER.error("Failed to load game thumbnail", e);
        }
        setPreferredSize(new Dimension(MINIMUM_WIDTH, MINIMUM_HEIGHT));
        addMouseListener(new MouseHoverListener());
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics;
        g.setRenderingHints(ANTI_ALIAS_HINT);

        Color backgroundColor;

        if (selected || hovered) {
            backgroundColor = Colors.DARCULA_HOVERED;
        } else {
            backgroundColor = Colors.DARCULA_DARKENED_ALTERNATIVE;
        }
        g.setColor(backgroundColor);
        g.fillRect(0, 0, getWidth(), getHeight());
        g.drawImage(thumbnail, 5, 5, null);

        g.setFont(Fonts.OPEN_SANS_13);
        g.setColor(Colors.TEXT_DEFAULT);
        drawGameTitle(g, availableGame.getName());
    }

    private void drawGameTitle(Graphics g, String title) {
        FontMetrics fontMetrics = g.getFontMetrics();
        int titleWidth = fontMetrics.stringWidth(title);

        if (titleWidth > MAX_GAME_TITLE_WIDTH) {
            String[] words = title.split(" ");
            int currentWord = 0;
            StringBuilder lineBuffer = new StringBuilder();
            int lineWidth = 0;

            int y = 90;
            while (true) {

                if (currentWord >= words.length) {
                    String line = lineBuffer.toString();

                    g.drawString(line, getWidth() / 2 - fontMetrics.stringWidth(line) / 2, y);
                    break;
                }
                String nextWord = words[currentWord];
                int wordWidth = fontMetrics.stringWidth(nextWord);

                if (lineWidth + wordWidth > MAX_GAME_TITLE_WIDTH) {
                    String line = lineBuffer.toString();

                    g.drawString(line, getWidth() / 2 - fontMetrics.stringWidth(line) / 2, y);
                    lineBuffer = new StringBuilder();
                    lineWidth = 0;
                    y += fontMetrics.getHeight();
                } else {
                    lineBuffer.append(nextWord).append(' ');
                    lineWidth += wordWidth;
                    currentWord++;
                }
            }
        } else {
            g.drawString(title, getWidth() / 2 - titleWidth / 2, 100);
        }
    }

    private class MouseHoverListener extends MouseAdapter {

        private final Cursor HAND_CURSOR = new Cursor(Cursor.HAND_CURSOR);

        @Override
        public void mouseEntered(MouseEvent e) {
            hovered = true;
            setCursor(HAND_CURSOR);
            repaint();
        }

        @Override
        public void mouseExited(MouseEvent e) {
            hovered = false;
            repaint();
        }
    }
}
