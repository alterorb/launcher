package net.alterorb.launcher;

import lombok.extern.log4j.Log4j2;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

@Log4j2
public class PatcherClassLoader extends ClassLoader {

    private final Map<String, byte[]> classData = new HashMap<>();

    public PatcherClassLoader(JarFile jar, String gameshellClass, String checkhostMethod, String checkhostMethodDesc) {
        Enumeration<JarEntry> entries = jar.entries();

        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            String entryName = entry.getName();

            if (entryName.endsWith(".class")) {
                String className = entryName.substring(0, entryName.indexOf('.')).trim();

                try (DataInputStream in = new DataInputStream(jar.getInputStream(entry))) {
                    byte[] data = new byte[in.available()];
                    in.readFully(data);

                    if (Objects.equals(className, gameshellClass)) {
                        LOGGER.debug("Patching gameshell class={}", className);
                        data = patchCheckhostMethod(data, checkhostMethod, checkhostMethodDesc);
                    }
                    this.classData.put(className, data);
                } catch (IOException e) {
                    LOGGER.error("Failed to read jar entry {}", entryName);
                    LOGGER.catching(e);
                }
            }
        }
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        Class<?> clazz = findLoadedClass(name);

        if (clazz != null) {
            return clazz;
        } else if (!classData.containsKey(name)) {
            return super.loadClass(name);
        } else {
            byte[] data = classData.remove(name);

            return defineClass(name, data, 0, data.length);
        }
    }

    private byte[] patchCheckhostMethod(byte[] classData, String methodName, String methodDesc) {
        ClassReader reader = new ClassReader(classData);
        ClassNode classNode = new ClassNode();
        reader.accept(classNode, ClassReader.SKIP_DEBUG);

        for (MethodNode methodNode : classNode.methods) {

            if (Objects.equals(methodNode.name, methodName) && Objects.equals(methodNode.desc, methodDesc)) {
                InsnList insnList = new InsnList();
                insnList.add(new InsnNode(Opcodes.ICONST_1));
                insnList.add(new InsnNode(Opcodes.IRETURN));

                methodNode.instructions = insnList;
                methodNode.tryCatchBlocks.clear();
                LOGGER.debug("Patched checkhost method");
                break;
            }
        }

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        classNode.accept(writer);

        return writer.toByteArray();
    }
}
