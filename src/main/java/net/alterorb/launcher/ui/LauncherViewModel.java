package net.alterorb.launcher.ui;

import net.alterorb.launcher.alterorb.AvailableGame;

public class LauncherViewModel {

    private AvailableGame selectedGame;

    public AvailableGame getSelectedGame() {
        return selectedGame;
    }

    public void setSelectedGame(AvailableGame selectedGame) {
        this.selectedGame = selectedGame;
    }
}
