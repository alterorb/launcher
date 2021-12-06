package net.alterorb.launcher.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("rawtypes")
public final class EventDispatcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventDispatcher.class);
    private static final Map<Class<? extends Event>, List<EventListener>> REGISTERED_LISTENERS = new HashMap<>();

    private EventDispatcher() {
    }

    public static <T extends Event> void register(Class<T> event, EventListener<T> listener) {
        List<EventListener> listeners = REGISTERED_LISTENERS.computeIfAbsent(event, key -> new ArrayList<>());

        listeners.add(listener);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Event> void dispatch(T event) {
        Class<? extends Event> eventClass = event.getClass();
        List<EventListener> listeners = REGISTERED_LISTENERS.get(eventClass);

        if (listeners == null) {
            return;
        }

        for (var listener : listeners) {

            try {
                listener.onEvent(event);
            } catch (Throwable e) {
                LOGGER.warn("Uncaught exception on event listener", e);
            }
        }
    }
}