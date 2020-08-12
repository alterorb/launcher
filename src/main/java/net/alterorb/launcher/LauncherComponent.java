package net.alterorb.launcher;

import dagger.Component;
import net.alterorb.launcher.ui.LauncherController;

import javax.inject.Singleton;

@Singleton
@Component(modules = LauncherModule.class)
public interface LauncherComponent {

    Launcher launcher();

    LauncherConfig launcherConfig();

    LauncherController launcherController();

    VersionCheck versionCheck();
}
