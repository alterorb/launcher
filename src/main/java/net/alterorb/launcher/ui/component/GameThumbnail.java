package net.alterorb.launcher.ui.component;

import lombok.extern.log4j.Log4j2;
import net.alterorb.launcher.alterorb.AlterorbGame;
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

@Log4j2
public class GameThumbnail extends JComponent {

    private static final RenderingHints ANTI_ALIAS_HINT = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    private static final int MAX_GAME_TITLE_WIDTH = 95;
    private static final int MINIMUM_WIDTH = 105;
    private static final int MINIMUM_HEIGHT = 115;
    private BufferedImage thumbnail;
    private String gameName;
    private boolean selected;
    private boolean hovered;

    public GameThumbnail(AlterorbGame alterorbGame) {

        try {
            thumbnail = ImageIO.read(GameThumbnail.class.getResource("/thumbnails/orbdefence.jpg"));
        } catch (IOException e) {
            LOGGER.catching(e);
        }
        String[] names = {"Armies of Gielinor", "Dr P. Saves the Earth", "The Track Controller", "Zombie Dawn Multi", "Arcanists", "Orb Defence", "Wizard Run"};
        gameName = names[(int) (Math.random() * names.length)];
        setPreferredSize(new Dimension(MINIMUM_WIDTH, MINIMUM_HEIGHT));
        addMouseListener(new MouseHoverListener());
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics;
        g.setRenderingHints(ANTI_ALIAS_HINT);

        Color backgroundColor;

        if (selected) {
            backgroundColor = Colors.TEXT_SUCCESS;
        } else if (hovered) {
            backgroundColor = Colors.DARCULA_HOVERED;
        } else {
            backgroundColor = Colors.DARCULA;
        }
        g.setColor(backgroundColor);
        g.fillRect(0, 0, getWidth(), getHeight());
        g.drawImage(thumbnail, 5, 5, null);

        g.setFont(Fonts.OPEN_SANS_13);
        g.setColor(Colors.TEXT_DEFAULT);
        drawGameTitle(g, gameName);
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
