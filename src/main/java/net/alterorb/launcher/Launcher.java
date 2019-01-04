package net.alterorb.launcher;

import com.bulenkov.darcula.DarculaLaf;
import com.google.inject.Guice;
import com.google.inject.Injector;
import lombok.extern.log4j.Log4j2;
import net.alterorb.launcher.task.CheckVersionTask;
import net.alterorb.launcher.ui.UIConstants.Colors;
import net.alterorb.launcher.ui.UIConstants.Fonts;

import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;

@Log4j2
public class Launcher {

    private static void registerFont(InputStream stream) {
        try {
            Font font = Font.createFont(Font.TRUETYPE_FONT, stream);
            GraphicsEnvironment environment = GraphicsEnvironment.getLocalGraphicsEnvironment();

            environment.registerFont(font);
        } catch (FontFormatException | IOException e) {
            LOGGER.error("Failed to register font", e);
        }
    }

    private static void setDefaultFont(FontUIResource font) {
        Enumeration keys = UIManager.getDefaults().keys();

        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);

            if (value instanceof FontUIResource) {
                UIManager.put(key, font);
            }
        }
    }

    public static void main(String[] args) throws IOException {
        UIManager.put("ToolTip.background", Colors.DARCULA_DARKENED_ALTERNATIVE);
        UIManager.put("ToolTip.foreground", Colors.TEXT_DEFAULT);

        try {
            UIManager.setLookAndFeel(new DarculaLaf());
        } catch (Exception e) {
            LOGGER.warn("Failed to load darcula, falling back to system's look and feel.");
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {
                // Cannot happen
            }
        }
        Class<Launcher> clazz = Launcher.class;

        registerFont(clazz.getResourceAsStream("/fonts/opensans.ttf"));
        registerFont(clazz.getResourceAsStream("/fonts/opensans-bold.ttf"));

        setDefaultFont(new FontUIResource(Fonts.OPEN_SANS_13));

        Injector injector = Guice.createInjector(new LauncherModule());
        StorageManager storageManager = injector.getInstance(StorageManager.class);
        storageManager.initializeDirectories();

        CheckVersionTask checkVersionTask = injector.getInstance(CheckVersionTask.class);
        checkVersionTask.run();
    }
}
