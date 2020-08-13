package net.alterorb.launcher;

import com.formdev.flatlaf.FlatDarkLaf;
import lombok.extern.slf4j.Slf4j;
import net.alterorb.launcher.ui.LauncherController;
import net.alterorb.launcher.ui.UIConstants.Colors;
import net.alterorb.launcher.ui.UIConstants.Fonts;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.FontUIResource;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;

@Slf4j
public class Bootstrap {

    public static void main(String[] args) {
        LOGGER.info("Bootstrapping the launcher...");
        setupUiResources();
        LauncherComponent component = DaggerLauncherComponent.create();
        LauncherController launcherController = component.launcherController();

        component.launcherConfig().load(args);

        launcherController.display();
        try {
            if (component.versionCheck().check()) {
                LOGGER.info("Launcher is up to date");
                component.launcher().initialize();
            } else {
                LOGGER.info("Outdated launcher version={}", Launcher.VERSION);
                launcherController.setProgressBarMessage("Outdated launcher, download a new one at alterorb.net", Colors.TEXT_ERROR);
            }
        } catch (IOException e) {
            launcherController.setProgressBarMessage("There was an error while bootstrapping the launcher", Colors.TEXT_ERROR);
            LOGGER.error("Failed to validate launcher version", e);
        }
    }

    private static void setupUiResources() {
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
        Class<Launcher> clazz = Launcher.class;

        registerFont(clazz.getResourceAsStream("/fonts/opensans.ttf"));
        registerFont(clazz.getResourceAsStream("/fonts/opensans-bold.ttf"));

        setDefaultFont(new FontUIResource(Fonts.OPEN_SANS_13));
    }

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
        Enumeration<Object> keys = UIManager.getDefaults().keys();

        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);

            if (value instanceof FontUIResource) {
                UIManager.put(key, font);
            }
        }
    }
}
