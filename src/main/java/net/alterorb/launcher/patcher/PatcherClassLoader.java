package net.alterorb.launcher.patcher;

import lombok.extern.slf4j.Slf4j;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

@Slf4j
public class PatcherClassLoader extends ClassLoader {

    private final Map<String, byte[]> classData = new HashMap<>();

    public PatcherClassLoader(JarFile jar, List<Patch> patches) {
        Enumeration<JarEntry> entries = jar.entries();

        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            String entryName = entry.getName();

            if (entryName.endsWith(".class")) {
                String className = entryName.substring(0, entryName.indexOf('.'))
                                            .replace('/', '.')
                                            .trim();

                try (DataInputStream in = new DataInputStream(jar.getInputStream(entry))) {
                    byte[] data = new byte[in.available()];
                    in.readFully(data);

                    for (Patch patch : patches) {

                        if (patch.applicable(className)) {
                            LOGGER.debug("Applying patch={} to {}", patch.getClass().getSimpleName(), className);
                            data = patch.apply(data);
                        }
                    }
                    this.classData.put(className, data);
                } catch (IOException e) {
                    LOGGER.error("Failed to read jar entry", e);
                }
            }
        }
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        Class<?> clazz = findLoadedClass(name);

        if (clazz != null) {
            return clazz;
        } else if (classData.containsKey(name)) {
            byte[] data = classData.remove(name);

            return defineClass(name, data, 0, data.length);
        } else {
            return super.loadClass(name);
        }
    }
}
