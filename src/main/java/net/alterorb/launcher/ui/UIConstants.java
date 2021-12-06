package net.alterorb.launcher.ui;

import com.formdev.flatlaf.FlatDarkLaf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.FontUIResource;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.io.InputStream;

public class UIConstants {

    private static final Logger LOGGER = LoggerFactory.getLogger(UIConstants.class);

    public interface Colors {

        Color DARCULA = new Color(60, 63, 65);
        Color DARCULA_DARKENED_ALTERNATIVE = new Color(52, 54, 56);
        Color DARCULA_HOVERED = new Color(75, 110, 175);
        Color TEXT_DEFAULT = new Color(215, 212, 210);
        Color TEXT_ERROR = new Color(199, 64, 60);
    }

    public interface Fonts {

        Font OPEN_SANS_12 = new Font("Open Sans", Font.PLAIN, 12);
        Font OPEN_SANS_13 = new Font("Open Sans", Font.PLAIN, 13);
    }

    public static void loadResources() {
        UIManager.put("ToolTip.background", Colors.DARCULA_DARKENED_ALTERNATIVE);
        UIManager.put("ToolTip.foreground", Colors.TEXT_DEFAULT);

        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (UnsupportedLookAndFeelException e) {
            LOGGER.warn("Failed to load LAF, falling back to system's look and feel.");
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {
                // Cannot happen
            }
        }
        var clazz = UIConstants.class;

        registerFont(clazz.getResourceAsStream("/fonts/opensans.ttf"));
        registerFont(clazz.getResourceAsStream("/fonts/opensans-bold.ttf"));

        setDefaultFont(new FontUIResource(Fonts.OPEN_SANS_13));
    }

    private static void registerFont(InputStream stream) {
        try {
            var font = Font.createFont(Font.TRUETYPE_FONT, stream);
            var environment = GraphicsEnvironment.getLocalGraphicsEnvironment();

            environment.registerFont(font);
        } catch (FontFormatException | IOException e) {
            LOGGER.error("Failed to register font", e);
        }
    }

    private static void setDefaultFont(FontUIResource font) {
        var keys = UIManager.getDefaults().keys();

        while (keys.hasMoreElements()) {
            var key = keys.nextElement();
            var value = UIManager.get(key);

            if (value instanceof FontUIResource) {
                UIManager.put(key, font);
            }
        }
    }
}
