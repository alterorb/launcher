package net.alterorb.launcher.ui;

import net.alterorb.launcher.AssetLoader;
import net.alterorb.launcher.model.GameManifest;
import net.alterorb.launcher.ui.UIConstants.Colors;
import net.alterorb.launcher.ui.UIConstants.Fonts;

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

public class GameThumbnail extends JComponent {

    private static final RenderingHints ANTI_ALIAS_HINT = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    private static final int MAX_GAME_TITLE_WIDTH = 95;
    private static final int MINIMUM_WIDTH = 105;
    private static final int MINIMUM_HEIGHT = 115;

    private final GameManifest manifest;
    private final BufferedImage thumbnail;

    private boolean selected;
    private boolean hovered;

    public GameManifest manifest() {
        return manifest;
    }

    public boolean isSelected() {
        return selected;
    }

    public GameThumbnail(GameManifest manifest) {
        this.manifest = manifest;
        thumbnail = AssetLoader.getThumbnail(manifest.thumbnail());
        setPreferredSize(new Dimension(MINIMUM_WIDTH, MINIMUM_HEIGHT));
        addMouseListener(new MouseHoverListener());
    }

    public void selected(boolean selected) {
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
        drawGameTitle(g, manifest.name());
    }

    private void drawGameTitle(Graphics g, String title) {
        FontMetrics fontMetrics = g.getFontMetrics();
        int titleWidth = fontMetrics.stringWidth(title);

        if (titleWidth > MAX_GAME_TITLE_WIDTH) {
            drawGameTitleWrapped(g, title);
        } else {
            g.drawString(title, getWidth() / 2 - titleWidth / 2, 100);
        }
    }

    private void drawGameTitleWrapped(Graphics g, String title) {
        var fontMetrics = g.getFontMetrics();
        var words = title.split(" ");
        var lineBuffer = new StringBuilder();

        int currentWord = 0;
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
