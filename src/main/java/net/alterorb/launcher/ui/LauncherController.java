package net.alterorb.launcher.ui;

import dagger.Lazy;
import net.alterorb.launcher.Launcher;
import net.alterorb.launcher.alterorb.AvailableGame;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

@Singleton
public class LauncherController extends WindowAdapter {

    private final LauncherViewModel launcherViewModel = new LauncherViewModel();
    private final LauncherView launcherView = new LauncherView(this, launcherViewModel);

    private final Lazy<Launcher> launcher;

    @Inject
    public LauncherController(Lazy<Launcher> launcher) {
        this.launcher = launcher;
    }

    @Override
    public void windowClosing(WindowEvent e) {
        launcher.get().shutdown();
    }

    public void display() {
        launcherView.setVisible(true);
    }

    public void dispose() {
        launcherView.dispose();
    }

    public void updateProgressBar(int percentage) {
        launcherView.updateProgressBar(percentage);
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

    public void updateAvailableGames(List<AvailableGame> games) {
        launcherView.updateGameList(games);
    }

    public void launch(ActionEvent e) {
        launcher.get().launchGame(launcherViewModel.getSelectedGame().getInternalName());
        launcherView.disableLaunchButton();
    }
}
