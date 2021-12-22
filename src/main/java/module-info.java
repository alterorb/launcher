module alterorb.launcher {
    requires java.net.http;
    requires java.desktop;
    requires org.slf4j;
    requires com.formdev.flatlaf;
    requires jopt.simple;
    requires com.google.gson;

    opens net.alterorb.launcher to com.google.gson;
    opens net.alterorb.launcher.alterorb to com.google.gson;
}