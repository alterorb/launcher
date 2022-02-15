package net.alterorb.launcher.ui;

import net.alterorb.launcher.alterorb.AlterOrbGame;
import net.alterorb.launcher.event.EventDispatcher;
import net.alterorb.launcher.event.ui.GameSelectedEvent;
import net.alterorb.launcher.event.ui.LaunchGameEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.util.List;

public final class LauncherController extends WindowAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(LauncherController.class);

    private static final LauncherController INSTANCE = new LauncherController();

    private final LauncherView launcherView = new LauncherView(this);

    private AlterOrbGame selectedGame;

    private LauncherController() {
        EventDispatcher.register(GameSelectedEvent.class, this::onGameSelected);
    }

    public static LauncherController instance() {
        return INSTANCE;
    }

    public void display() {
        launcherView.setVisible(true);
    }

    public void dispose() {
        launcherView.dispose();
    }

    public void setProgressBarMessage(String text) {
        setProgressBarMessage(text, null);
    }

    public void setProgressBarMessage(String text, Color color) {
        launcherView.updateProgressBarText(text, color);
    }

    public void hideProgressBarAndText() {
        launcherView.hideProgressBarAndText();
    }

    public void updateAvailableGames(List<AlterOrbGame> games) {
        launcherView.updateGameList(games);
    }

    public void launch(ActionEvent e) {
        EventDispatcher.dispatch(new LaunchGameEvent(selectedGame));
        launcherView.disableLaunchButton();
    }

    private void onGameSelected(GameSelectedEvent event) {
        LOGGER.debug("Selected game {}", event.game().name());
        selectedGame = event.game();
    }
}
