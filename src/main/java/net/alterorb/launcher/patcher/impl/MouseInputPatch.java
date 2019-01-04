package net.alterorb.launcher.patcher.impl;

import lombok.extern.log4j.Log4j2;
import net.alterorb.launcher.patcher.Patch;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ListIterator;
import java.util.Objects;

/**
 * Patches the MouseInput listener to make right click work on jdk9+.
 */
@Log4j2
public class MouseInputPatch implements Patch {

    private String mouseListenerClass;

    @Override
    public boolean applicable(String className) {
        return Objects.equals(className, mouseListenerClass);
    }

    @Override
    public byte[] apply(byte[] classData) {
        ClassReader reader = new ClassReader(classData);
        ClassNode classNode = new ClassNode();
        reader.accept(classNode, ClassReader.SKIP_DEBUG);

        for (MethodNode methodNode : classNode.methods) {

            if (Objects.equals(methodNode.name, "mousePressed")) {
                InsnList instructions = methodNode.instructions;
                ListIterator<AbstractInsnNode> iterator = instructions.iterator();

                while (iterator.hasNext()) {
                    AbstractInsnNode abstractInsn = iterator.next();

                    if (abstractInsn.getOpcode() == Opcodes.INVOKEVIRTUAL) {
                        MethodInsnNode methodInsn = (MethodInsnNode) abstractInsn;

                        if (methodInsn.name.equals("isMetaDown")) {
                            LOGGER.debug("Replaced instruction");
                            MethodInsnNode replacementFieldInsn = new MethodInsnNode(Opcodes.INVOKESTATIC, "javax/swing/SwingUtilities", "isRightMouseButton", "(Ljava/awt/event/MouseEvent;)Z", false);
                            instructions.set(methodInsn, replacementFieldInsn);
                            break;
                        }
                    }
                }
                LOGGER.debug("Patched mousePressed method");
                break;
            }
        }
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        classNode.accept(writer);

        return writer.toByteArray();
    }
}
