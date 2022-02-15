#### Downloading the launcher

The latest launcher version can be downloaded from the [releases](https://github.com/alterorb/launcher/releases) page.

#### Compiling the launcher yourself

To compile the launcher you need to use Maven. Most IDEs have maven integration out of the box. 
To compile simply run maven with the `package` goal. After compiling a folder called `target` will be created containing a jar called `alterorb-launcher.jar`, use this jar to start the launcher.

#### Reproducing the AlterOrb game jars

The jars that AlterOrb uses are pre-patched, to reproduce the jar builds or simply see what/how is being patched out 
checkout the [AlterOrb's patcher project](https://github.com/alterorb/patcher).