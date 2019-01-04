package net.alterorb.launcher.patcher;

public interface Patch {

    boolean applicable(String className);

    byte[] apply(byte[] classData);
}
