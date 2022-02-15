package net.alterorb.launcher.event;

@FunctionalInterface
public interface EventListener<T extends Event> {

    void onEvent(T event);
}