package net.alterorb.launcher.ui.controller;

import net.alterorb.launcher.task.FetchGameListTask;
import net.alterorb.launcher.task.ValidateGamepackTask;
import net.alterorb.launcher.ui.LauncherView;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.awt.event.ActionEvent;
import java.util.concurrent.ExecutorService;

@Singleton
public class LauncherController {

    @Inject
    @Named("singleThread")
    private ExecutorService executorService;

    @Inject
    private LauncherView launcherView;

    @Inject
    private FetchGameListTask fetchGameListTask;

    @Inject
    private ValidateGamepackTask validateGamepackTask;

    public void display() {
        launcherView.setVisible(true);
        executorService.submit(fetchGameListTask);
    }

    public void dispose() {
        launcherView.dispose();
    }

    public void launch(ActionEvent e) {
        executorService.submit(validateGamepackTask);
    }
}
