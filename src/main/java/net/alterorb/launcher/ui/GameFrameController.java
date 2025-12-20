package net.alterorb.launcher.ui;

import net.alterorb.launcher.event.EventDispatcher;
import net.alterorb.launcher.event.ui.ShutdownEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.SwingUtilities;
import java.applet.Applet;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public final class GameFrameController extends WindowAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(GameFrameController.class);

    private static final GameFrameController INSTANCE = new GameFrameController();

    private final GameFrameView gameFrameView = new GameFrameView(this);

    private GameFrameController() {
    }

    public static GameFrameController instance() {
        return INSTANCE;
    }

    @Override
    public void windowClosing(WindowEvent e) {
        LOGGER.debug("Game frame window is closing");
        EventDispatcher.dispatch(new ShutdownEvent());
    }

    public void display(boolean allowResizing) {
        gameFrameView.setResizable(allowResizing);
        gameFrameView.setLocationRelativeTo(null);
        gameFrameView.setVisible(true);
    }

    public void dispose() {
        gameFrameView.dispose();
    }

    public void addApplet(Applet applet) {
        applet.setMinimumSize(GameFrameView.APPLET_MIN_SIZE);
        applet.setPreferredSize(GameFrameView.APPLET_MIN_SIZE);
        gameFrameView.add(applet);
        gameFrameView.pack();
    }
}
